package com.trivago.fastutilconcurrentwrapper.longkey;

import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;

import java.util.concurrent.locks.Lock;
import java.util.function.BiFunction;

public class ConcurrentBusyWaitingLongObjectMap<V> extends ConcurrentLongObjectMap<V> {
    /**
     * Constructs a new ConcurrentBusyWaitingLongObjectMap with the specified number of buckets, initial capacity, load factor, and default value.
     *
     * @param numBuckets the number of buckets used for segmenting the map
     * @param initialCapacity the initial total capacity of the map
     * @param loadFactor the load factor threshold for resizing the map
     * @param defaultValue the default value returned when a key is not present
     */
    public ConcurrentBusyWaitingLongObjectMap (int numBuckets, int initialCapacity, float loadFactor, V defaultValue) {
        super(numBuckets, initialCapacity, loadFactor, defaultValue);
    }

    /**
     * Checks if the map contains a mapping for the specified key.
     *
     * <p>This method repeatedly attempts to acquire a read lock for the bucket corresponding 
     * to the specified key using busy waiting. Once the lock is acquired, it returns whether
     * the underlying bucket map contains the key.</p>
     *
     * @param key the key whose presence is to be tested
     * @return {@code true} if a mapping for the key exists; {@code false} otherwise
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

    /**
     * Retrieves the value associated with the specified key.
     * 
     * <p>This method returns the value mapped to the key if present; otherwise, it returns
     * the default value. It acquires the corresponding bucket's read lock using a busy waiting approach,
     * continuously yielding with {@code Thread.onSpinWait()} until the lock is obtained.
     *
     * @param key the key whose associated value is to be retrieved
     * @return the value associated with the key, or the default value if the key is not present
     */
    @Override
    public V get (long key) {
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
     * Inserts or updates the mapping for the specified key using busy waiting for write lock acquisition.
     *
     * <p>This method determines the corresponding bucket for the key and repeatedly attempts to acquire the
     * write lock. Once the lock is held, it delegates the put operation to the underlying bucket map and returns
     * the previous value associated with the key, if any.
     *
     * @param key the key for which the value is being stored
     * @param value the new value to associate with the key
     * @return the previous value associated with the key, or null if no mapping existed
     */
    @Override
    public V put (long key, V value) {
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
     * Removes the entry associated with the specified key from the map.
     *
     * <p>The method identifies the correct bucket for the key and continually attempts to acquire the bucket's
     * write lock using busy waiting. When the lock is acquired, it removes the key from the corresponding bucket's
     * underlying map.
     *
     * @param key the key whose mapping is to be removed
     * @return the previous value associated with the key, or {@code null} if no mapping existed
     */
    @Override
    public V remove (long key) {
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
     * Conditionally removes the mapping for the specified key if it is currently associated with the given value.
     * This method busy-waits to acquire a write lock on the bucket corresponding to the key before attempting removal.
     *
     * @param key the key whose mapping is to be removed
     * @param value the expected value associated with the key
     * @return true if the mapping was removed, false otherwise
     */
    @Override
    public boolean remove (long key, V value) {
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
     * Computes and stores a value for the specified key if it is not already present.
     *
     * <p>This method identifies the bucket corresponding to the key and employs a busy waiting loop to acquire its
     * write lock. Once the lock is acquired, if the key is absent, the provided mapping function is used to compute
     * and store the value, which is then returned.</p>
     *
     * @param key the key whose value is to be computed if absent
     * @param mappingFunction the function to compute a value when the key is not mapped
     * @return the current (existing or computed) value associated with the specified key
     */
    @Override
    public V computeIfAbsent (long key, Long2ObjectFunction<V> mappingFunction) {
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
     * Computes a new mapping for the specified key if it is already present.
     *
     * <p>This method continually attempts to acquire a write lock on the bucket corresponding to the key using a busy-wait loop.
     * Once the lock is acquired, if the key exists, it applies the provided mapping function to compute and update the value associated with the key.
     *
     * @param key the key whose associated value is to be computed
     * @param mappingFunction a function that takes the key and its current value and returns the new value
     * @return the new value associated with the specified key, or {@code null} if the key was not present
     */
    @Override
    public V computeIfPresent (long key, BiFunction<Long,V,V> mappingFunction) {
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