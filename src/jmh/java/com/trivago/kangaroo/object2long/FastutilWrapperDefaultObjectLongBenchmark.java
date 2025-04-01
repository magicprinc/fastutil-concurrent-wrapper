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
public class FastutilWrapperDefaultObjectLongBenchmark extends AbstractObjectLongBenchHelper {
    /**
     * Loads and initializes data for the benchmark trial.
     *
     * <p>This method is executed once per trial (annotated with {@code @Setup(Level.Trial)}) and prepares
     * the benchmark environment by invoking {@code initAndLoadData} with {@code PrimitiveMapBuilder.MapMode.BLOCKING}.
     */
    @Setup(Level.Trial)
    public void loadData() {
        initAndLoadData(PrimitiveMapBuilder.MapMode.BLOCKING);
    }
}