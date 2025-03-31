package com.trivago.fastutilconcurrentwrapper;

import com.trivago.fastutilconcurrentwrapper.map.ConcurrentBusyWaitingLongFloatMap;
import com.trivago.fastutilconcurrentwrapper.map.ConcurrentLongFloatMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ConcurrentLongFloatMapBuilderTest {
    private static final float DEFAULT_VALUE = -1f;

    @Test
    public void buildsBusyWaitingMap() {
        var b = LongFloatMap.newBuilder()
                .withBuckets(2)
                .withDefaultValue(DEFAULT_VALUE)
                .withInitialCapacity(100)
                .withMode(PrimitiveMapBuilder.MapMode.BUSY_WAITING)
                .withLoadFactor(0.8f);

        LongFloatMap map = b.build();

        map.put(1L, 10.1f);
        float v = map.get(1L);

			  assertInstanceOf(ConcurrentBusyWaitingLongFloatMap.class, map);
        assertEquals(10.1f, v);
        assertEquals(map.get(2L), map.getDefaultValue());
    }

    @Test
    public void buildsBlockingMap() {
        var b = LongFloatMap.newBuilder()
                .withBuckets(2)
                .withDefaultValue(DEFAULT_VALUE)
                .withInitialCapacity(100)
                .withMode(PrimitiveMapBuilder.MapMode.BLOCKING)
                .withLoadFactor(0.8f);

        LongFloatMap map = b.build();

        map.put(1L, 10.1f);
        float v = map.get(1L);

			  assertInstanceOf(ConcurrentLongFloatMap.class, map);
        assertEquals(10.1f, v);
        assertEquals(map.get(2L), map.getDefaultValue());
    }
}