package com.trivago.kangaroo;

import com.trivago.fastutilconcurrentwrapper.PrimitiveMapBuilder;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 3, time = 2)
public class FastutilWrapperDefaultBenchmark extends AbstractBenchHelper {
    /**
     * Initializes and loads the data required for the benchmark trial in blocking mode.
     *
     * <p>
     * This method is executed once before the benchmark trial starts and sets up the necessary data by
     * invoking the initialization routine with the blocking mode specified by {@code PrimitiveMapBuilder.MapMode.BLOCKING}.
     * </p>
     */
    @Setup(Level.Trial)
    public void loadData() {
        initAndLoadData(PrimitiveMapBuilder.MapMode.BLOCKING);
    }
}