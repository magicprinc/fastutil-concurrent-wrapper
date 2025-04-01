package com.trivago.fastutilconcurrentwrapper.longkey;

import it.unimi.dsi.fastutil.longs.Long2LongFunction;

import java.util.concurrent.locks.Lock;
import java.util.function.BiFunction;

public class ConcurrentBusyWaitingLongLongMap extends ConcurrentLongLongMap {
    /**
     * Constructs a ConcurrentBusyWaitingLongLongMap with the specified configuration.
     *
     * <p>This constructor initializes the map by partitioning it into a fixed number of buckets to support concurrent
     * operations via busy-waiting locks. The initial capacity and load factor influence the map's performance and resizing behavior,
     * while the default value is returned for keys that are not present.</p>
     *
     * @param numBuckets the number of buckets used to partition the map
     * @param initialCapacity the initial capacity for each bucket
     * @param loadFactor the factor that determines when the map should be resized
     * @param defaultValue the default value returned for missing keys
     */
    public ConcurrentBusyWaitingLongLongMap (int numBuckets, int initialCapacity, float loadFactor, long defaultValue) {
        super(numBuckets, initialCapacity, loadFactor, defaultValue);
    }

    /**
     * Checks if the map contains the specified key.
     *
     * <p>This method busy-waits until a read lock on the corresponding bucket is acquired, then it verifies
     * the presence of the key in the underlying map.
     *
     * @param key the key to check for presence
     * @return {@code true} if the key exists in the map; {@code false} otherwise
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
    public long get(long key) {
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
     * Inserts or updates the mapping for the specified key with the given value.
     *
     * <p>This method employs a busy-waiting strategy to acquire the write lock on the bucket corresponding to the key.
     * Once the lock is obtained, it updates the underlying map and returns the previous value associated with the key,
     * or the default value if the key was not present.
     *
     * @param key   the key whose mapping is to be updated
     * @param value the value to associate with the key
     * @return the previous value associated with the key, or the default value if the key did not have a mapping
     */
    @Override
    public long put(long key, long value) {
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
     * Removes the mapping for the specified key.
     *
     * <p>This method repeatedly attempts to acquire the write lock for the bucket associated with the given key using a busy-waiting strategy. Once the lock is acquired, it removes and returns the value associated with the key from the underlying map.
     *
     * @param key the key whose mapping is to be removed
     * @return the value previously associated with the key, or the default value if the key was not present
     */
    @Override
    public long remove(long key) {
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
    public boolean remove(long key, long value) {
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
    public long computeIfAbsent(long key, Long2LongFunction mappingFunction) {
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
    public long computeIfPresent(long key, BiFunction<Long, Long, Long> mappingFunction) {
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