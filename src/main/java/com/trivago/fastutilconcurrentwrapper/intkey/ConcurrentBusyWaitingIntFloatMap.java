package com.trivago.fastutilconcurrentwrapper.intkey;

import it.unimi.dsi.fastutil.ints.Int2FloatFunction;

import java.util.concurrent.locks.Lock;
import java.util.function.BiFunction;

public class ConcurrentBusyWaitingIntFloatMap extends ConcurrentIntFloatMap {
    /**
     * Constructs a new ConcurrentBusyWaitingIntFloatMap with the specified configuration.
     *
     * This map employs busy waiting for lock acquisition and delegates initialization to its superclass.
     * It partitions data into a specified number of buckets, defines an initial capacity and load factor,
     * and uses a default value for missing keys.
     *
     * @param numBuckets    the number of buckets used for partitioning the map for concurrent access
     * @param initialCapacity the initial capacity of the map
     * @param loadFactor    the load factor that triggers resizing when exceeded
     * @param defaultValue  the default value returned when a key is not present in the map
     */
    public ConcurrentBusyWaitingIntFloatMap (int numBuckets, int initialCapacity, float loadFactor, float defaultValue) {
        super(numBuckets, initialCapacity, loadFactor, defaultValue);
    }

    /**
     * Checks if the map contains the specified key.
     *
     * <p>This method continuously attempts to acquire a read lock on the bucket associated
     * with the key using busy waiting. Once the lock is obtained, it checks whether the key
     * exists in the corresponding bucket.</p>
     *
     * @param key the key to check in the map
     * @return {@code true} if the key is present, {@code false} otherwise
     */
    @Override
		public boolean containsKey(int key) {
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
     * Retrieves the value associated with the specified key from the map.
     *
     * <p>This method continuously attempts to acquire the read lock for the bucket corresponding to the key via busy waiting.
     * It returns the value mapped to the key if present, or the default value otherwise.
     *
     * @param key the key whose associated value is to be returned
     * @return the value associated with the key, or the default value if the key does not exist
     */
    @Override
		public float get (int key) {
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
     * Inserts or updates the value for the specified key in the map.
     *
     * <p>This method acquires a write lock for the appropriate bucket using busy waiting. Once the lock is acquired, it updates
     * the mapping for the key and returns the previous value associated with the key, or the default value if no mapping existed.
     *
     * @param key   the key for which the value is to be inserted or updated
     * @param value the value to associate with the specified key
     * @return      the previous value associated with the key, or the default value if there was no mapping
     */
    @Override
		public float put(int key, float value) {
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
     * Removes the mapping for the specified key and returns its associated value.
     *
     * This method busy-waits to acquire the write lock for the bucket determined by the key, then
     * removes the key from that bucket. If the key is present, its associated value is returned;
     * otherwise, the map's default value is returned.
     *
     * @param key the key whose mapping is to be removed
     * @return the value previously associated with the key, or the default value if no mapping existed
     */
    @Override
		public float remove(int key) {
        int bucket = getBucket(key);

        Lock writeLock = locks[bucket].writeLock();

        while (true) {
            if (writeLock.tryLock()) {
                try {
                    return maps[bucket].remove(key);
                } finally {
                    writeLock.unlock();
                }
            }
            Thread.onSpinWait();
        }
    }

    /**
     * Removes the mapping for the specified key if it is currently associated with the given value.
     * <p>
     * This method repeatedly attempts to acquire the write lock for the key's bucket using busy waiting.
     * If the current value associated with the key equals the specified value, the mapping is removed.
     * </p>
     *
     * @param key the key whose mapping is to be conditionally removed
     * @param value the expected value associated with the key for removal to occur
     * @return {@code true} if the mapping was present and removed, {@code false} otherwise
     */
    @Override
		public boolean remove(int key, float value) {
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
     * Computes and returns the value associated with the specified key if it is not already present.
     *
     * <p>
     * This method busy-waits to acquire a write lock for the bucket corresponding to the key. Once the lock is obtained,
     * it either returns the existing value or computes and stores a new value using the provided mapping function.
     * </p>
     *
     * @param key the key for which the value should be computed if absent
     * @param mappingFunction the function to compute a new value if the key is not already present
     * @return the current value associated with the key, either existing or newly computed
     */
    @Override
		public float computeIfAbsent(int key, Int2FloatFunction mappingFunction) {
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
    	 * Computes a new value for the specified key if it is already present.
    	 *
    	 * <p>This method repeatedly attempts to acquire the write lock for the bucket corresponding to the key
    	 * using busy waiting. Once the lock is obtained, it applies the provided mapping function to the key and its
    	 * current value to compute an updated value.
    	 *
    	 * @param key the key whose associated value is to be updated if present
    	 * @param mappingFunction a function that computes a new value from the key and its current value
    	 * @return the updated value associated with the key, or the current value if the mapping function does not alter it
    	 */
    @Override
		public float computeIfPresent(int key, BiFunction<Integer, Float, Float> mappingFunction) {
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