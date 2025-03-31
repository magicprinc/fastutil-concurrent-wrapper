package com.trivago.fastutilconcurrentwrapper.map;

import com.trivago.fastutilconcurrentwrapper.IntFloatMap;
import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.ints.Int2FloatFunction;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;

import java.util.concurrent.locks.Lock;
import java.util.function.BiFunction;

public class ConcurrentBusyWaitingIntFloatMap extends PrimitiveConcurrentMap<Integer,Float> implements IntFloatMap {
    private final Int2FloatOpenHashMap[] maps;
    private final float defaultValue;

    public ConcurrentBusyWaitingIntFloatMap(
        int numBuckets,
        int initialCapacity,
        float loadFactor,
        float defaultValue
    ){
        super(numBuckets);
        this.maps = new Int2FloatOpenHashMap[numBuckets];
        this.defaultValue = defaultValue;
        for (int i = 0; i < numBuckets; i++)
            maps[i] = new Int2FloatOpenHashMap(initialCapacity, loadFactor);
    }

    @Override
    protected Function<Integer,Float> mapAt (int index) {
        return maps[index];
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
            Thread.onSpinWait();
        }
    }

    @Override
    public float get(int key) {
        int bucket = getBucket(key);

        Lock readLock = locks[bucket].readLock();

        while (true) {
            if (readLock.tryLock()) {
                try {
                    return maps[bucket].getOrDefault(key, defaultValue);
                } finally {
                    readLock.unlock();
                }
            }
            Thread.onSpinWait();
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
            Thread.onSpinWait();
        }
    }

    @Override public float getDefaultValue (){ return defaultValue; }

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
            Thread.onSpinWait();
        }
    }

    @Override
    public boolean remove(int key, float value) {
        int bucket = getBucket(key);

        Lock writeLock = locks[bucket].writeLock();

        while (true) {
            if (writeLock.tryLock()) {
                try {
                    return maps[bucket].remove(key, value);
                } finally {
                    writeLock.unlock();
                }
            }
            Thread.onSpinWait();
        }
    }

    @Override
    public float computeIfAbsent(int key, Int2FloatFunction mappingFunction) {
        int bucket = getBucket(key);

        Lock writeLock = locks[bucket].writeLock();

        while (true) {
            if (writeLock.tryLock()) {
                try {
                    return maps[bucket].computeIfAbsent(key, mappingFunction);
                } finally {
                    writeLock.unlock();
                }
            }
            Thread.onSpinWait();
        }
    }

    @Override
    public float computeIfPresent(int key, BiFunction<Integer, Float, Float> mappingFunction) {
        int bucket = getBucket(key);

        Lock writeLock = locks[bucket].writeLock();

        while (true) {
            if (writeLock.tryLock()) {
                try {
                    return maps[bucket].computeIfPresent(key, mappingFunction);
                } finally {
                    writeLock.unlock();
                }
            }
            Thread.onSpinWait();
        }
    }
}