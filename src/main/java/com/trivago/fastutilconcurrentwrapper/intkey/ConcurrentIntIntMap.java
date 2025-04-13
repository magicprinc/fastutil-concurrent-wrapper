package com.trivago.fastutilconcurrentwrapper.intkey;

import com.trivago.fastutilconcurrentwrapper.PrimitiveConcurrentMap;
import com.trivago.fastutilconcurrentwrapper.PrimitiveMapBuilder;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

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

    @Override protected Int2IntOpenHashMap mapAt (int index){ return maps[index]; }

    public boolean containsKey(int key) {
        int bucket = getBucket(key);
        try (var __ = readAt(bucket)){
            return maps[bucket].containsKey(key);
        }
    }

    public int get (int key) {
        int bucket = getBucket(key);
        try (var __ = readAt(bucket)){
            return maps[bucket].getOrDefault(key, defaultValue);
        }
    }

    public int put(int key, int value) {
        int bucket = getBucket(key);
        try (var __ = writeAt(bucket)){
            return maps[bucket].put(key, value);
        }
    }

    public int getDefaultValue (){ return defaultValue; }

    public int remove (int key) {
        int bucket = getBucket(key);
        try (var __ = writeAt(bucket)){
            return maps[bucket].remove(key);
        }
    }

    public boolean remove(int key, int value) {
        int bucket = getBucket(key);
        try (var __ = writeAt(bucket)){
            return maps[bucket].remove(key, value);
        }
    }

    public int computeIfAbsent(int key, Int2IntFunction mappingFunction) {
        int bucket = getBucket(key);
        try (var __ = writeAt(bucket)){
            return maps[bucket].computeIfAbsent(key, mappingFunction);
        }
    }

    public int computeIfPresent(int key, BiFunction<Integer, Integer, Integer> mappingFunction) {
        int bucket = getBucket(key);
        try (var __ = writeAt(bucket)){
            return maps[bucket].computeIfPresent(key, mappingFunction);
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