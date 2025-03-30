package com.trivago.fastutilconcurrentwrapper.map;

import com.trivago.fastutilconcurrentwrapper.PrimitiveKeyMap;
import it.unimi.dsi.fastutil.HashCommon;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class PrimitiveConcurrentMap implements PrimitiveKeyMap {
    protected final int numBuckets;
    protected final ReadWriteLock[] locks;

    protected PrimitiveConcurrentMap(int numBuckets) {
        this.numBuckets = numBuckets;
        this.locks = new ReadWriteLock[numBuckets];
        for (int i = 0; i < numBuckets; i++)
            locks[i] = new ReentrantReadWriteLock();
    }

    @Override
		public int size () {
        int sum = 0;
        for (int i = 0; i < numBuckets; i++) {
            Lock readLock = locks[i].readLock();
            readLock.lock();
            try {
                sum += sizeOfMap(i);
            } finally {
                readLock.unlock();
            }
        }
        return sum;
    }

    protected abstract int sizeOfMap (int index);

    @Override
		public boolean isEmpty () {
        for (int i = 0; i < numBuckets; i++) {
            Lock readLock = locks[i].readLock();
            readLock.lock();
            try {
                boolean nonEmpty = sizeOfMap(i) > 0;
                if (nonEmpty)
                    return false;
            } finally {
                readLock.unlock();
            }
        }
        return true;// all sub-maps are empty
    }

    protected int getBucket(long key) {
        int hash = Long.hashCode(key);
        return bucket(hash, numBuckets);
    }

    protected int getBucket(int key) {
        return bucket(key, numBuckets);// Integer.hashCode(key) == key
    }

    static int bucket (int hash, int bucketSize) {
        return Math.abs(HashCommon.mix(hash) % bucketSize);
    }
}