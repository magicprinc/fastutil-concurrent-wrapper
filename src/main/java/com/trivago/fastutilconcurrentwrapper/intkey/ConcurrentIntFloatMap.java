package com.trivago.fastutilconcurrentwrapper.intkey;

import com.trivago.fastutilconcurrentwrapper.PrimitiveConcurrentMap;
import com.trivago.fastutilconcurrentwrapper.PrimitiveMapBuilder;
import it.unimi.dsi.fastutil.ints.Int2FloatFunction;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;

import java.util.function.BiFunction;

public class ConcurrentIntFloatMap extends PrimitiveConcurrentMap<Integer,Float> {
    protected final Int2FloatOpenHashMap[] maps;
    protected final float defaultValue;

    public ConcurrentIntFloatMap(
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

    @Override protected Int2FloatOpenHashMap mapAt (int index){ return maps[index]; }

    public boolean containsKey (int key) {
        int bucket = getBucket(key);
        try (var __ = readAt(bucket)){
            return maps[bucket].containsKey(key);
        }
    }

    public float get(int key) {
        int bucket = getBucket(key);
        try (var __ = readAt(bucket)){
            return maps[bucket].getOrDefault(key, defaultValue);
        }
    }

    public float put(int key, float value) {
        int bucket = getBucket(key);
        try (var __ = writeAt(bucket)){
            return maps[bucket].put(key, value);
        }
    }

    public float getDefaultValue (){ return defaultValue; }

    public float remove(int key) {
        int bucket = getBucket(key);
        try (var __ = writeAt(bucket)){
            return maps[bucket].remove(key);
        }
    }

    public boolean remove(int key, float value) {
        int bucket = getBucket(key);
        try (var __ = writeAt(bucket)){
            return maps[bucket].remove(key, value);
        }
    }

    public float computeIfAbsent(int key, Int2FloatFunction mappingFunction) {
        int bucket = getBucket(key);
        try (var __ = writeAt(bucket)){
            return maps[bucket].computeIfAbsent(key, mappingFunction);
        }
    }

    public float computeIfPresent(int key, BiFunction<Integer, Float, Float> mappingFunction) {
        int bucket = getBucket(key);
        try (var __ = writeAt(bucket)){
            return maps[bucket].computeIfPresent(key, mappingFunction);
        }
    }

    public static PrimitiveMapBuilder<ConcurrentIntFloatMap,Float> newBuilder () {
        return new PrimitiveMapBuilder<>(){
            @Override
            public ConcurrentIntFloatMap build () {
                float def = super.defaultValue != null ? super.defaultValue : 0;
                return switch (mapMode){
                    case BUSY_WAITING -> new ConcurrentBusyWaitingIntFloatMap(buckets, initialCapacity, loadFactor, def);
                    case BLOCKING -> new ConcurrentIntFloatMap(buckets, initialCapacity, loadFactor, def);
                };
            }
        };
    }
}