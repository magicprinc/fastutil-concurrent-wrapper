package com.trivago.kangaroo.long2long;

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
public class FastutilWrapperBusyWaitingLongLongBenchmark extends AbstractLongLongBenchHelper {
    /**
     * Initializes and loads data in busy-waiting mode for the benchmark trial.
     *
     * <p>This method is executed once before the benchmark trial begins and sets up the data by
     * invoking the superclass's data initialization routine with a busy-waiting strategy.
     */
    @Setup(Level.Trial)
    public void loadData() {
        initAndLoadData(PrimitiveMapBuilder.MapMode.BUSY_WAITING);
    }
}