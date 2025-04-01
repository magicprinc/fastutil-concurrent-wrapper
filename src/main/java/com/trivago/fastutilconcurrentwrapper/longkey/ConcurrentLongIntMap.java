package com.trivago.fastutilconcurrentwrapper.longkey;

import com.trivago.fastutilconcurrentwrapper.PrimitiveConcurrentMap;
import com.trivago.fastutilconcurrentwrapper.PrimitiveMapBuilder;
import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.longs.Long2IntFunction;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;

import java.util.concurrent.locks.Lock;
import java.util.function.BiFunction;

public class ConcurrentLongIntMap extends PrimitiveConcurrentMap<Long,Integer> {
    protected final Long2IntOpenHashMap[] maps;
    protected final int defaultValue;

    /**
     * Constructs a ConcurrentLongIntMap with the specified configuration.
     *
     * <p>This constructor partitions the map into a fixed number of buckets, where each bucket is a Long2IntOpenHashMap
     * initialized with the given capacity and load factor. The provided default value is used to represent absent keys.</p>
     *
     * @param numBuckets the number of buckets to partition the map for concurrent access
     * @param initialCapacity the initial capacity for each underlying hash map
     * @param loadFactor the load factor used to determine resizing thresholds for each bucket
     * @param defaultValue the default value returned when a key is not present in the map
     */
    public ConcurrentLongIntMap (
        int numBuckets,
        int initialCapacity,
        float loadFactor,
        int defaultValue
    ){
        super(numBuckets);
        this.maps = new Long2IntOpenHashMap[numBuckets];
        this.defaultValue = defaultValue;
        for (int i = 0; i < numBuckets; i++)
            maps[i] = new Long2IntOpenHashMap(initialCapacity, loadFactor);
    }

    /**
     * Returns the mapping function for the bucket at the specified index.
     *
     * <p>This method provides access to the underlying map for a given bucket, allowing retrieval
     * of integer values associated with long keys.</p>
     *
     * @param index the index of the bucket whose mapping function is to be returned
     * @return a function mapping long keys to their corresponding integer values in the specified bucket
     */
    @Override
    protected Function<Long,Integer> mapAt (int index) {
        return maps[index];
    }

    /**
     * Checks if the map contains the specified key.
     *
     * <p>This method determines the appropriate bucket for the given key, acquires a read lock to ensure thread safety,
     * and verifies if the key exists in that bucket.</p>
     *
     * @param key the key to be checked for presence in the map
     * @return {@code true} if the key exists in the map, {@code false} otherwise
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
     * Retrieves the value associated with the specified key in a thread-safe manner.
     * <p>
     * This method determines the appropriate bucket for the key, acquires a read lock on that bucket,
     * and then returns the corresponding integer value. If the key is not present in the map, the default value is returned.
     * </p>
     *
     * @param key the key whose associated value is to be retrieved
     * @return the integer value mapped to the key, or the default value if the key does not exist
     */
    public int get(long key) {
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
     * Inserts or updates the value for the specified key and returns the previous value.
     *
     * <p>This method acquires the write lock for the bucket corresponding to the key to ensure thread safety.
     * If the key was already present, the method returns the previous value; otherwise, it returns the default value.
     *
     * @param key the key for the value to be inserted or updated
     * @param value the value to be associated with the key
     * @return the previous value associated with the key, or the default value if the key was not present
     */
    public int put(long key, int value) {
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
 * Retrieves the default integer value used when a key is not found in the map.
 *
 * @return the default value configured for missing keys.
 */
public int getDefaultValue (){ return defaultValue; }

    /**
     * Removes the mapping for the specified key from the map.
     *
     * <p>This method determines the bucket corresponding to the given key, acquires an exclusive write lock on that bucket,
     * and removes the mapping if it exists. It returns the value that was associated with the key, or the default value if no mapping was found.</p>
     *
     * @param key the key whose mapping is to be removed
     * @return the previous value associated with the key, or the default value if the key was not present
     */
    public int remove(long key) {
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
     * Conditionally removes the mapping for a key if it is currently associated with the specified value.
     *
     * <p>This method acquires a write lock on the bucket corresponding to the key to ensure thread safety.
     * It only removes the entry if the current mapping matches the given value.</p>
     *
     * @param key the key whose mapping is to be removed
     * @param value the expected value associated with the key
     * @return {@code true} if the mapping was removed, {@code false} otherwise
     */
    public boolean remove(long key, int value) {
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
     * Atomically computes and associates a value for the specified key if it is absent.
     * <p>
     * If the key is not already mapped, this method applies the provided mapping function to
     * compute a new value, inserts it into the map, and returns the computed value. If the key
     * is already mapped, the existing value is returned.
     *
     * @param key the key for which the value is to be computed
     * @param mappingFunction the function used to compute a value from the key if absent
     * @return the current (existing or computed) value associated with the key
     */
    public int computeIfAbsent(long key, Long2IntFunction mappingFunction) {
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
     * Computes a new value for the specified key if it is currently mapped to a value.
     *
     * <p>This method determines the appropriate bucket for the given key and acquires its write lock
     * to ensure thread safety. It then applies the provided mapping function to the key and its existing
     * value. If the mapping function returns a non-null result, the key is updated with the new value;
     * if it returns {@code null}, the key is removed from the map.</p>
     *
     * @param key the key whose mapping is to be computed
     * @param mappingFunction a function that takes the key and its current value to compute a new value;
     *                        a {@code null} return value indicates removal of the key
     * @return the new value associated with the key, or the default value if the key was absent or removed
     */
    public int computeIfPresent(long key, BiFunction<Long, Integer, Integer> mappingFunction) {
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
     * Returns a new builder for constructing {@code ConcurrentLongIntMap} instances.
     *
     * <p>This builder facilitates configuration of parameters such as bucket count, initial capacity, load factor, and
     * default value. The {@code build()} method creates a new map instance based on the builder’s map mode: it returns a
     * {@code ConcurrentBusyWaitingLongIntMap} for busy waiting mode or a {@code ConcurrentLongIntMap} for blocking mode.
     * If no default value is provided, it defaults to 0.</p>
     *
     * @return a builder for instantiating {@code ConcurrentLongIntMap} instances
     */
    public static PrimitiveMapBuilder<ConcurrentLongIntMap,Integer> newBuilder () {
        return new PrimitiveMapBuilder<>(){
            @Override
            public ConcurrentLongIntMap build() {
                int def = super.defaultValue != null ? super.defaultValue : 0;
                return switch (mapMode){
                    case BUSY_WAITING -> new ConcurrentBusyWaitingLongIntMap(buckets, initialCapacity, loadFactor, def);
                    case BLOCKING -> new ConcurrentLongIntMap(buckets, initialCapacity, loadFactor, def);
                };
            }
        };
    }
}