package com.trivago.fastutilconcurrentwrapper;

import com.trivago.fastutilconcurrentwrapper.map.ConcurrentBusyWaitingIntFloatMap;
import com.trivago.fastutilconcurrentwrapper.map.ConcurrentIntFloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatFunction;

import java.util.function.BiFunction;

public interface IntFloatMap extends PrimitiveKeyMap {
    boolean containsKey(int key);

    float get(int key);

    float put(int key, float value);

    float getDefaultValue();

    float remove(int key);

    boolean remove(int key, float value);

    float computeIfAbsent(int key, Int2FloatFunction mappingFunction);

    float computeIfPresent(int key, BiFunction<Integer, Float, Float> mappingFunction);

    static PrimitiveMapBuilder<IntFloatMap,Float> newBuilder () {
        return new PrimitiveMapBuilder<>(){
            @Override
            public IntFloatMap build () {
                float def = defaultValue != null ? defaultValue : 0;
                return switch (mapMode){
                    case BUSY_WAITING -> new ConcurrentBusyWaitingIntFloatMap(buckets, initialCapacity, loadFactor, def);
                    case BLOCKING -> new ConcurrentIntFloatMap(buckets, initialCapacity, loadFactor, def);
                };
            }
        };
    }
}