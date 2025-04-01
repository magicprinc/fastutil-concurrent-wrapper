package com.trivago.fastutilconcurrentwrapper.longkey;

import it.unimi.dsi.fastutil.longs.Long2IntFunction;

import java.util.concurrent.locks.Lock;
import java.util.function.BiFunction;

public class ConcurrentBusyWaitingLongIntMap extends ConcurrentLongIntMap {
    /**
     * Constructs a new ConcurrentBusyWaitingLongIntMap with the specified configuration.
     * <p>
     * This map partitions its storage into the given number of buckets to facilitate concurrent access,
     * using busy waiting for lock acquisition. Each bucket is initialized with the specified capacity and load factor,
     * and the supplied default value is returned for absent keys.
     *
     * @param numBuckets      the number of partitions for concurrent access
     * @param initialCapacity the starting capacity for each bucket
     * @param loadFactor      the load factor threshold for resizing each bucket
     * @param defaultValue    the value to return when a key is not present in the map
     */
    public ConcurrentBusyWaitingLongIntMap (int numBuckets, int initialCapacity, float loadFactor, int defaultValue) {
        super(numBuckets, initialCapacity, loadFactor, defaultValue);
    }

    /**
     * Checks whether the map contains the specified key.
     *
     * <p>
     * This method calculates the appropriate bucket for the given key and repeatedly attempts to acquire a read lock
     * using busy waiting. Once the lock is acquired, it checks if the key exists in the bucket.
     * </p>
     *
     * @param key the key to search for in the map
     * @return {@code true} if the key is present, {@code false} otherwise
     */
    @Override
    public boolean containsKey(long key) {
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
    public int get(long key) {
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
     * Inserts or updates the value associated with the specified key.
     *
     * <p>This method repeatedly attempts to acquire a write lock on the bucket for the given key using busy waiting.
     * Once the lock is obtained, it updates the stored value for the key and returns the previous value associated with it,
     * or a default value if the key was not previously mapped.
     *
     * @param key   the key whose value is to be inserted or updated
     * @param value the new value to associate with the key
     * @return the previous value associated with the key, or the default if none existed
     */
    @Override
    public int put(long key, int value) {
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
     * <p>This method uses busy waiting to acquire the write lock associated with the bucket
     * containing the key. Once the appropriate lock is obtained, it removes the key-value
     * pair and returns the value that was previously associated with the key.
     *
     * @param key the long key whose mapping is to be removed
     * @return the value previously associated with the key, or the map's default value if not present
     */
    @Override
    public int remove(long key) {
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
    public boolean remove(long key, int value) {
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
    public int computeIfAbsent(long key, Long2IntFunction mappingFunction) {
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
    public int computeIfPresent(long key, BiFunction<Long, Integer, Integer> mappingFunction) {
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