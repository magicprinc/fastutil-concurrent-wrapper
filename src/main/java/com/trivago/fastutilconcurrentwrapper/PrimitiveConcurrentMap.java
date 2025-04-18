package com.trivago.fastutilconcurrentwrapper;

import com.trivago.fastutilconcurrentwrapper.util.CloseableLock;
import com.trivago.fastutilconcurrentwrapper.util.CloseableReadWriteLock;
import it.unimi.dsi.fastutil.Function;
import jakarta.validation.constraints.Positive;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 @see it.unimi.dsi.fastutil.Function
 @see com.google.common.util.concurrent.Striped
 @see org.jctools.maps.NonBlockingHashMapLong
 */
public abstract class PrimitiveConcurrentMap<K,V> implements PrimitiveKeyMap {
    private final int numBuckets;
    private final CloseableReadWriteLock.Padded[] locks;

    @SuppressWarnings({"resource", "ConstantValue"})
		protected PrimitiveConcurrentMap (@Positive int numBuckets) {
        if (numBuckets < 1 || numBuckets > 100_000_000)
            throw new IllegalArgumentException("numBuckets must be between 1 and 100_000_000, but: "+ numBuckets);
        this.numBuckets = numBuckets;
        this.locks = new CloseableReadWriteLock.Padded[numBuckets];
        for (int i = 0; i < numBuckets; i++)
            locks[i] = new CloseableReadWriteLock.Padded();
    }//new

    /** Lock must be held! */
    protected abstract Function<K,V> mapAt (int index);

    protected CloseableLock readAt (int lockIndex) {
        return locks[lockIndex].read();
    }
    protected CloseableLock writeAt (int lockIndex) {
        return locks[lockIndex].write();
    }
    protected ReentrantReadWriteLock.ReadLock readLock (int lockIndex) {
        return locks[lockIndex].readLock();
    }
    protected ReentrantReadWriteLock.WriteLock writeLock (int lockIndex) {
        return locks[lockIndex].writeLock();
    }

    @Override
		public int size () {
        int sum = 0;
        for (int i = 0; i < numBuckets; i++){
            try (var __ = readAt(i)){
                sum += mapAt(i).size();
            }
        }
        return sum;
    }

    @Override
		public boolean isEmpty () {
        for (int i = 0; i < numBuckets; i++) {
            try (var __ = readAt(i)){
                boolean nonEmpty = mapAt(i).size() > 0;
                if (nonEmpty)
                    return false;
            }
        }
        return true;// all sub-maps are empty
    }

    @Override
    public void clear () {
        for (int i = 0; i < numBuckets; i++){
            try (var __ = writeAt(i)){
                mapAt(i).clear();
            }
        }
    }

    protected int getBucket (long key) {
        return PrimitiveKeyMap.bucket(key, numBuckets);
    }

    protected int getBucket (int key) {
        return PrimitiveKeyMap.bucket(key, numBuckets);// Integer.hashCode(key) == key
    }

    protected int getBucket (Object key) {
        return PrimitiveKeyMap.bucket(key, numBuckets);
    }
}