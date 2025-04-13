package com.trivago.fastutilconcurrentwrapper.longkey;

import com.trivago.fastutilconcurrentwrapper.PrimitiveConcurrentMap;
import com.trivago.fastutilconcurrentwrapper.PrimitiveMapBuilder;
import it.unimi.dsi.fastutil.longs.Long2IntFunction;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;

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

    @Override protected Long2IntOpenHashMap mapAt (int index){ return maps[index]; }

    public boolean containsKey(long key) {
        int bucket = getBucket(key);
        try (var __ = readAt(bucket)){
            return maps[bucket].containsKey(key);
        }
    }

    public int get(long key) {
        int bucket = getBucket(key);
        try (var __ = readAt(bucket)){
            return maps[bucket].getOrDefault(key, defaultValue);
        }
    }

    public int put (long key, int value) {
        int bucket = getBucket(key);
        try (var __  = writeAt(bucket)){
            return maps[bucket].put(key, value);
        }
    }

    public int getDefaultValue (){ return defaultValue; }

    public int remove(long key) {
        int bucket = getBucket(key);
        try (var __  = writeAt(bucket)){
            return maps[bucket].remove(key);
        }
    }

    public boolean remove(long key, int value) {
        int bucket = getBucket(key);
        try (var __  = writeAt(bucket)){
            return maps[bucket].remove(key, value);
        }
    }

    public int computeIfAbsent(long key, Long2IntFunction mappingFunction) {
        int bucket = getBucket(key);
        try (var __  = writeAt(bucket)){
            return maps[bucket].computeIfAbsent(key, mappingFunction);
        }
    }

    public int computeIfPresent(long key, BiFunction<Long, Integer, Integer> mappingFunction) {
        int bucket = getBucket(key);
        try (var __  = writeAt(bucket)){
            return maps[bucket].computeIfPresent(key, mappingFunction);
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