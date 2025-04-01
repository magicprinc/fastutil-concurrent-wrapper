package com.trivago.kangaroo.long2long;

import com.trivago.kangaroo.AbstractCommonBenchHelper;
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

@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 3, time = 2)
public class JavaUtilWrapperLongLongBenchmark extends AbstractCommonBenchHelper {

    Map<Long, Long> map;

    /**
     * Initializes the map with random long key-value pairs for benchmarking.
     *
     * <p>This method creates a Long2LongOpenHashMap with an initial capacity defined by
     * AbstractLongLongBenchHelper.NUM_VALUES and a load factor of 0.8, populating it with
     * randomly generated key-value pairs using ThreadLocalRandom. The generated map is then wrapped
     * in a synchronized map to ensure thread safety during benchmark operations. This setup is
     * executed once per trial as indicated by the @Setup(Level.Trial) annotation.</p>
     */
    @Setup(Level.Trial)
    public void loadData() {
        Long2LongOpenHashMap m = new Long2LongOpenHashMap(AbstractLongLongBenchHelper.NUM_VALUES, 0.8f);
        for (int i = 0; i < AbstractLongLongBenchHelper.NUM_VALUES; i++) {
            long key = ThreadLocalRandom.current().nextLong();
            long value = ThreadLocalRandom.current().nextLong();
            m.put(key, value);
        }
        map = Collections.synchronizedMap(m);
    }

    /**
     * Benchmarks the retrieval of a value from the map using a randomly generated key.
     *
     * <p>This method generates a random key and retrieves the associated value from the map, helping to simulate
     * random access patterns during performance measurements.
     */
    @Override
		public void testGet() {
        long key = ThreadLocalRandom.current().nextLong();
        map.get(key);
    }

    /**
     * Inserts a random key-value pair into the map to benchmark the put operation.
     *
     * <p>This method generates random long values for both the key and the value using ThreadLocalRandom,
     * then adds the pair to the map.
     */
    @Override
		public void testPut() {
        long key = ThreadLocalRandom.current().nextLong();
        long value = ThreadLocalRandom.current().nextLong();
        map.put(key, value);
    }

    /**
     * Performs a random operation on the map.
     *
     * <p>This method randomly selects one of the following operations using a randomly generated key:
     * <ul>
     *   <li>If the random operation equals 1, inserts a new key-value pair with a randomly generated value.</li>
     *   <li>If the random operation equals 2, removes the entry associated with the key.</li>
     *   <li>Otherwise, retrieves the value associated with the key.</li>
     * </ul>
     *
     * <p>The method does not return any value.
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