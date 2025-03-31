package com.trivago.fastutilconcurrentwrapper;

import com.trivago.fastutilconcurrentwrapper.map.ConcurrentBusyWaitingLongIntMap;
import com.trivago.fastutilconcurrentwrapper.map.ConcurrentLongIntMap;
import it.unimi.dsi.fastutil.longs.Long2IntFunction;

import java.util.function.BiFunction;

public interface LongIntMap extends PrimitiveKeyMap {
    boolean containsKey (long key);

    /**
     * @param key key to get
     * @return configured LongIntMap.getDefaultValue(), if the key is not present
     */
    int get(long key);

    int put(long key, int value);

    int getDefaultValue();

    int remove(long key);

    boolean remove(long key, int value);

    int computeIfAbsent(long key, Long2IntFunction mappingFunction);

    int computeIfPresent(long key, BiFunction<Long, Integer, Integer> mappingFunction);

    static PrimitiveMapBuilder<LongIntMap,Integer> newBuilder () {
        return new PrimitiveMapBuilder<>(){
            @Override
            public LongIntMap build() {
                int def = defaultValue != null ? defaultValue : 0;
                return switch (mapMode){
                    case BUSY_WAITING -> new ConcurrentBusyWaitingLongIntMap(buckets, initialCapacity, loadFactor, def);
                    case BLOCKING -> new ConcurrentLongIntMap(buckets, initialCapacity, loadFactor, def);
                };
            }
        };
    }
}