package com.trivago.kangaroo.long2long;

import com.trivago.fastutilconcurrentwrapper.PrimitiveMapBuilder;
import com.trivago.fastutilconcurrentwrapper.longkey.ConcurrentLongLongMap;
import com.trivago.kangaroo.AbstractCommonBenchHelper;

import java.util.concurrent.ThreadLocalRandom;

public abstract class AbstractLongLongBenchHelper extends AbstractCommonBenchHelper {
    protected static final int NUM_VALUES = 1_000_000;
    protected ConcurrentLongLongMap map;

    /**
     * Initializes the concurrent map with a fixed configuration and populates it with random key-value pairs.
     *
     * <p>The map is built with 16 buckets, an initial capacity of {@code NUM_VALUES} (1,000,000), and a load factor of 0.8.
     * The provided map mode is used during construction. After initialization, the map is filled with 
     * {@code NUM_VALUES} randomly generated long key-value pairs.
     *
     * @param mode the map mode to be applied during the map's initialization
     */
    public void initAndLoadData (PrimitiveMapBuilder.MapMode mode) {
        map = ConcurrentLongLongMap.newBuilder()
                .withBuckets(16)
                .withInitialCapacity(NUM_VALUES)
                .withMode(mode)
                .withLoadFactor(0.8f)
                .build();

        for (int i = 0; i < NUM_VALUES; i++) {
            long key = ThreadLocalRandom.current().nextLong();
            long value = ThreadLocalRandom.current().nextLong();
            map.put(key, value);
        }
    }

    /**
     * Retrieves a value from the concurrent map using a randomly generated key.
     *
     * <p>This benchmark method generates a random long key via ThreadLocalRandom and uses it to perform a
     * get operation on the map, facilitating the measurement of concurrent retrieval performance.</p>
     */
    @Override
		public void testGet() {
        long key = ThreadLocalRandom.current().nextLong();
        map.get(key);
    }

    /**
     * Benchmarks the put operation on the concurrent map by inserting a randomly generated key-value pair.
     *
     * <p>This method uses thread-local random values for both the key and value to simulate a put operation
     * in a concurrent benchmarking scenario.</p>
     */
    @Override
		public void testPut() {
        long key = ThreadLocalRandom.current().nextLong();
        long value = ThreadLocalRandom.current().nextLong();
        map.put(key, value);
    }

    /**
     * Executes a random operation on the concurrent map.
     *
     * <p>This method randomly selects one of three operations:
     * <ul>
     *   <li>When the random value is 1, it inserts a new key-value pair into the map.</li>
     *   <li>When the random value is 2, it removes the entry associated with a random key.</li>
     *   <li>Otherwise, it retrieves the value of a random key.</li>
     * </ul>
     */
    @Override
		public void testAllOps() {
        int op = ThreadLocalRandom.current().nextInt(3);
        long key = ThreadLocalRandom.current().nextLong();
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