package com.trivago.fastutilconcurrentwrapper.longkey;

import com.trivago.fastutilconcurrentwrapper.PrimitiveConcurrentMap;
import com.trivago.fastutilconcurrentwrapper.PrimitiveMapBuilder;
import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.longs.Long2IntFunction;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;

import java.util.concurrent.locks.Lock;
import java.util.function.BiFunction;

public class ConcurrentLongIntMap extends PrimitiveConcurrentMap<Long,Integer> {
    protected final Long2IntOpenHashMap[] maps;
    protected final int defaultValue;

    public ConcurrentLongIntMap (
        int numBuckets,
        int initialCapacity,
        float loadFactor,
        int defaultValue
    ){
        super(numBuckets);
        this.maps = new Long2IntOpenHashMap[numBuckets];
        this.defaultValue = defaultValue;
        for (int i = 0; i < numBuckets; i++)
            maps[i] = new Long2IntOpenHashMap(initialCapacity, loadFactor);
    }

    @Override
    protected Function<Long,Integer> mapAt (int index) {
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

    public int get(long key) {
        int bucket = getBucket(key);

        Lock readLock = locks[bucket].readLock();
        readLock.lock();
        try {
            return maps[bucket].getOrDefault(key, defaultValue);
        } finally {
            readLock.unlock();
        }
    }

    public int put(long key, int value) {
        int bucket = getBucket(key);

        Lock writeLock = locks[bucket].writeLock();
        writeLock.lock();
        try {
            return maps[bucket].put(key, value);
        } finally {
            writeLock.unlock();
        }
    }

    public int getDefaultValue (){ return defaultValue; }

    public int remove(long key) {
        int bucket = getBucket(key);

        Lock writeLock = locks[bucket].writeLock();
        writeLock.lock();
        try {
            return maps[bucket].remove(key);
        } finally {
            writeLock.unlock();
        }
    }

    public boolean remove(long key, int value) {
        int bucket = getBucket(key);

        Lock writeLock = locks[bucket].writeLock();
        writeLock.lock();
        try {
            return maps[bucket].remove(key, value);
        } finally {
            writeLock.unlock();
        }
    }

    public int computeIfAbsent(long key, Long2IntFunction mappingFunction) {
        int bucket = getBucket(key);

        Lock writeLock = locks[bucket].writeLock();
        writeLock.lock();
        try {
            return maps[bucket].computeIfAbsent(key, mappingFunction);
        } finally {
            writeLock.unlock();
        }
    }

    public int computeIfPresent(long key, BiFunction<Long, Integer, Integer> mappingFunction) {
        int bucket = getBucket(key);

        Lock writeLock = locks[bucket].writeLock();
        writeLock.lock();
        try {
            return maps[bucket].computeIfPresent(key, mappingFunction);
        } finally {
            writeLock.unlock();
        }
    }

    public static PrimitiveMapBuilder<ConcurrentLongIntMap,Integer> newBuilder () {
        return new PrimitiveMapBuilder<>(){
            @Override
            public ConcurrentLongIntMap build() {
                int def = super.defaultValue != null ? super.defaultValue : 0;
                return switch (mapMode){
                    case BUSY_WAITING -> new ConcurrentBusyWaitingLongIntMap(buckets, initialCapacity, loadFactor, def);
                    case BLOCKING -> new ConcurrentLongIntMap(buckets, initialCapacity, loadFactor, def);
                };
            }
        };
    }
}