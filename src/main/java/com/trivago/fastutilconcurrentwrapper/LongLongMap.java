package com.trivago.fastutilconcurrentwrapper;

import com.trivago.fastutilconcurrentwrapper.map.ConcurrentBusyWaitingLongLongMap;
import com.trivago.fastutilconcurrentwrapper.map.ConcurrentLongLongMap;
import it.unimi.dsi.fastutil.longs.Long2LongFunction;

import java.util.function.BiFunction;

public interface LongLongMap extends PrimitiveKeyMap {
    boolean containsKey (long key);

    /**
     * @param key long map.key
     * @return defaultValue if the key is not present
     */
    long get(long key);

    long put(long key, long value);

    long getDefaultValue();

    long remove(long key);

    boolean remove(long key, long value);

    long computeIfAbsent(long key, Long2LongFunction mappingFunction);

    long computeIfPresent(long key, BiFunction<Long, Long, Long> mappingFunction);

    static PrimitiveMapBuilder<LongLongMap,Long> newBuilder () {
        return new PrimitiveMapBuilder<>(){
            @Override
            public LongLongMap build() {
                long def = defaultValue != null ? defaultValue : 0;
                return switch (mapMode){
                    case BUSY_WAITING -> new ConcurrentBusyWaitingLongLongMap(buckets, initialCapacity, loadFactor, def);
                    case BLOCKING -> new ConcurrentLongLongMap(buckets, initialCapacity, loadFactor, def);
                };
            }
        };
    }
}