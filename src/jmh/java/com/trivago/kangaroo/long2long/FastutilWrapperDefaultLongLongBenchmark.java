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
public class FastutilWrapperDefaultLongLongBenchmark extends AbstractLongLongBenchHelper {
    /**
     * Initializes and loads data for the benchmark trial.
     * 
     * <p>This setup method is executed once before the benchmark trial begins. It initializes
     * the necessary data structures in blocking mode by invoking {@code initAndLoadData} with
     * {@code PrimitiveMapBuilder.MapMode.BLOCKING}.</p>
     */
    @Setup(Level.Trial)
    public void loadData() {
        initAndLoadData(PrimitiveMapBuilder.MapMode.BLOCKING);
    }
}