package com.trivago.fastutilconcurrentwrapper.intkey;

import com.trivago.fastutilconcurrentwrapper.PrimitiveConcurrentMap;
import com.trivago.fastutilconcurrentwrapper.PrimitiveMapBuilder;
import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.ints.Int2FloatFunction;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;

import java.util.concurrent.locks.Lock;
import java.util.function.BiFunction;

public class ConcurrentIntFloatMap extends PrimitiveConcurrentMap<Integer,Float> {
    protected final Int2FloatOpenHashMap[] maps;
    protected final float defaultValue;

    /**
     * Constructs a concurrent map that maps integer keys to float values using a segmented bucket approach.
     * <p>
     * The map is divided into multiple buckets to support concurrent access. Each bucket is initialized
     * as an instance of {@code Int2FloatOpenHashMap} with the specified initial capacity and load factor.
     * The specified default value is returned for any key that is not present in the map.
     * </p>
     *
     * @param numBuckets    the number of buckets used for concurrency segmentation
     * @param initialCapacity the initial capacity for each bucket
     * @param loadFactor    the load factor that determines when each bucket's capacity should be increased
     * @param defaultValue  the value returned for keys that do not exist in the map
     */
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

    /**
     * Retrieves the mapping function for the specified bucket index.
     *
     * <p>This method returns the internal mapping function (bucket) corresponding to the given index,
     * allowing for thread-safe operations on that segment of the map.
     *
     * @param index the index of the bucket to access
     * @return the mapping function for the specified bucket index
     */
    @Override
    protected Function<Integer,Float> mapAt (int index) {
        return maps[index];
    }

    /**
     * Checks if the map contains the specified key.
     *
     * <p>This method determines the appropriate bucket for the key, acquires a read lock on that bucket,
     * and then verifies if the key is present in the corresponding map.</p>
     *
     * @param key the key to check
     * @return true if the map contains a mapping for the specified key, false otherwise
     */
    public boolean containsKey(int key) {
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
     * Retrieves the value associated with the specified key.
     * <p>
     * If the key is not present in the map, the default value is returned.
     * </p>
     *
     * @param key the key whose associated value is to be returned
     * @return the float value associated with the key, or the default value if the key is absent
     */
    public float get(int key) {
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
     * Inserts or updates the value associated with the specified key.
     *
     * <p>If the key already has an associated value, that value is replaced and returned.
     * If the key is not present, the key is mapped to the given value and the default map value
     * is returned. This operation is thread-safe.</p>
     *
     * @param key the key whose mapping is to be inserted or updated
     * @param value the new float value to associate with the key
     * @return the previous float value associated with the key, or the default value if none existed
     */
    public float put(int key, float value) {
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
 * Returns the default float value used in the map for keys that have no associated value.
 *
 * @return the default float value
 */
public float getDefaultValue (){ return defaultValue; }

    /**
     * Removes the entry for the specified key in a thread-safe manner.
     * <p>
     * Returns the value previously associated with the key, or the default value if the key was not present.
     * </p>
     *
     * @param key the key of the entry to remove
     * @return the value previously associated with the specified key, or the default value if none existed
     */
    public float remove(int key) {
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
     * Removes the entry for the specified key only if it is currently mapped to the given value.
     *
     * @param key the key to remove
     * @param value the value expected to be associated with the key
     * @return {@code true} if the mapping was removed, or {@code false} if the key was not mapped to the specified value
     */
    public boolean remove(int key, float value) {
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
     * Computes and associates a value with the specified key if it is not already present.
     * This method acquires a write lock on the relevant bucket to ensure thread safety, then either
     * returns the existing value or computes a new one using the provided mapping function, stores it,
     * and returns it.
     *
     * @param key the key for which the value should be computed if absent
     * @param mappingFunction the function to compute a value when the key is absent
     * @return the current value associated with the key, whether already present or newly computed
     */
    public float computeIfAbsent(int key, Int2FloatFunction mappingFunction) {
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
     * Computes a new value for the given key if it is already present in the map.
     *
     * <p>If the key exists, the supplied mapping function is applied to the key and its current value,
     * and the mapping is updated with the computed result in a thread-safe manner. If the key is not
     * present, the mapping function is not invoked and the map remains unchanged.</p>
     *
     * @param key the key whose value is to be computed and potentially updated
     * @param mappingFunction a function that takes the key and its current value to compute a new value
     * @return the new value associated with the key, or the default value if the key was not present
     */
    public float computeIfPresent(int key, BiFunction<Integer, Float, Float> mappingFunction) {
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
     * Returns a new builder for configuring and constructing {@code ConcurrentIntFloatMap} instances.
     *
     * <p>The returned builder lets you define map properties such as the number of buckets, initial capacity,
     * load factor, and default value for absent keys. When the builder's {@code build()} method is called, it
     * selects the appropriate implementation—either a busy-waiting or blocking version—based on the current map mode.
     * If no default value is specified, 0 is used.</p>
     *
     * @return a new {@code PrimitiveMapBuilder} for creating {@code ConcurrentIntFloatMap} instances with the desired configuration
     */
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