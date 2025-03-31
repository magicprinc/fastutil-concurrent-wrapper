package com.trivago.fastutilconcurrentwrapper;

import com.trivago.fastutilconcurrentwrapper.map.ConcurrentBusyWaitingIntIntMap;
import com.trivago.fastutilconcurrentwrapper.map.ConcurrentIntIntMap;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;

import java.util.function.BiFunction;

public interface IntIntMap extends PrimitiveKeyMap {
    boolean containsKey(int key);

    /**
     * @param key int Map.key
     * @return defaultValue if the key is not present
     */
    int get(int key);

    int put(int key, int value);

    int getDefaultValue();

    int remove(int key);

    boolean remove(int key, int value);

    int computeIfAbsent(int key, Int2IntFunction mappingFunction);

    int computeIfPresent(int key, BiFunction<Integer, Integer, Integer> mappingFunction);

    static PrimitiveMapBuilder<IntIntMap,Integer> newBuilder () {
        return new PrimitiveMapBuilder<>(){
            @Override
            public IntIntMap build () {
                int def = defaultValue != null ? defaultValue : 0;
                return switch (mapMode){
                    case BUSY_WAITING -> new ConcurrentBusyWaitingIntIntMap(buckets, initialCapacity, loadFactor, def);
                    case BLOCKING -> new ConcurrentIntIntMap(buckets, initialCapacity, loadFactor, def);
                };
            }
        };
    }
}