package com.trivago.fastutilconcurrentwrapper.longkey;

import com.trivago.fastutilconcurrentwrapper.PrimitiveConcurrentMap;
import com.trivago.fastutilconcurrentwrapper.PrimitiveMapBuilder;
import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.concurrent.locks.Lock;
import java.util.function.BiFunction;

public class ConcurrentLongObjectMap<V> extends PrimitiveConcurrentMap<Long,V> {
    protected final Long2ObjectOpenHashMap<V>[] maps;
    protected final V defaultValue;

    /**
     * Constructs a new ConcurrentLongObjectMap with the specified bucket configuration and default value.
     *
     * <p>This constructor initializes the map by creating the specified number of internal buckets,
     * each as a Long2ObjectOpenHashMap configured with the given initial capacity and load factor.
     * The provided default value is returned when a key is not mapped to any value.</p>
     *
     * @param numBuckets the number of buckets used to segment the map for concurrent access
     * @param initialCapacity the initial capacity for each internal hash map bucket
     * @param loadFactor the load factor that determines the resizing threshold for each bucket
     * @param defaultValue the default value to return for keys that are not present in the map
     */
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

    /**
     * Returns the mapping function for the bucket at the specified index.
     *
     * <p>This method provides access to the internal map responsible for managing key-value pairs
     * for a given bucket within the concurrent map. The returned function serves as the bucket's
     * lookup mechanism for long keys.
     *
     * @param index the index of the bucket
     * @return the mapping function associated with the specified bucket index
     */
    @Override
    protected final Function<Long,V> mapAt (int index) {
        return maps[index];
    }

    /**
     * Checks whether the map contains the specified key.
     *
     * <p>This method performs a thread-safe lookup by acquiring a read lock on the bucket corresponding
     * to the provided key.
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
     * Retrieves the value associated with the specified key, or returns the default value if the key is not present.
     *
     * <p>This method provides thread-safe access by employing a read lock on the bucket corresponding to the key.
     *
     * @param key the key whose associated value is to be returned
     * @return the value mapped to the key, or the map's default value if the key is absent
     */
    public V get (long key) {
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
     * Inserts or updates the mapping for the specified key with the given value.
     *
     * <p>This operation is thread-safe, as it acquires the write lock on the relevant bucket
     * before updating the map.</p>
     *
     * @param key the key to insert or update
     * @param value the value to be associated with the key
     * @return the previous value associated with the key, or {@code null} if there was no mapping
     */
    public V put (long key, V value) {
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
 * Returns the default value assigned to keys that are not present in the map.
 *
 * @return the default value
 */
public V getDefaultValue (){ return defaultValue; }

    /**
     * Removes the mapping for the specified key from the map.
     * <p>
     * This operation is thread-safe, acquiring a write lock on the corresponding bucket.
     * </p>
     *
     * @param key the key whose mapping is to be removed
     * @return the previous value associated with the key, or {@code null} if there was no mapping
     */
    public V remove (long key) {
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
     * <p>This method acquires a write lock on the corresponding bucket to ensure thread safety, and it removes the entry
     * only when the current mapped value exactly matches the provided value.</p>
     *
     * @param key the key whose mapping is to be conditionally removed
     * @param value the value expected to be associated with the key; removal occurs only if this value matches the current mapping
     * @return {@code true} if the mapping was removed, or {@code false} if the key was absent or its value did not match
     */
    public boolean remove(long key, V value) {
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
     * Computes a value for the specified key if it is not already present.
     * <p>
     * If the key is absent, the provided mapping function is applied to compute a value,
     * which is then stored and returned. If the key already exists, its current value is returned.
     * This operation is performed under a write lock to ensure thread safety.
     *
     * @param key the key for which a value is to be computed if absent
     * @param mappingFunction the function to compute a value for the key
     * @return the current (existing or computed) value associated with the key
     */
    public V computeIfAbsent (long key, Long2ObjectFunction<V> mappingFunction) {
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
     * If a mapping for the specified key exists, computes a new mapping using the provided
     * function and updates the entry with the new value.
     *
     * <p>The operation is performed under a write lock for thread safety. If the mapping
     * function returns {@code null}, the mapping is removed.
     *
     * @param key the key for which the mapping is to be computed
     * @param mappingFunction a function that accepts the current key and value, and returns
     *        a new value (or {@code null} to remove the mapping)
     * @return the new value associated with the key, or {@code null} if the entry was removed
     */
    public V computeIfPresent (long key, BiFunction<Long,V,V> mappingFunction) {
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
     * Creates a new builder for constructing a concurrent long-to-object map.
     *
     * <p>This builder enables configuration of the map's parameters. When the {@code build()} method
     * is invoked, the builder creates an instance of either a busy waiting or a blocking
     * concurrent map implementation based on the configured map mode.</p>
     *
     * @param <V> the type of values maintained by the map
     * @return a builder for constructing a concurrent long-to-object map
     */
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