package com.trivago.fastutilconcurrentwrapper;

import com.trivago.fastutilconcurrentwrapper.map.ConcurrentBusyWaitingLongFloatMap;
import com.trivago.fastutilconcurrentwrapper.map.ConcurrentLongFloatMap;
import it.unimi.dsi.fastutil.longs.Long2FloatFunction;

import java.util.function.BiFunction;

public interface LongFloatMap extends PrimitiveKeyMap {
    boolean containsKey (long key);

    /**
     * @param key key
     * @return 0.0 if the key is not present
     */
    float get(long key);

    float put(long key, float value);

    float getDefaultValue();

    float remove(long key);

    /**
     * Remove this key only if it has the given value.
     *
     * @param key
     * @param value
     * @return
     */
    boolean remove(long key, float value);

    float computeIfAbsent(long key, Long2FloatFunction mappingFunction);

    float computeIfPresent(int key, BiFunction<Long, Float, Float> mappingFunction);

    static PrimitiveMapBuilder<LongFloatMap,Float> newBuilder () {
        return new PrimitiveMapBuilder<>(){
            @Override
            public LongFloatMap build () {
                float def = defaultValue != null ? defaultValue : 0;
                return switch (mapMode){
                    case BUSY_WAITING -> new ConcurrentBusyWaitingLongFloatMap(buckets, initialCapacity, loadFactor, def);
                    case BLOCKING -> new ConcurrentLongFloatMap(buckets, initialCapacity, loadFactor, def);
                };
            }
        };
    }
}