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

    /**
     * Initializes a new ConcurrentObjectLongMap with the specified parameters.
     *
     * <p>This constructor creates a concurrent map that divides its data into a set number of buckets,
     * each backed by an Object2LongOpenHashMap. The specified initial capacity and load factor configure
     * each bucket's underlying map, while the default value is returned for keys that are not present.</p>
     *
     * @param numBuckets    the number of buckets to partition the map into
     * @param initialCapacity the initial capacity for each bucket's map
     * @param loadFactor    the load factor for each bucket's map
     * @param defaultValue  the default value returned for keys that do not exist in the map
     */
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

    /**
     * Retrieves the bucket-specific map function at the given index.
     *
     * @param index the index of the bucket to access
     * @return the function mapping keys of type {@code K} to their corresponding long values in the bucket
     */
    @Override
    protected final Function<K,Long> mapAt (int index) {
        return maps[index];
    }

    /**
     * Checks if the map contains a mapping for the specified key.
     *
     * <p>This method acquires the read lock on the corresponding bucket to ensure thread-safe access when determining
     * whether the key is present in the map.</p>
     *
     * @param key the key whose presence is to be tested
     * @return {@code true} if a mapping for the specified key exists, {@code false} otherwise
     */
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

    /**
     * Retrieves the value associated with the specified key from the map.
     *
     * <p>If the key is not present, the default value is returned. This method ensures thread-safe access by acquiring
     * the read lock on the bucket corresponding to the key.
     *
     * @param key the key whose associated value is to be returned
     * @return the value associated with the key, or the default value if no mapping exists
     */
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

    /**
     * Associates the specified long value with the specified key in the map.
     *
     * <p>
     * Acquires a write lock on the bucket corresponding to the key to ensure thread safety and returns 
     * the previous value associated with the key, or the default value if no prior mapping existed.
     * </p>
     *
     * @param key the key to be associated with the given value
     * @param value the value to associate with the key
     * @return the previous value associated with the key, or the default value if there was no mapping
     */
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

    /**
 * Returns the default value used for keys that are not present in the map.
 *
 * This value is set during the construction of the map and is returned whenever a queried key does not exist.
 *
 * @return the default value
 */
public long getDefaultValue (){ return defaultValue; }

    /**
     * Removes the mapping for the specified key from this map.
     *
     * <p>This method acquires a write lock on the bucket corresponding to the key to ensure thread-safe removal.
     *
     * @param key the key whose mapping is to be removed
     * @return the value previously associated with the key, or the default value if the key was not present
     */
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

    /**
     * Removes the mapping for the specified key only if it is currently associated with the provided value.
     *
     * <p>This method acquires a write lock on the bucket containing the key to ensure thread safety.
     * If the current value mapped to the key matches the given value, the entry is removed and {@code true}
     * is returned; otherwise, the map remains unchanged and {@code false} is returned.
     *
     * @param key the key whose mapping is to be conditionally removed
     * @param value the expected value associated with the key
     * @return {@code true} if the key-value pair was successfully removed, otherwise {@code false}
     */
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

    /**
     * Computes a value for the specified key if it is not already associated with one.
     *
     * <p>This method acquires a write lock on the corresponding bucket to ensure thread safety. 
     * If no value exists for the key, the provided mapping function is used to compute and store a new value.
     * The current (existing or computed) value is then returned.</p>
     *
     * @param key the key for which a value should be computed if absent
     * @param mappingFunction the function that computes a value for the key if it is not present
     * @return the current value associated with the specified key, either preexisting or computed
     */
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

    /**
     * Computes a new value for the specified key if it is already present in the map.
     *
     * <p>If the key exists, the provided mapping function is applied to the key and its current value to compute a new value,
     * which then replaces the old value. This method acquires a write lock on the corresponding bucket to ensure thread safety.
     * If the key is not present, the mapping function is not invoked and the map remains unchanged.</p>
     *
     * @param key the key for which to compute a new value if present
     * @param mappingFunction a function that takes the key and its current value, and computes a new value
     * @return the updated value associated with the key after computation, or the default value if the key is not present
     */
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

    /**
     * Returns a new builder for configuring and constructing instances of {@link ConcurrentObjectLongMap}.
     *
     * <p>The returned builder supports different concurrency modes. When the map mode is set to
     * {@code BUSY_WAITING}, the builder produces an instance of {@link ConcurrentBusyWaitingObjectLongMap};
     * when set to {@code BLOCKING}, it produces a standard {@link ConcurrentObjectLongMap}.</p>
     *
     * @param <K> the type of keys maintained by the map
     * @return a new {@link PrimitiveMapBuilder} for building a {@link ConcurrentObjectLongMap}
     */
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