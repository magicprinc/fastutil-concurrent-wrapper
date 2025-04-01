package com.trivago.kangaroo.object2long;

import com.trivago.kangaroo.AbstractCommonBenchHelper;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 3, time = 2)
public class JavaUtilWrapperObjectLongBenchmark extends AbstractCommonBenchHelper {

    Map<TestObjectKey, Long> map;

    /**
     * Initializes the benchmark map with random key-value pairs.
     *
     * <p>This method creates an {@code Object2LongOpenHashMap} with a predefined capacity and load factor,
     * populates it with random long values using new {@code TestObjectKey} instances, and wraps the map
     * in a synchronized wrapper. It is executed once per trial as part of the benchmark setup.</p>
     */
    @Setup(Level.Trial)
    public void loadData() {
        Object2LongOpenHashMap<TestObjectKey> m = new Object2LongOpenHashMap<>(AbstractObjectLongBenchHelper.NUM_VALUES, 0.8f);
        for (int i = 0; i < AbstractObjectLongBenchHelper.NUM_VALUES; i++) {
            TestObjectKey key = new TestObjectKey();
            long value = ThreadLocalRandom.current().nextLong();
            m.put(key, value);
        }
        map = Collections.synchronizedMap(m);
    }

    /**
     * Executes a benchmark test for the get operation on the map.
     *
     * <p>This method retrieves a value using a newly instantiated TestObjectKey to measure the performance of 
     * map lookup operations as part of the benchmark.
     */
    @Override
		public void testGet() {
        map.get(new TestObjectKey());
    }

    /**
     * Inserts a new key-value pair into the map used for benchmarking.
     *
     * <p>This method creates a new instance of TestObjectKey and generates a random long value,
     * then adds the pair to the map.
     */
    @Override
		public void testPut() {
        TestObjectKey key = new TestObjectKey();
        long value = ThreadLocalRandom.current().nextLong();
        map.put(key, value);
    }

    /**
     * Executes one of the map operations (put, remove, or get) at random.
     *
     * <p>This method randomly selects one of three operations to perform on the map:
     * <ul>
     *   <li><b>Put</b> - Inserts a new {@code TestObjectKey} with a randomly generated long value.
     *   <li><b>Remove</b> - Removes an entry associated with a new {@code TestObjectKey}.
     *   <li><b>Get</b> - Retrieves the value for a new {@code TestObjectKey}.
     * </ul>
     * Each operation uses a freshly created instance of {@code TestObjectKey}. Note that no operation result is returned.
     * </p>
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