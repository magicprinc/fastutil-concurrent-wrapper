package com.trivago.fastutilconcurrentwrapper.objkey;

import com.trivago.fastutilconcurrentwrapper.PrimitiveConcurrentMap;
import com.trivago.fastutilconcurrentwrapper.PrimitiveMapBuilder;
import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.objects.Object2LongFunction;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

import java.util.concurrent.locks.Lock;
import java.util.function.BiFunction;

public class ConcurrentObjectLongMap<K> extends PrimitiveConcurrentMap<K,Long> {
    protected final Object2LongOpenHashMap<K>[] maps;
    protected final long defaultValue;

    @SuppressWarnings("unchecked")
		public ConcurrentObjectLongMap (
        int numBuckets,
        int initialCapacity,
        float loadFactor,
        long defaultValue
    ){
        super(numBuckets);
        this.maps = new Object2LongOpenHashMap[numBuckets];
        this.defaultValue = defaultValue;
        for (int i = 0; i < numBuckets; i++)
            maps[i] = new Object2LongOpenHashMap<>(initialCapacity, loadFactor);
    }

    @Override
    protected final Function<K,Long> mapAt (int index) {
        return maps[index];
    }

    public boolean containsKey(K key) {
        int bucket = getBucket(key);

        Lock readLock = locks[bucket].readLock();
        readLock.lock();
        try {
            return maps[bucket].containsKey(key);
        } finally {
            readLock.unlock();
        }
    }

    public long get (K key) {
        int bucket = getBucket(key);

        Lock readLock = locks[bucket].readLock();
        readLock.lock();
        try {
            return maps[bucket].getOrDefault(key, defaultValue);
        } finally {
            readLock.unlock();
        }
    }

    public long put (K key, long value) {
        int bucket = getBucket(key);

        Lock writeLock = locks[bucket].writeLock();
        writeLock.lock();
        try {
            return maps[bucket].put(key, value);
        } finally {
            writeLock.unlock();
        }
    }

    public long getDefaultValue (){ return defaultValue; }

    public long remove (K key) {
        int bucket = getBucket(key);

        Lock writeLock = locks[bucket].writeLock();
        writeLock.lock();
        try {
            return maps[bucket].removeLong(key);
        } finally {
            writeLock.unlock();
        }
    }

    public boolean remove (K key, long value) {
        int bucket = getBucket(key);

        Lock writeLock = locks[bucket].writeLock();
        writeLock.lock();
        try {
            return maps[bucket].remove(key, value);
        } finally {
            writeLock.unlock();
        }
    }

    public long computeIfAbsent (K key, Object2LongFunction<K> mappingFunction) {
        int bucket = getBucket(key);

        Lock writeLock = locks[bucket].writeLock();
        writeLock.lock();
        try {
            return maps[bucket].computeIfAbsent(key, mappingFunction);
        } finally {
            writeLock.unlock();
        }
    }

    public long computeIfPresent (K key, BiFunction<K,Long,Long> mappingFunction) {
        int bucket = getBucket(key);

        Lock writeLock = locks[bucket].writeLock();
        writeLock.lock();
        try {
            return maps[bucket].computeIfPresent(key, mappingFunction);
        } finally {
            writeLock.unlock();
        }
    }

    public static <K> PrimitiveMapBuilder<ConcurrentObjectLongMap<K>,Long> newBuilder () {
        return new PrimitiveMapBuilder<>(){
            @Override
            public ConcurrentObjectLongMap<K> build() {
                return switch (mapMode){
                    case BUSY_WAITING -> new ConcurrentBusyWaitingObjectLongMap<>(buckets, initialCapacity, loadFactor, super.defaultValue);
                    case BLOCKING -> new ConcurrentObjectLongMap<>(buckets, initialCapacity, loadFactor, super.defaultValue);
                };
            }
        };
    }
}