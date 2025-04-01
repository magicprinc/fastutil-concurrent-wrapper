package com.trivago.fastutilconcurrentwrapper.longkey;

import com.trivago.fastutilconcurrentwrapper.PrimitiveConcurrentMap;
import com.trivago.fastutilconcurrentwrapper.PrimitiveMapBuilder;
import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.longs.Long2FloatFunction;
import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;

import java.util.concurrent.locks.Lock;
import java.util.function.BiFunction;

public class ConcurrentLongFloatMap extends PrimitiveConcurrentMap<Long,Float> {
    protected final Long2FloatOpenHashMap[] maps;
    protected final float defaultValue;

    /**
     * Constructs a new ConcurrentLongFloatMap with the specified number of buckets and configuration parameters.
     * 
     * <p>This constructor partitions the map into multiple buckets to improve concurrency. Each bucket is
     * initialized as a Long2FloatOpenHashMap with the specified initial capacity and load factor. The provided
     * default value is used as the fallback when a key is not present in the map.</p>
     *
     * @param numBuckets      the number of buckets for partitioning the map
     * @param initialCapacity the initial capacity for each bucket
     * @param loadFactor      the load factor for resizing the bucket maps
     * @param defaultValue    the default value returned for absent keys
     */
    public ConcurrentLongFloatMap (
        int numBuckets,
        int initialCapacity,
        float loadFactor,
        float defaultValue
    ){
        super(numBuckets);
        this.maps = new Long2FloatOpenHashMap[numBuckets];
        this.defaultValue = defaultValue;
        for (int i = 0; i < numBuckets; i++)
            maps[i] = new Long2FloatOpenHashMap(initialCapacity, loadFactor);
    }

    /**
 * Returns the default value used when a key is not present in the map.
 *
 * @return the default float value for absent keys
 */
public float getDefaultValue (){ return defaultValue; }

    /**
     * Returns the internal bucket map corresponding to the specified index.
     *
     * @param index the index of the bucket in the internal maps array
     * @return the map associated with the given bucket index
     */
    @Override
    protected Function<Long,Float> mapAt (int index) {
        return maps[index];
    }

    /**
     * Checks if the map contains a mapping for the specified key.
     *
     * <p>This method calculates the appropriate bucket for the key, acquires a read lock on that bucket,
     * and then verifies if the key exists in the corresponding sub-map in a thread-safe manner.
     *
     * @param key the key to check for presence in the map
     * @return {@code true} if the key exists in the map; {@code false} otherwise
     */
    public boolean containsKey(long key) {
        int bucket = getBucket(key);

        Lock readLock = locks[bucket].readLock();
        readLock.lock();
        try {
            return maps[bucket].containsKey(key);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Retrieves the float value associated with the specified key.
     *
     * <p>This method identifies the corresponding bucket for the key, locks it for thread-safe access, and
     * returns the associated value if present. If the key is not found, it returns the default value.
     *
     * @param key the key whose associated value is to be returned
     * @return the value associated with the key, or the default value if the key is absent
     */
    public float get(long key) {
        int bucket = getBucket(key);

        Lock readLock = locks[bucket].readLock();
        readLock.lock();
        try {
            return maps[bucket].getOrDefault(key, defaultValue);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Inserts or updates the float value associated with the specified key in a thread-safe manner.
     *
     * <p>This method acquires a write lock on the bucket determined by the key, updates the mapping,
     * and returns the previous value associated with the key, or the default value if the key was absent.</p>
     *
     * @param key   the key for which the value is to be inserted or updated
     * @param value the new float value to associate with the key
     * @return the previous value mapped to the key, or the default value if no mapping existed
     */
    public float put(long key, float value) {
        int bucket = getBucket(key);

        Lock writeLock = locks[bucket].writeLock();
        writeLock.lock();
        try {
            return maps[bucket].put(key, value);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Removes the mapping for the specified key from this map.
     *
     * <p>This method locates the bucket corresponding to the key, acquires the write lock to ensure thread safety,
     * and removes the associated value. If the key is not present, the default value is returned.
     *
     * @param key the key whose mapping is to be removed
     * @return the previous value associated with the key, or the default value if no mapping existed
     */
    public float remove(long key) {
        int bucket = getBucket(key);

        Lock writeLock = locks[bucket].writeLock();
        writeLock.lock();
        try {
            return maps[bucket].remove(key);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Conditionally removes the mapping for the specified key if it is currently associated with the given value.
     * 
     * <p>The method acquires a write lock on the bucket corresponding to the key to ensure a thread-safe removal.
     * If the key is mapped to the provided value, the mapping is removed and the method returns true;
     * otherwise, the map remains unchanged and the method returns false.</p>
     *
     * @param key the key whose mapping is to be conditionally removed
     * @param value the value expected to be associated with the key
     * @return true if the mapping existed and was removed, false otherwise
     */
    public boolean remove(long key, float value) {
        int bucket = getBucket(key);

        Lock writeLock = locks[bucket].writeLock();
        writeLock.lock();
        try {
            return maps[bucket].remove(key, value);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Atomically computes and returns the value associated with the given key if absent.
     *
     * <p>If the key is not already present in the map, the provided mapping function is applied
     * to compute a new value, which is then stored and returned. If the key is already mapped,
     * its existing value is returned.</p>
     *
     * @param key the key for which a value is to be computed if absent
     * @param mappingFunction the function to compute a value for the given key if absent
     * @return the current value associated with the key, either existing or computed
     */
    public float computeIfAbsent(long key, Long2FloatFunction mappingFunction) {
        int bucket = getBucket(key);

        Lock writeLock = locks[bucket].writeLock();
        writeLock.lock();
        try {
            return maps[bucket].computeIfAbsent(key, mappingFunction);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Computes a new value for the specified key if it is present in the map.
     *
     * <p>This method acquires a write lock on the bucket corresponding to the key, ensuring thread-safe updates.
     * If the key exists, the provided {@code mappingFunction} is applied to the current key-value pair. If the
     * function returns a non-null value, the map is updated with this new value; if it returns {@code null}, the key
     * is removed from the map.</p>
     *
     * @param key the key for which the value is to be computed if present
     * @param mappingFunction a function that takes the key and its current value and returns a new value (or {@code null}
     *                        to remove the entry)
     * @return the new value associated with the key, or the default value if the key is not present or has been removed
     */
    public float computeIfPresent(int key, BiFunction<Long, Float, Float> mappingFunction) {
        int bucket = getBucket(key);

        Lock writeLock = locks[bucket].writeLock();
        writeLock.lock();
        try {
            return maps[bucket].computeIfPresent(key, mappingFunction);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Creates a new builder for constructing instances of ConcurrentLongFloatMap.
     *
     * <p>The returned builder supports configuration of map parameters such as the number of buckets,
     * initial capacity, load factor, and default value. Depending on the configured map mode, the builder
     * constructs either a busy-waiting variant (ConcurrentBusyWaitingLongFloatMap) or a blocking variant
     * (ConcurrentLongFloatMap). If no default value is provided, it defaults to 0.</p>
     *
     * @return a builder for configuring and creating ConcurrentLongFloatMap instances
     */
    public static PrimitiveMapBuilder<ConcurrentLongFloatMap,Float> newBuilder () {
        return new PrimitiveMapBuilder<>(){
            @Override
            public ConcurrentLongFloatMap build () {
                float def = super.defaultValue != null ? super.defaultValue : 0;
                return switch (mapMode){
                    case BUSY_WAITING -> new ConcurrentBusyWaitingLongFloatMap(buckets, initialCapacity, loadFactor, def);
                    case BLOCKING -> new ConcurrentLongFloatMap(buckets, initialCapacity, loadFactor, def);
                };
            }
        };
    }
}