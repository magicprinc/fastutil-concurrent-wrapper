package com.trivago.kangaroo;

import com.trivago.fastutilconcurrentwrapper.PrimitiveMapBuilder;
import com.trivago.fastutilconcurrentwrapper.longkey.ConcurrentLongLongMap;

import java.util.concurrent.ThreadLocalRandom;

public abstract class AbstractBenchHelper extends AbstractCommonBenchHelper {
    protected static final int NUM_VALUES = 1_000_000;
    protected ConcurrentLongLongMap map;

    /**
     * Initializes the concurrent map with a pre-populated set of random key-value pairs.
     *
     * <p>The map is configured with 16 buckets, an initial capacity defined by {@code NUM_VALUES},
     * and a load factor of 0.8. When the {@code mode} parameter is {@code BUSY_WAITING}, the map is built
     * with busy-waiting behavior; otherwise, a default configuration is used. After initialization,
     * the map is populated with {@code NUM_VALUES} entries, each assigned random long keys and values
     * generated via {@link ThreadLocalRandom}.</p>
     *
     * @param mode the map mode that determines whether busy-waiting is enabled
     */
    public void initAndLoadData(PrimitiveMapBuilder.MapMode mode) {
        if (mode == PrimitiveMapBuilder.MapMode.BUSY_WAITING){
            map = ConcurrentLongLongMap.newBuilder()
                    .withBuckets(16)
                    .withInitialCapacity(NUM_VALUES)
                    .withMode(PrimitiveMapBuilder.MapMode.BUSY_WAITING)
                    .withLoadFactor(0.8f)
                    .build();
        } else {
            map = ConcurrentLongLongMap.newBuilder()
                    .withBuckets(16)
                    .withInitialCapacity(NUM_VALUES)
                    .withLoadFactor(0.8f)
                    .build();
        }

        for (int i = 0; i < NUM_VALUES; i++) {
            long key = ThreadLocalRandom.current().nextLong();
            long value = ThreadLocalRandom.current().nextLong();
            map.put(key, value);
        }
    }

    @Override
		public void testGet() {
        long key = ThreadLocalRandom.current().nextLong();
        map.get(key);
    }

    @Override
		public void testPut() {
        long key = ThreadLocalRandom.current().nextLong();
        long value = ThreadLocalRandom.current().nextLong();
        map.put(key, value);
    }

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