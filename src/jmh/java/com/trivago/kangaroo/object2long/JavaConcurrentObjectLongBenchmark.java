package com.trivago.kangaroo.object2long;

import com.trivago.kangaroo.AbstractCommonBenchHelper;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 3, time = 2)
public class JavaConcurrentObjectLongBenchmark extends AbstractCommonBenchHelper {

    Map<TestObjectKey, Long> map;

    /**
     * Populates the concurrent map with test data for benchmarking.
     *
     * <p>This method is executed once per trial setup. It initializes the map as a new
     * {@link java.util.concurrent.ConcurrentHashMap} with an initial capacity of
     * {@link AbstractObjectLongBenchHelper#NUM_VALUES} and a load factor of 0.8. The method then
     * iterates {@code NUM_VALUES} times, generating a new {@link TestObjectKey} and a random
     * long value for each entry, which is added to the map.
     */
    @Setup(Level.Trial)
    public void loadData() {
        map = new ConcurrentHashMap<>(AbstractObjectLongBenchHelper.NUM_VALUES, 0.8f);
        for (int i = 0; i < AbstractObjectLongBenchHelper.NUM_VALUES; i++) {
            TestObjectKey key = new TestObjectKey();
            long value = ThreadLocalRandom.current().nextLong();
            map.put(key, value);
        }
    }

    /**
     * Executes a get operation on the concurrent map for benchmarking purposes.
     *
     * <p>
     * This method retrieves a value from the map using a new instance of {@code TestObjectKey},
     * measuring the performance of read operations in a concurrent environment.
     * </p>
     */
    @Override
		public void testGet() {
        map.get(new TestObjectKey());
    }

    /**
     * Executes a put operation by inserting a new key-value pair into the concurrent map.
     *
     * <p>This method creates a new instance of TestObjectKey and associates it with a random long value,
     * simulating a put operation for benchmark testing.
     */
    @Override
		public void testPut() {
        TestObjectKey key = new TestObjectKey();
        long value = ThreadLocalRandom.current().nextLong();
        map.put(key, value);
    }

    /**
     * Executes a random operation on the concurrent map used for benchmarking.
     * 
     * <p>This method simulates a mixed workload by randomly choosing one of three actions:
     * <ul>
     *   <li><b>Insertion</b>: Inserts a new key with a random long value.</li>
     *   <li><b>Removal</b>: Removes a new key from the map.</li>
     *   <li><b>Retrieval</b>: Retrieves a value associated with a new key.</li>
     * </ul>
     * The operation is selected using a thread-local random generator.
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