package com.trivago.kangaroo.object2long;

import com.trivago.fastutilconcurrentwrapper.PrimitiveMapBuilder;
import com.trivago.fastutilconcurrentwrapper.objkey.ConcurrentObjectLongMap;
import com.trivago.kangaroo.AbstractCommonBenchHelper;

import java.util.concurrent.ThreadLocalRandom;

public abstract class AbstractObjectLongBenchHelper extends AbstractCommonBenchHelper {
    protected static final int NUM_VALUES = 1_000_000;
    protected ConcurrentObjectLongMap<TestObjectKey> map;

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

    @Override
		public void testGet() {
        TestObjectKey key = new TestObjectKey();
        map.get(key);
    }

    @Override
		public void testPut() {
        TestObjectKey key = new TestObjectKey();
        long value = ThreadLocalRandom.current().nextLong();
        map.put(key, value);
    }

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