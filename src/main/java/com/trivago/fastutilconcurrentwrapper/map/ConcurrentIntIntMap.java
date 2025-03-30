package com.trivago.fastutilconcurrentwrapper.map;

import com.trivago.fastutilconcurrentwrapper.IntIntMap;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

import java.util.concurrent.locks.Lock;
import java.util.function.BiFunction;

public class ConcurrentIntIntMap extends PrimitiveConcurrentMap implements IntIntMap {
    private final Int2IntOpenHashMap[] maps;
    private final int defaultValue;

    public ConcurrentIntIntMap(
        int numBuckets,
        int initialCapacity,
        float loadFactor,
        int defaultValue
    ){
        super(numBuckets);
        this.maps = new Int2IntOpenHashMap[numBuckets];
        this.defaultValue = defaultValue;
        for (int i = 0; i < numBuckets; i++)
            maps[i] = new Int2IntOpenHashMap(initialCapacity, loadFactor);
    }

    @Override
    protected int sizeOfMap (int index) {
        return maps[index].size();
    }

    @Override
    public boolean containsKey(int key) {
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
    public int get (int key) {
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
    public int put(int key, int value) {
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
    public int remove(int key) {
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
    public boolean remove(int key, int value) {
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
    public int computeIfAbsent(int key, Int2IntFunction mappingFunction) {
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
    public int computeIfPresent(int key, BiFunction<Integer, Integer, Integer> mappingFunction) {
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