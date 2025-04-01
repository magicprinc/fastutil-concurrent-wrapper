package com.trivago.fastutilconcurrentwrapper.objkey;

import it.unimi.dsi.fastutil.objects.Object2LongFunction;

import java.util.concurrent.locks.Lock;
import java.util.function.BiFunction;

public class ConcurrentBusyWaitingObjectLongMap<K> extends ConcurrentObjectLongMap<K> {
    /**
     * Constructs a new ConcurrentBusyWaitingObjectLongMap with the specified configuration.
     *
     * <p>This constructor initializes the map with a fixed number of buckets for concurrent access,
     * an initial capacity, a load factor to determine resizing thresholds, and a default value to return
     * when a key is not present.
     *
     * @param numBuckets     the number of buckets used for partitioning the map
     * @param initialCapacity the initial capacity of the map
     * @param loadFactor     the load factor threshold for resizing the map
     * @param defaultValue   the default value to return for absent keys
     */
    public ConcurrentBusyWaitingObjectLongMap (int numBuckets, int initialCapacity, float loadFactor, long defaultValue) {
        super(numBuckets, initialCapacity, loadFactor, defaultValue);
    }

    /**
     * Checks if the map contains the specified key.
     * 
     * <p>This method identifies the bucket corresponding to the given key and continuously
     * attempts to acquire its read lock using busy waiting. Once the lock is acquired, it
     * checks whether the underlying map for that bucket contains the key.
     *
     * @param key the key to check for presence in the map
     * @return {@code true} if the key is present, {@code false} otherwise
     */
    @Override
    public boolean containsKey (K key) {
        int bucket = getBucket(key);

        Lock readLock = locks[bucket].readLock();

        while (true) {
            if (readLock.tryLock()) {
                try {
                    return maps[bucket].containsKey(key);
                } finally {
                    readLock.unlock();
                }
            }
            Thread.onSpinWait();
        }
    }

    /**
     * Retrieves the value associated with the specified key.
     *
     * <p>
     * This method attempts to acquire a read lock on the bucket corresponding to the key using busy waiting.
     * If the key is not found, the default value is returned.
     * </p>
     *
     * @param key the key whose associated value is to be returned
     * @return the value associated with the key, or the default value if the key is not present
     */
    @Override
    public long get (K key) {
        int bucket = getBucket(key);

        Lock readLock = locks[bucket].readLock();

        while (true) {
            if (readLock.tryLock()) {
                try {
                    return maps[bucket].getOrDefault(key, defaultValue);
                } finally {
                    readLock.unlock();
                }
            }
            Thread.onSpinWait();
        }
    }

    /**
     * Inserts or updates the specified key with the given value.
     *
     * <p>This method uses busy waiting to acquire a write lock on the bucket corresponding to the key,
     * ensuring thread-safe update operations. Once the lock is obtained, the value for the key is updated,
     * and the previous value (or the default value if no mapping existed) is returned.
     *
     * @param key the key to insert or update
     * @param value the value to associate with the key
     * @return the previous value associated with the key, or the default value if none was present
     */
    @Override
    public long put (K key, long value) {
        int bucket = getBucket(key);

        Lock writeLock = locks[bucket].writeLock();

        while (true) {
            if (writeLock.tryLock()) {
                try {
                    return maps[bucket].put(key, value);
                } finally {
                    writeLock.unlock();
                }
            }
            Thread.onSpinWait();
        }
    }

    /**
     * Removes the entry for the specified key from the map and returns the associated value.
     *
     * <p>This method repeatedly attempts to acquire the write lock for the bucket corresponding
     * to the given key using busy waiting. Once the lock is acquired, it removes the key's entry
     * from the underlying map.
     *
     * @param key the key for which the mapping is to be removed
     * @return the value previously associated with the specified key, or the default value if no mapping existed
     */
    @Override
    public long remove (K key) {
        int bucket = getBucket(key);

        Lock writeLock = locks[bucket].writeLock();

        while (true) {
            if (writeLock.tryLock()) {
                try {
                    return maps[bucket].removeLong(key);
                } finally {
                    writeLock.unlock();
                }
            }
            Thread.onSpinWait();
        }
    }

    /**
     * Removes the mapping for the specified key only if it is currently associated with the given value.
     *
     * <p>This method busy-waits to acquire a write lock on the bucket corresponding to the key.
     * Once the lock is obtained, it conditionally removes the entry from the underlying map.
     * </p>
     *
     * @param key the key whose mapping is to be considered for removal
     * @param value the expected value associated with the key for removal to occur
     * @return {@code true} if the mapping was successfully removed, {@code false} otherwise
     */
    @Override
    public boolean remove (K key, long value) {
        int bucket = getBucket(key);

        Lock writeLock = locks[bucket].writeLock();

        while (true) {
            if (writeLock.tryLock()) {
                try {
                    return maps[bucket].remove(key, value);
                } finally {
                    writeLock.unlock();
                }
            }
            Thread.onSpinWait();
        }
    }

    /**
     * Computes and returns the value associated with the specified key, computing it with the provided mapping function if absent.
     * <p>
     * This method busy-waits until a write lock for the key’s bucket is acquired. If the key is not present, the mapping function is
     * applied to compute its value, which is then stored and returned.
     *
     * @param key the key for which the value is to be computed if absent
     * @param mappingFunction the function to compute the value if the key is not already associated with one
     * @return the existing or newly computed value associated with the key
     */
    @Override
    public long computeIfAbsent (K key, Object2LongFunction<K> mappingFunction) {
        int bucket = getBucket(key);

        Lock writeLock = locks[bucket].writeLock();

        while (true) {
            if (writeLock.tryLock()) {
                try {
                    return maps[bucket].computeIfAbsent(key, mappingFunction);
                } finally {
                    writeLock.unlock();
                }
            }
            Thread.onSpinWait();
        }
    }

    /**
     * Computes and updates the value for the specified key if it is present.
     *
     * <p>
     * This method busy-waits to acquire a write lock on the bucket associated with the key to ensure thread-safe updates.
     * If the key is present, the provided mapping function is applied to compute a new value, which then replaces the existing value.
     * If the key is not present, the map returns its default value.
     * </p>
     *
     * @param key the key to update, if present in the map
     * @param mappingFunction a function that computes a new value from the key and its current value
     * @return the new value associated with the key if it was present; otherwise, the default value
     */
    @Override
    public long computeIfPresent (K key, BiFunction<K,Long,Long> mappingFunction) {
        int bucket = getBucket(key);

        Lock writeLock = locks[bucket].writeLock();

        while (true) {
            if (writeLock.tryLock()) {
                try {
                    return maps[bucket].computeIfPresent(key, mappingFunction);
                } finally {
                    writeLock.unlock();
                }
            }
            Thread.onSpinWait();
        }
    }
}