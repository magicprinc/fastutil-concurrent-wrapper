package com.trivago.fastutilconcurrentwrapper.map;

import com.trivago.fastutilconcurrentwrapper.LongIntMap;
import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.longs.Long2IntFunction;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;

import java.util.concurrent.locks.Lock;
import java.util.function.BiFunction;

public class ConcurrentLongIntMap extends PrimitiveConcurrentMap<Long,Integer> implements LongIntMap {
    private final Long2IntOpenHashMap[] maps;
    private final int defaultValue;

    public ConcurrentLongIntMap(
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

    @Override
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

    @Override public int getDefaultValue (){ return defaultValue; }

    @Override
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

    @Override
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

    @Override
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

    @Override
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
}