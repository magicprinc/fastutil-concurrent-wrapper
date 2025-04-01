package com.trivago.kangaroo.object2long;

import com.trivago.fastutilconcurrentwrapper.PrimitiveMapBuilder;
import com.trivago.fastutilconcurrentwrapper.objkey.ConcurrentObjectLongMap;
import com.trivago.kangaroo.AbstractCommonBenchHelper;

import java.util.concurrent.ThreadLocalRandom;

public abstract class AbstractObjectLongBenchHelper extends AbstractCommonBenchHelper {
    protected static final int NUM_VALUES = 1_000_000;
    protected ConcurrentObjectLongMap<TestObjectKey> map;

    /**
     * Initializes the concurrent map and loads it with test data.
     *
     * <p>This method configures the map using a builder pattern with 16 buckets, an initial capacity of
     * NUM_VALUES, the specified map mode, and a load factor of 0.8. It then populates the map with NUM_VALUES
     * entries, where each entry consists of a new TestObjectKey instance and a randomly generated long value.
     *
     * @param mode the mode used to configure the map builder
     */
    public void initAndLoadData (PrimitiveMapBuilder.MapMode mode) {
        map = ConcurrentObjectLongMap.<TestObjectKey>newBuilder()
                .withBuckets(16)
                .withInitialCapacity(NUM_VALUES)
                .withMode(mode)
                .withLoadFactor(0.8f)
                .build();

        for (int i = 0; i < NUM_VALUES; i++) {
            TestObjectKey key = new TestObjectKey();
            long value = ThreadLocalRandom.current().nextLong();
            map.put(key, value);
        }
    }

    /**
     * Performs a benchmark retrieval operation on the concurrent map.
     *
     * <p>This method creates a new TestObjectKey and retrieves its associated value from the map,
     * allowing measurement of the map's get performance.
     */
    @Override
		public void testGet() {
        TestObjectKey key = new TestObjectKey();
        map.get(key);
    }

    /**
     * Inserts a new key-value pair into the concurrent map.
     * 
     * <p>This method creates a fresh key and associates it with a randomly generated long value,
     * simulating a put operation for benchmarking purposes.
     */
    @Override
		public void testPut() {
        TestObjectKey key = new TestObjectKey();
        long value = ThreadLocalRandom.current().nextLong();
        map.put(key, value);
    }

    /**
     * Executes a random operation on the concurrent map.
     *
     * <p>This method randomly selects one of three operations—insert (put), removal (remove), or retrieval (get)—
     * using a newly instantiated key for each call. When inserting, it generates a random long value to associate with the key.
     */
    @Override
		public void testAllOps() {
        int op = ThreadLocalRandom.current().nextInt(3);
        TestObjectKey key = new TestObjectKey();
        switch (op) {
            case 1:
                long value = ThreadLocalRandom.current().nextLong();
                map.put(key, value);
                break;
            case 2:
                map.remove(key);
                break;
            default:
                map.get(key);
                break;
        }
    }
}