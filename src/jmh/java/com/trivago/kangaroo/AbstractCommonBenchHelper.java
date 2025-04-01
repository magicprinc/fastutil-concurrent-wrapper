package com.trivago.kangaroo;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Threads;

import java.util.concurrent.TimeUnit;

public abstract class AbstractCommonBenchHelper {
    /**
     * Benchmarks the throughput of the "get" operation.
     *
     * <p>This method executes the testGet() operation using the throughput mode and measures its performance
     * with 4 concurrent threads.
     */
    @Threads(4)
    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void testRandomGetThroughput() {
        testGet();
    }

    @Threads(4)
    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void testRandomPutThroughput() {
        testPut();
    }

    @Threads(4)
    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void testRandomAllOpsThroughput() {
        testAllOps();
    }

    @Threads(4)
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void testRandomGetAvgTime() {
        testGet();
    }

    @Threads(4)
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void testRandomPutAvgTime() {
        testPut();
    }

    /**
     * Measures the average execution time of all operations.
     * <p>
     * Executes the {@code testAllOps()} method across four concurrent threads
     * and reports the average execution time in nanoseconds.
     * </p>
     */
    @Threads(4)
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void testRandomAllOpsAvgTime() {
        testAllOps();
    }

    /**
 * Executes the GET operation benchmark.
 *
 * <p>This abstract method should be implemented by subclasses to perform the GET operation,
 * which is used in benchmarking tests to measure performance metrics such as throughput and average execution time.</p>
 */
public abstract void testGet();

    /**
 * Executes a put operation for benchmarking purposes.
 *
 * <p>Implementations should override this method to define the logic for a put operation,
 * which will be measured by throughput and average execution time benchmarks.</p>
 */
public abstract void testPut();

    /**
 * Executes a comprehensive set of benchmark operations.
 *
 * <p>This abstract method should be implemented to perform a mixed workload—typically combining
 * get and put operations—to simulate a realistic full-spectrum scenario. It is invoked during
 * benchmarking runs to measure both throughput and average execution time in multi-threaded environments.
 */
public abstract void testAllOps();
}