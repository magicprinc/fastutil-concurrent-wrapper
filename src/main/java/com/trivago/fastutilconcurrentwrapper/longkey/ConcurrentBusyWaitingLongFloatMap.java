package com.trivago.fastutilconcurrentwrapper.longkey;

import it.unimi.dsi.fastutil.longs.Long2FloatFunction;

import java.util.concurrent.locks.Lock;
import java.util.function.BiFunction;

public class ConcurrentBusyWaitingLongFloatMap extends ConcurrentLongFloatMap {
    /**
     * Constructs a new ConcurrentBusyWaitingLongFloatMap with the specified configuration.
     *
     * <p>This constructor initializes a concurrent map for long keys and float values that employs busy waiting
     * for lock acquisition. The map is partitioned into the specified number of buckets, uses the given initial capacity
     * and load factor, and returns the provided default value when a key is not present.</p>
     *
     * @param numBuckets the number of buckets for partitioning the map
     * @param initialCapacity the initial capacity of the map
     * @param loadFactor the load factor that determines when the map should be resized
     * @param defaultValue the default float value returned if a key is absent
     */
    public ConcurrentBusyWaitingLongFloatMap (int numBuckets, int initialCapacity, float loadFactor, float defaultValue) {
        super(numBuckets, initialCapacity, loadFactor, defaultValue);
    }

    /**
     * Checks whether the map contains the specified key.
     *
     * <p>This method determines the appropriate bucket for the key and uses a busy-wait loop to acquire a read lock. Once the lock is acquired,
     * it queries the underlying map to check for the key's presence.
     *
     * @param key the key to check for presence in the map
     * @return {@code true} if the key exists in the map, otherwise {@code false}
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
    public float get(long key) {
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
     * Inserts or updates the mapping for the specified key using busy waiting until a write lock is acquired.
     *
     * <p>The method identifies the appropriate bucket based on the key, then repeatedly attempts to acquire the corresponding
     * write lock. Once the lock is obtained, it updates the value associated with the key and returns the previous value.
     *
     * @param key the key whose mapping is to be updated
     * @param value the new value to associate with the key
     * @return the previous value associated with the key, or the default value if no mapping existed
     */
    @Override
    public float put(long key, float value) {
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
     * Removes the entry for the specified key from the map.
     *
     * <p>This method determines the appropriate bucket for the key and repeatedly attempts
     * to acquire the corresponding write lock using busy waiting. Once the lock is acquired,
     * it removes the key and returns the value that was previously associated with it. If the
     * key is not present, the default value is returned.
     *
     * @param key the key whose entry is to be removed
     * @return the value associated with the removed key, or the default value if the key was not present
     */
    @Override
    public float remove(long key) {
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
    public boolean remove(long key, float value) {
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
    public float computeIfAbsent(long key, Long2FloatFunction mappingFunction) {
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
    public float computeIfPresent(int key, BiFunction<Long, Float, Float> mappingFunction) {
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