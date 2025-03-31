package com.trivago.fastutilconcurrentwrapper.intkey;

import com.trivago.fastutilconcurrentwrapper.PrimitiveConcurrentMap;
import com.trivago.fastutilconcurrentwrapper.PrimitiveMapBuilder;
import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

import java.util.concurrent.locks.Lock;
import java.util.function.BiFunction;

public class ConcurrentIntIntMap extends PrimitiveConcurrentMap<Integer,Integer> {
    protected final Int2IntOpenHashMap[] maps;
    protected final int defaultValue;

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
    protected Function<Integer,Integer> mapAt (int index) {
        return maps[index];
    }

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

    public int getDefaultValue (){ return defaultValue; }

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

    public static PrimitiveMapBuilder<ConcurrentIntIntMap,Integer> newBuilder () {
        return new PrimitiveMapBuilder<>(){
            @Override
            public ConcurrentIntIntMap build () {
                int def = super.defaultValue != null ? super.defaultValue : 0;
                return switch (mapMode){
                    case BUSY_WAITING -> new ConcurrentBusyWaitingIntIntMap(buckets, initialCapacity, loadFactor, def);
                    case BLOCKING -> new ConcurrentIntIntMap(buckets, initialCapacity, loadFactor, def);
                };
            }
        };
    }
}