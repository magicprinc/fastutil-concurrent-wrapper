package com.trivago.fastutilconcurrentwrapper;

import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.HashCommon;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 @see it.unimi.dsi.fastutil.Function
 @see com.google.common.util.concurrent.Striped
 @see org.jctools.maps.NonBlockingHashMapLong
 */
public abstract class PrimitiveConcurrentMap<K,V> implements PrimitiveKeyMap {
    protected final int numBuckets;
    protected final ReadWriteLock[] locks;

    /**
     * Constructs a new {@code PrimitiveConcurrentMap} with the specified number of buckets.
     *
     * <p>The map is initialized with a fixed number of buckets, each protected by a separate {@link ReentrantReadWriteLock}
     * for thread safety. The {@code numBuckets} parameter must be within the range [1, 100,000,000]; otherwise, an
     * {@link IllegalArgumentException} is thrown.
     *
     * @param numBuckets the number of buckets for the map; must be between 1 and 100,000,000
     * @throws IllegalArgumentException if {@code numBuckets} is less than 1 or greater than 100,000,000
     */
    protected PrimitiveConcurrentMap (int numBuckets) {
        if (numBuckets < 1 || numBuckets > 100_000_000)
            throw new IllegalArgumentException("numBuckets must be between 1 and 100_000_000, but: "+ numBuckets);
        this.numBuckets = numBuckets;
        this.locks = new ReadWriteLock[numBuckets];
        for (int i = 0; i < numBuckets; i++)
            locks[i] = new ReentrantReadWriteLock();
    }//new

    /**
 * Retrieves the mapping function for the bucket at the specified index.
 * <p>
 * <strong>Precondition:</strong> The appropriate lock must be held when calling this method.
 *
 * @param index the index of the bucket whose mapping function is to be retrieved
 * @return the mapping function associated with the specified bucket index
 */
    protected abstract Function<K,V> mapAt (int index);

    @Override
		public int size () {
        int sum = 0;
        for (int i = 0; i < numBuckets; i++) {
            Lock readLock = locks[i].readLock();
            readLock.lock();
            try {
                sum += mapAt(i).size();
            } finally {
                readLock.unlock();
            }
        }
        return sum;
    }

    @Override
		public boolean isEmpty () {
        for (int i = 0; i < numBuckets; i++) {
            Lock readLock = locks[i].readLock();
            readLock.lock();
            try {
                boolean nonEmpty = mapAt(i).size() > 0;
                if (nonEmpty)
                    return false;
            } finally {
                readLock.unlock();
            }
        }
        return true;// all sub-maps are empty
    }

    @Override
    public void clear () {
        for (int i = 0; i < numBuckets; i++) {
            Lock lock = locks[i].writeLock();
            lock.lock();
            try {
                mapAt(i).clear();
            } finally {
                lock.unlock();
            }
        }
    }

    protected int getBucket(long key) {
        int hash = Long.hashCode(key);
        return bucket(hash, numBuckets);
    }

    /**
     * Computes the bucket index for the given integer key.
     *
     * <p>
     * The key is used as its own hash code and passed to the static {@link #bucket(int, int)} method,
     * which applies a mixing function to distribute entries uniformly across the available buckets.
     * </p>
     *
     * @param key the integer key for which to compute the bucket index
     * @return the computed bucket index, within the range [0, numBuckets)
     */
    protected int getBucket(int key) {
        return bucket(key, numBuckets);// Integer.hashCode(key) == key
    }

    /**
     * Computes the bucket index for the given key.
     *
     * <p>If the key is {@code null}, returns a default index of 0; otherwise, uses the key's hash
     * code and the total number of buckets to compute the appropriate bucket index.</p>
     *
     * @param key the key used to determine the bucket index, may be {@code null}
     * @return the computed bucket index for the key
     */
    protected int getBucket (Object key) {
        return key != null ? bucket(key.hashCode(), numBuckets) : 0;
    }

    /**
     * Computes the bucket index for the given hash value.
     *
     * <p>This method applies a mixing function to the input hash and returns the absolute value of the remainder
     * when divided by the total number of buckets. The resulting index is within the range [0, bucketSize).
     *
     * @param hash the hash value to be mixed
     * @param bucketSize the total number of available buckets; must be positive
     * @return the computed bucket index
     */
    public static int bucket (int hash, int bucketSize) {
        return Math.abs(HashCommon.mix(hash) % bucketSize);
    }
}