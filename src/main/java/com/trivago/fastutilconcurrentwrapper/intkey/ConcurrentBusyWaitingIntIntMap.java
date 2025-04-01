package com.trivago.fastutilconcurrentwrapper.intkey;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;

import java.util.concurrent.locks.Lock;
import java.util.function.BiFunction;

public class ConcurrentBusyWaitingIntIntMap extends ConcurrentIntIntMap {
    /**
     * Constructs a new ConcurrentBusyWaitingIntIntMap with the specified configuration parameters.
     *
     * <p>This implementation partitions its data into a fixed number of buckets and uses busy waiting to acquire
     * locks for thread-safe access. Initialization is delegated to the superclass, ConcurrentIntIntMap.
     *
     * @param numBuckets      the number of buckets used for partitioning the map
     * @param initialCapacity the initial capacity of the map
     * @param loadFactor      the load factor that influences the map's resizing threshold
     * @param defaultValue    the default value to return when a key is not found
     */
    public ConcurrentBusyWaitingIntIntMap (int numBuckets, int initialCapacity, float loadFactor, int defaultValue) {
        super(numBuckets, initialCapacity, loadFactor, defaultValue);
    }

    /**
     * Determines if the map contains the specified key.
     *
     * <p>This method calculates the bucket corresponding to the key and repeatedly attempts to acquire a
     * read lock using busy waiting. Once the lock is obtained, it verifies the presence of the key in the
     * associated bucket.</p>
     *
     * @param key the key whose presence in the map is to be tested
     * @return {@code true} if the key exists in the map; {@code false} otherwise
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

    @Override
    public int get(int key) {
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
     * Inserts or updates the value for the specified key.
     * <p>
     * This method acquires the write lock on the appropriate bucket using a busy-wait
     * mechanism. Once the lock is obtained, it updates the mapping for the given key by
     * delegating to the underlying map implementation.
     *
     * @param key the key whose value is to be inserted or updated
     * @param value the value to associate with the key
     * @return the previous value associated with the key, or a default value if no mapping existed
     */
    @Override
    public int put(int key, int value) {
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
     * Removes the mapping for the specified key from the map.
     * 
     * <p>This method repeatedly attempts to acquire the write lock for the bucket
     * corresponding to the given key using a busy waiting strategy. Once the lock is
     * obtained, it removes the mapping and returns the previously associated value.
     *
     * @param key the key whose mapping should be removed
     * @return the value that was associated with the key, or the default value if no mapping existed
     */
    @Override
    public int remove(int key) {
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

    @Override
    public boolean remove(int key, int value) {
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

    @Override
    public int computeIfAbsent(int key, Int2IntFunction mappingFunction) {
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

    @Override
    public int computeIfPresent(int key, BiFunction<Integer, Integer, Integer> mappingFunction) {
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