package com.trivago.fastutilconcurrentwrapper.longkey;

import com.trivago.fastutilconcurrentwrapper.PrimitiveConcurrentMap;
import com.trivago.fastutilconcurrentwrapper.PrimitiveMapBuilder;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.function.BiFunction;

public class ConcurrentLongObjectMap<V> extends PrimitiveConcurrentMap<Long,V> {
    protected final Long2ObjectOpenHashMap<V>[] maps;
    protected final V defaultValue;

    @SuppressWarnings("unchecked")
		public ConcurrentLongObjectMap (
        int numBuckets,
        int initialCapacity,
        float loadFactor,
        V defaultValue
    ){
        super(numBuckets);
        this.maps = new Long2ObjectOpenHashMap[numBuckets];
        this.defaultValue = defaultValue;
        for (int i = 0; i < numBuckets; i++)
            maps[i] = new Long2ObjectOpenHashMap<>(initialCapacity, loadFactor);
    }

    @Override protected final Long2ObjectOpenHashMap<V> mapAt (int index){ return maps[index]; }

    public boolean containsKey(long key) {
        int bucket = getBucket(key);
        try (var __ = readAt(bucket)){
            return maps[bucket].containsKey(key);
        }
    }

    public V get (long key) {
        int bucket = getBucket(key);
        try (var __ = readAt(bucket)){
            return maps[bucket].getOrDefault(key, defaultValue);
        }
    }

    public V put (long key, V value) {
        int bucket = getBucket(key);
        try (var __ = writeAt(bucket)){
            return maps[bucket].put(key, value);
        }
    }

    public V getDefaultValue (){ return defaultValue; }

    public V remove (long key) {
        int bucket = getBucket(key);
        try (var __ = writeAt(bucket)){
            return maps[bucket].remove(key);
        }
    }

    public boolean remove(long key, V value) {
        int bucket = getBucket(key);
        try (var __ = writeAt(bucket)){
            return maps[bucket].remove(key, value);
        }
    }

    public V computeIfAbsent (long key, Long2ObjectFunction<V> mappingFunction) {
        int bucket = getBucket(key);
        try (var __ = writeAt(bucket)){
            return maps[bucket].computeIfAbsent(key, mappingFunction);
        }
    }

    public V computeIfPresent (long key, BiFunction<Long,V,V> mappingFunction) {
        int bucket = getBucket(key);
        try (var __ = writeAt(bucket)){
            return maps[bucket].computeIfPresent(key, mappingFunction);
        }
    }

    public static <V> PrimitiveMapBuilder<ConcurrentLongObjectMap<V>,V> newBuilder () {
        return new PrimitiveMapBuilder<>(){
            @Override
            public ConcurrentLongObjectMap<V> build() {
                return switch (mapMode){
                    case BUSY_WAITING -> new ConcurrentBusyWaitingLongObjectMap<>(buckets, initialCapacity, loadFactor, super.defaultValue);
                    case BLOCKING -> new ConcurrentLongObjectMap<>(buckets, initialCapacity, loadFactor, super.defaultValue);
                };
            }
        };
    }
}