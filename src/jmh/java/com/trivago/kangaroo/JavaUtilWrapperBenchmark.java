package com.trivago.kangaroo;

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 JDK Collections.synchronizedMap(Long2LongOpenHashMap)
*/
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 3, time = 2)
public class JavaUtilWrapperBenchmark extends AbstractCommonBenchHelper {
    Map<Long, Long> map;

    /**
     * Initializes the benchmark map with random long key-value pairs.
     *
     * <p>This setup method creates a {@code Long2LongOpenHashMap} with a predefined capacity and a load factor of 0.8,
     * populates it with random long keys and values generated via {@code ThreadLocalRandom}, and then wraps the map with
     * {@code Collections.synchronizedMap} to ensure thread-safe access during benchmark trials.
     *
     * <p>The method is executed once per trial, as indicated by the {@code @Setup(Level.Trial)} annotation.
     */
    @Setup(Level.Trial)
    public void loadData() {
        Long2LongOpenHashMap m = new Long2LongOpenHashMap(AbstractBenchHelper.NUM_VALUES, 0.8f);
        for (int i = 0; i < AbstractBenchHelper.NUM_VALUES; i++) {
            long key = ThreadLocalRandom.current().nextLong();
            long value = ThreadLocalRandom.current().nextLong();
            m.put(key, value);
        }
        map = Collections.synchronizedMap(m);
    }

    /**
     * Benchmarks a map retrieval operation using a randomly generated key.
     *
     * <p>This method generates a random key via {@link java.util.concurrent.ThreadLocalRandom}
     * and retrieves the corresponding value from the synchronized map. It is designed to measure
     * the performance of get operations during benchmarking tests.</p>
     */
    @Override
		public void testGet() {
        long key = ThreadLocalRandom.current().nextLong();
        map.get(key);
    }

    /**
     * Inserts a new entry into the map using randomly generated key and value.
     *
     * <p>This method generates random long values for both the key and value with 
     * {@link ThreadLocalRandom} and inserts the entry into the synchronized map, 
     * serving as a benchmark for put operations.</p>
     */
    @Override
		public void testPut() {
        long key = ThreadLocalRandom.current().nextLong();
        long value = ThreadLocalRandom.current().nextLong();
        map.put(key, value);
    }

    /**
     * Executes a randomly selected map operation for benchmarking purposes.
     *
     * <p>This method randomly performs one of three actions on the map:
     * <ul>
     *   <li>Inserts a new key-value pair (put operation) when the random choice equals 1.</li>
     *   <li>Removes an entry (remove operation) when the random choice equals 2.</li>
     *   <li>Retrieves a value (get operation) for any other random choice.</li>
     * </ul>
     * This supports mixed operation workloads during benchmark tests.
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