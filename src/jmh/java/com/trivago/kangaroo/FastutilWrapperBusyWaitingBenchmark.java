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
public class FastutilWrapperBusyWaitingBenchmark extends AbstractBenchHelper {
    /**
     * Initializes and loads benchmark data using busy waiting mode.
     *
     * <p>This setup method is executed once per trial (as dictated by the @Setup(Level.Trial)
     * annotation) to prepare the data required for the benchmark.
     */
    @Setup(Level.Trial)
    public void loadData() {
        initAndLoadData(PrimitiveMapBuilder.MapMode.BUSY_WAITING);
    }
}