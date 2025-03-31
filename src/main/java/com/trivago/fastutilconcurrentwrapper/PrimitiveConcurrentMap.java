package com.trivago.fastutilconcurrentwrapper;

import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.HashCommon;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 @see it.unimi.dsi.fastutil.Function
 @see com.google.common.util.concurrent.Striped
 */
public abstract class PrimitiveConcurrentMap<K,V> implements PrimitiveKeyMap {
    protected final int numBuckets;
    protected final ReadWriteLock[] locks;

    protected PrimitiveConcurrentMap (int numBuckets) {
        if (numBuckets < 1 || numBuckets > 100_000_000)
            throw new IllegalArgumentException("numBuckets must be between 1 and 100_000_000, but: "+ numBuckets);
        this.numBuckets = numBuckets;
        this.locks = new ReadWriteLock[numBuckets];
        for (int i = 0; i < numBuckets; i++)
            locks[i] = new ReentrantReadWriteLock();
    }//new

    /** Lock must be held! */
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

    protected int getBucket(int key) {
        return bucket(key, numBuckets);// Integer.hashCode(key) == key
    }

    public static int bucket (int hash, int bucketSize) {
        return Math.abs(HashCommon.mix(hash) % bucketSize);
    }
}