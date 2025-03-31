package com.trivago.fastutilconcurrentwrapper.map;

import com.trivago.fastutilconcurrentwrapper.PrimitiveMapBuilder;
import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.longs.Long2FloatFunction;
import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;

import java.util.concurrent.locks.Lock;
import java.util.function.BiFunction;

public class ConcurrentLongFloatMap extends PrimitiveConcurrentMap<Long,Float> {
    protected final Long2FloatOpenHashMap[] maps;
    protected final float defaultValue;

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

    public float getDefaultValue (){ return defaultValue; }

    @Override
    protected Function<Long,Float> mapAt (int index) {
        return maps[index];
    }

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

    public static PrimitiveMapBuilder<ConcurrentLongFloatMap,Float> newBuilder () {
        return new PrimitiveMapBuilder<>(){
            @Override
            public ConcurrentLongFloatMap build () {
                float def = super.defaultValue != null ? super.defaultValue : 0;
                return switch (mapMode){
                    case BUSY_WAITING -> new ConcurrentBusyWaitingLongFloatMap(buckets, initialCapacity, loadFactor, def);
                    case BLOCKING -> new ConcurrentLongFloatMap(buckets, initialCapacity, loadFactor, def);
                };
            }
        };
    }
}