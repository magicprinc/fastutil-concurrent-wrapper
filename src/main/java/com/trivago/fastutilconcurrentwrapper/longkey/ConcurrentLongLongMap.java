package com.trivago.fastutilconcurrentwrapper.longkey;

import com.trivago.fastutilconcurrentwrapper.PrimitiveConcurrentMap;
import com.trivago.fastutilconcurrentwrapper.PrimitiveMapBuilder;
import it.unimi.dsi.fastutil.longs.Long2LongFunction;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;

import java.util.function.BiFunction;

public class ConcurrentLongLongMap extends PrimitiveConcurrentMap<Long,Long> {
    protected final Long2LongOpenHashMap[] maps;
    protected final long defaultValue;

    public ConcurrentLongLongMap(
        int numBuckets,
        int initialCapacity,
        float loadFactor,
        long defaultValue
    ){
        super(numBuckets);
        this.maps = new Long2LongOpenHashMap[numBuckets];
        this.defaultValue = defaultValue;
        for (int i = 0; i < numBuckets; i++)
            maps[i] = new Long2LongOpenHashMap(initialCapacity, loadFactor);
    }

    @Override protected final Long2LongOpenHashMap mapAt (int index){ return maps[index]; }

    public boolean containsKey(long key) {
        int bucket = getBucket(key);
        try (var __ = readAt(bucket)){
            return maps[bucket].containsKey(key);
        }
    }

    public long get (long key) {
        int bucket = getBucket(key);
        try (var __ = readAt(bucket)){
            return maps[bucket].getOrDefault(key, defaultValue);
        }
    }

    public long put(long key, long value) {
        int bucket = getBucket(key);
        try (var __ = writeAt(bucket)){
            return maps[bucket].put(key, value);
        }
    }

    public long getDefaultValue (){ return defaultValue; }

    public long remove(long key) {
        int bucket = getBucket(key);
        try (var __ = writeAt(bucket)){
            return maps[bucket].remove(key);
        }
    }

    public boolean remove(long key, long value) {
        int bucket = getBucket(key);
        try (var __ = writeAt(bucket)){
            return maps[bucket].remove(key, value);
        }
    }

    public long computeIfAbsent(long key, Long2LongFunction mappingFunction) {
        int bucket = getBucket(key);
        try (var __ = writeAt(bucket)){
            return maps[bucket].computeIfAbsent(key, mappingFunction);
        }
    }

    public long computeIfPresent(long key, BiFunction<Long, Long, Long> mappingFunction) {
        int bucket = getBucket(key);
        try (var __ = writeAt(bucket)){
            return maps[bucket].computeIfPresent(key, mappingFunction);
        }
    }

    public static PrimitiveMapBuilder<ConcurrentLongLongMap,Long> newBuilder () {
        return new PrimitiveMapBuilder<>(){
            @Override
            public ConcurrentLongLongMap build() {
                long def = super.defaultValue != null ? super.defaultValue : 0;
                return switch (mapMode){
                    case BUSY_WAITING -> new ConcurrentBusyWaitingLongLongMap(buckets, initialCapacity, loadFactor, def);
                    case BLOCKING -> new ConcurrentLongLongMap(buckets, initialCapacity, loadFactor, def);
                };
            }
        };
    }
}