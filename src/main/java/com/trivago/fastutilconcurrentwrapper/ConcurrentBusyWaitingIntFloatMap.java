package com.trivago.fastutilconcurrentwrapper;

import com.trivago.fastutilconcurrentwrapper.wrapper.PrimitiveFastutilIntFloatWrapper;

import java.util.concurrent.locks.Lock;

public class ConcurrentBusyWaitingIntFloatMap extends PrimitiveConcurrentMap implements IntFloatMap {

    private final IntFloatMap[] maps;

    public ConcurrentBusyWaitingIntFloatMap(int numBuckets,
                                            int initialCapacity,
                                            float loadFactor) {
        super(numBuckets);
        this.maps = new IntFloatMap[numBuckets];
        for (int i = 0; i < numBuckets; i++) {
            maps[i] = new PrimitiveFastutilIntFloatWrapper(initialCapacity, loadFactor, IntFloatMap.DEFAULT_VALUE);
        }
    }

    @Override
    public int size() {
        return super.size(maps);
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty(maps);
    }

    @Override
    public boolean containsKey(int key) {
        int bucket = getBucket(key);

        Lock readLock = locks[bucket].readLock();

        while (true) {
            if (readLock.tryLock()) {
                try {
                    return maps[bucket].containsKey(key);
                } finally {
                    readLock.unlock();
                }
            }
        }
    }

    @Override
    public float get(int key) {
        int bucket = getBucket(key);

        Lock readLock = locks[bucket].readLock();

        while (true) {
            if (readLock.tryLock()) {
                try {
                    return maps[bucket].get(key);
                } finally {
                    readLock.unlock();
                }
            }
        }
    }

    @Override
    public float put(int key, float value) {
        int bucket = getBucket(key);

        Lock writeLock = locks[bucket].writeLock();

        while (true) {
            if (writeLock.tryLock()) {
                try {
                    return maps[bucket].put(key, value);
                } finally {
                    writeLock.unlock();
                }
            }
        }
    }

    @Override
    public float remove(int key) {
        int bucket = getBucket(key);

        Lock writeLock = locks[bucket].writeLock();

        while (true) {
            if (writeLock.tryLock()) {
                try {
                    return maps[bucket].remove(key);
                } finally {
                    writeLock.unlock();
                }
            }
        }
    }

}
