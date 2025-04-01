package com.trivago.fastutilconcurrentwrapper.intkey;

import com.trivago.fastutilconcurrentwrapper.PrimitiveConcurrentMap;
import com.trivago.fastutilconcurrentwrapper.PrimitiveMapBuilder;
import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

import java.util.concurrent.locks.Lock;
import java.util.function.BiFunction;

public class ConcurrentIntIntMap extends PrimitiveConcurrentMap<Integer,Integer> {
    protected final Int2IntOpenHashMap[] maps;
    protected final int defaultValue;

    /**
     * Constructs a new ConcurrentIntIntMap with the specified number of buckets, initial capacity, load factor, and default value.
     *
     * <p>This constructor creates an array of Int2IntOpenHashMap instances, one per bucket, where each map is initialized with
     * the given initial capacity and load factor. The provided default value is stored and used for any key that is not present.
     *
     * @param numBuckets      the number of buckets to divide the map into
     * @param initialCapacity the initial capacity of each bucket's map
     * @param loadFactor      the load factor for each bucket's map
     * @param defaultValue    the default value returned for missing keys
     */
    public ConcurrentIntIntMap(
        int numBuckets,
        int initialCapacity,
        float loadFactor,
        int defaultValue
    ){
        super(numBuckets);
        this.maps = new Int2IntOpenHashMap[numBuckets];
        this.defaultValue = defaultValue;
        for (int i = 0; i < numBuckets; i++)
            maps[i] = new Int2IntOpenHashMap(initialCapacity, loadFactor);
    }

    /**
     * Retrieves the mapping function for the bucket at the specified index.
     *
     * @param index the index of the bucket to retrieve
     * @return the mapping function corresponding to the bucket at the given index
     */
    @Override
    protected Function<Integer,Integer> mapAt (int index) {
        return maps[index];
    }

    /**
     * Checks if the map contains an entry for the specified key.
     *
     * <p>This thread-safe method uses a read lock on the appropriate bucket to verify the presence of the key in the map.</p>
     *
     * @param key the key to check in the map
     * @return {@code true} if the map contains an entry for the key, {@code false} otherwise
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
     * Retrieves the value associated with the specified key in a thread-safe manner.
     * <p>
     * This method acquires a read lock on the bucket corresponding to the provided key
     * and returns its value. If the key is not found, the default value is returned.
     * </p>
     *
     * @param key the key whose associated value is to be returned
     * @return the value mapped to the key, or the default value if no mapping exists
     */
    public int get (int key) {
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
     * <p>This method determines the bucket corresponding to the key, acquires the bucket's write lock,
     * and updates the underlying map with the new value. It returns the previous value associated with the key,
     * or the default value if no mapping was present.
     *
     * @param key the key to update
     * @param value the value to associate with the key
     * @return the previous value associated with the key, or the default value if the key was not present
     */
    public int put(int key, int value) {
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
 * @return the default value.
 */
public int getDefaultValue (){ return defaultValue; }

    /**
     * Removes the mapping for the specified key and returns the associated value.
     *
     * <p>This method identifies the corresponding bucket for the key, acquires a write lock on that bucket, 
     * and removes the key-value mapping. It returns the value that was previously associated with the key, 
     * or the map's default value if no mapping was present.</p>
     *
     * @param key the key whose mapping is to be removed
     * @return the value associated with the key before removal, or the default value if the key was not mapped
     */
    public int remove(int key) {
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
     * <p>
     * This method ensures thread-safe removal by acquiring a write lock on the bucket corresponding to the key.
     *
     * @param key the key whose mapping is to be conditionally removed
     * @param value the value expected to be associated with the key
     * @return {@code true} if the entry was removed, {@code false} otherwise
     */
    public boolean remove(int key, int value) {
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
     * Atomically computes and associates a value for the specified key if absent.
     * If no value is present for the key, the provided mapping function is applied to compute a new value,
     * which is then stored and returned. If a value already exists, it is simply returned.
     *
     * @param key the key for which the value is to be computed
     * @param mappingFunction the function to compute a value for the key if it is not already present
     * @return the existing or computed value associated with the specified key
     */
    public int computeIfAbsent(int key, Int2IntFunction mappingFunction) {
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
     * Atomically updates the value for the specified key if a mapping is present.
     *
     * <p>If the key is currently mapped to a value, this method applies the provided mapping function
     * to compute a new value and updates the mapping accordingly. If the key is not present,
     * the default value is returned.
     *
     * @param key the key whose associated value is to be updated
     * @param mappingFunction the function that computes a new value from the key and its current value
     * @return the updated value for the key if present; otherwise, the default value
     */
    public int computeIfPresent(int key, BiFunction<Integer, Integer, Integer> mappingFunction) {
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
     * Creates a builder for constructing instances of ConcurrentIntIntMap.
     *
     * <p>The returned builder allows configuration of map parameters such as the number of buckets, initial capacity,
     * load factor, and default value for missing keys. When the build method is invoked, the builder creates a map
     * instance based on the configured map mode: a ConcurrentBusyWaitingIntIntMap is returned for BUSY_WAITING mode,
     * and a ConcurrentIntIntMap for BLOCKING mode. If no default value is set, zero is used.</p>
     *
     * @return a builder for creating ConcurrentIntIntMap instances
     */
    public static PrimitiveMapBuilder<ConcurrentIntIntMap,Integer> newBuilder () {
        return new PrimitiveMapBuilder<>(){
            @Override
            public ConcurrentIntIntMap build () {
                int def = super.defaultValue != null ? super.defaultValue : 0;
                return switch (mapMode){
                    case BUSY_WAITING -> new ConcurrentBusyWaitingIntIntMap(buckets, initialCapacity, loadFactor, def);
                    case BLOCKING -> new ConcurrentIntIntMap(buckets, initialCapacity, loadFactor, def);
                };
            }
        };
    }
}