package com.trivago.kangaroo.object2long;

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
public class FastutilWrapperBusyWaitingObjectLongBenchmark extends AbstractObjectLongBenchHelper {
    /**
     * Initializes and loads data for the benchmark using a busy waiting strategy.
     *
     * <p>This setup method is executed once per trial and prepares the benchmark state by invoking
     * the data initialization routine with a busy-waiting configuration.</p>
     */
    @Setup(Level.Trial)
    public void loadData() {
        initAndLoadData(PrimitiveMapBuilder.MapMode.BUSY_WAITING);
    }
}