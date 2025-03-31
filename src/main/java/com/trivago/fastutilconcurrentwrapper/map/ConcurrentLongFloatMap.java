package com.trivago.fastutilconcurrentwrapper.map;

import com.trivago.fastutilconcurrentwrapper.LongFloatMap;
import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.longs.Long2FloatFunction;
import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;

import java.util.concurrent.locks.Lock;
import java.util.function.BiFunction;

public class ConcurrentLongFloatMap extends PrimitiveConcurrentMap<Long,Float> implements LongFloatMap {
    private final Long2FloatOpenHashMap[] maps;
    private final float defaultValue;

    public ConcurrentLongFloatMap (
        int numBuckets,
        int initialCapacity,
        float loadFactor,
        float defaultValue
    ){
        super(numBuckets);
        this.maps = new Long2FloatOpenHashMap[numBuckets];
        this.defaultValue = defaultValue;
        for (int i = 0; i < numBuckets; i++)
            maps[i] = new Long2FloatOpenHashMap(initialCapacity, loadFactor);
    }

    @Override public float getDefaultValue (){ return defaultValue; }

    @Override
    protected Function<Long,Float> mapAt (int index) {
        return maps[index];
    }

    @Override
    public boolean containsKey(long key) {
        int bucket = getBucket(key);

        Lock readLock = locks[bucket].readLock();
        readLock.lock();
        try {
            return maps[bucket].containsKey(key);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public float get(long key) {
        int bucket = getBucket(key);

        Lock readLock = locks[bucket].readLock();
        readLock.lock();
        try {
            return maps[bucket].getOrDefault(key, defaultValue);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public float put(long key, float value) {
        int bucket = getBucket(key);

        Lock writeLock = locks[bucket].writeLock();
        writeLock.lock();
        try {
            return maps[bucket].put(key, value);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public float remove(long key) {
        int bucket = getBucket(key);

        Lock writeLock = locks[bucket].writeLock();
        writeLock.lock();
        try {
            return maps[bucket].remove(key);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean remove(long key, float value) {
        int bucket = getBucket(key);

        Lock writeLock = locks[bucket].writeLock();
        writeLock.lock();
        try {
            return maps[bucket].remove(key, value);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public float computeIfAbsent(long key, Long2FloatFunction mappingFunction) {
        int bucket = getBucket(key);

        Lock writeLock = locks[bucket].writeLock();
        writeLock.lock();
        try {
            return maps[bucket].computeIfAbsent(key, mappingFunction);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public float computeIfPresent(int key, BiFunction<Long, Float, Float> mappingFunction) {
        int bucket = getBucket(key);

        Lock writeLock = locks[bucket].writeLock();
        writeLock.lock();
        try {
            return maps[bucket].computeIfPresent(key, mappingFunction);
        } finally {
            writeLock.unlock();
        }
    }
}