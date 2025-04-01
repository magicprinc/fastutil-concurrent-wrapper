package com.trivago.fastutilconcurrentwrapper.longkey;

import com.trivago.fastutilconcurrentwrapper.PrimitiveConcurrentMap;
import com.trivago.fastutilconcurrentwrapper.PrimitiveMapBuilder;
import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.longs.Long2LongFunction;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;

import java.util.concurrent.locks.Lock;
import java.util.function.BiFunction;

public class ConcurrentLongLongMap extends PrimitiveConcurrentMap<Long,Long> {
    protected final Long2LongOpenHashMap[] maps;
    protected final long defaultValue;

    /**
     * Constructs a new ConcurrentLongLongMap with the specified number of buckets, initial capacity, load factor, and default value.
     *
     * <p>Each bucket is backed by a Long2LongOpenHashMap created with the provided initial capacity and load factor.
     * The default value is returned when a key is not present in the map.
     *
     * @param numBuckets the number of buckets used to partition the map
     * @param initialCapacity the initial capacity for each bucket's hash map
     * @param loadFactor the load factor for each bucket's hash map
     * @param defaultValue the value returned if a key is not found in the map
     */
    public ConcurrentLongLongMap(
        int numBuckets,
        int initialCapacity,
        float loadFactor,
        long defaultValue
    ){
        super(numBuckets);
        this.maps = new Long2LongOpenHashMap[numBuckets];
        this.defaultValue = defaultValue;
        for (int i = 0; i < numBuckets; i++)
            maps[i] = new Long2LongOpenHashMap(initialCapacity, loadFactor);
    }

    /**
     * Retrieves the map corresponding to the specified bucket index.
     *
     * @param index the index of the bucket to access
     * @return the map function representing the bucket at the given index
     */
    @Override
    protected final Function<Long,Long> mapAt (int index) {
        return maps[index];
    }

    /**
     * Checks if the map contains the specified key.
     *
     * <p>This method performs a thread-safe lookup by acquiring a read lock on the bucket corresponding to the given key.
     *
     * @param key the key to check
     * @return {@code true} if the key is present, {@code false} otherwise
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
     * Retrieves the value associated with the specified key.
     *
     * <p>This method determines the appropriate bucket for the given key, acquires a read lock for thread safety,
     * and returns the corresponding value. If the key is not present in the map, the default value is returned.
     *
     * @param key the key whose associated value is to be returned
     * @return the value associated with the specified key, or the default value if the key is absent
     */
    public long get (long key) {
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
     * Inserts or updates the mapping for the specified key with the given value in a thread-safe manner.
     *
     * <p>This method identifies the target bucket based on the key, acquires its write lock, and then performs the update.
     * It returns the previous value associated with the key, or the map's default value if no mapping existed.</p>
     *
     * @param key the key to be inserted or updated in the map
     * @param value the new value to associate with the key
     * @return the previous value associated with the key, or the default value if no prior mapping existed
     */
    public long put(long key, long value) {
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
 * Returns the default value used in the map when a key is not present.
 *
 * @return the default value associated with the map
 */
public long getDefaultValue (){ return defaultValue; }

    /**
     * Removes the mapping for the specified key from this map and returns the previously associated value.
     *
     * <p>This method acquires a write lock on the bucket corresponding to the key to ensure thread-safe removal.</p>
     *
     * @param key the key whose mapping is to be removed
     * @return the value previously associated with the key, or the map's default value if no mapping was found
     */
    public long remove(long key) {
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
     * Removes the entry for the specified key only if it is currently associated with the given value.
     *
     * <p>This method checks the mapping of the provided key and removes it if the associated value matches the specified value, ensuring thread-safe removal.</p>
     *
     * @param key the key whose mapping is to be conditionally removed
     * @param value the value expected to be associated with the key for successful removal
     * @return {@code true} if the mapping was removed, {@code false} otherwise
     */
    public boolean remove(long key, long value) {
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
     *
     * <p>This method retrieves the appropriate bucket for the given key and locks it for exclusive write access.
     * If the key is absent, it computes the value using the provided mapping function, stores the computed value,
     * and returns it. If the key is already mapped, the current value is returned.
     *
     * @param key the key for which the value is to be computed if absent
     * @param mappingFunction the function to compute a value for the key if absent
     * @return the existing or computed value associated with the key
     */
    public long computeIfAbsent(long key, Long2LongFunction mappingFunction) {
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
     * Atomically updates the value for the specified key if it is present.
     *
     * <p>If the key exists, the provided remapping function is applied to its current value to compute a new
     * value, which is then stored and returned. If the key is not present, the mapping function is not invoked
     * and the map’s default value is returned.
     *
     * @param key the key for which the value should be updated if present
     * @param mappingFunction the function that computes a new value given the key and its current value
     * @return the new value associated with the key if present, or the default value otherwise
     */
    public long computeIfPresent(long key, BiFunction<Long, Long, Long> mappingFunction) {
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
     * Creates a new builder for constructing {@link ConcurrentLongLongMap} instances.
     *
     * <p>The returned builder allows configuration of parameters such as bucket count,
     * initial capacity, load factor, and default value. Depending on the map mode set in the builder,
     * the {@code build()} method will instantiate either a {@link ConcurrentBusyWaitingLongLongMap}
     * (for busy waiting) or a {@link ConcurrentLongLongMap} (for blocking) with the specified configuration.
     *
     * @return a {@link PrimitiveMapBuilder} configured for building {@link ConcurrentLongLongMap} instances
     */
    public static PrimitiveMapBuilder<ConcurrentLongLongMap,Long> newBuilder () {
        return new PrimitiveMapBuilder<>(){
            @Override
            public ConcurrentLongLongMap build() {
                long def = super.defaultValue != null ? super.defaultValue : 0;
                return switch (mapMode){
                    case BUSY_WAITING -> new ConcurrentBusyWaitingLongLongMap(buckets, initialCapacity, loadFactor, def);
                    case BLOCKING -> new ConcurrentLongLongMap(buckets, initialCapacity, loadFactor, def);
                };
            }
        };
    }
}