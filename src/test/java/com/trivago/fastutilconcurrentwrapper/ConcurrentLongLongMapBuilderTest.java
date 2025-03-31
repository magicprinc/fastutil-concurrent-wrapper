package com.trivago.fastutilconcurrentwrapper;

import com.trivago.fastutilconcurrentwrapper.map.ConcurrentBusyWaitingLongLongMap;
import com.trivago.fastutilconcurrentwrapper.map.ConcurrentLongLongMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ConcurrentLongLongMapBuilderTest {
    private static final long DEFAULT_VALUE = -1L;

    @Test
    public void simpleBuilderTest() {
        var b = LongLongMap.newBuilder()
                .withBuckets(2)
                .withDefaultValue(DEFAULT_VALUE)
                .withInitialCapacity(100)
                .withMode(PrimitiveMapBuilder.MapMode.BUSY_WAITING)
                .withLoadFactor(0.9f);

        LongLongMap map = b.build();

        map.put(1L, 10L);
        long v = map.get(1L);

			  assertInstanceOf(ConcurrentBusyWaitingLongLongMap.class, map);
        assertEquals(10L, v);
        assertEquals(map.get(2L), map.getDefaultValue());
    }

    @Test
    public void buildsBlockingMap() {
        var b = LongLongMap.newBuilder()
                .withBuckets(2)
                .withDefaultValue(DEFAULT_VALUE)
                .withInitialCapacity(100)
                .withMode(PrimitiveMapBuilder.MapMode.BLOCKING)
                .withLoadFactor(0.9f);

        LongLongMap map = b.build();

        map.put(1L, 10L);
        long v = map.get(1L);

			  assertInstanceOf(ConcurrentLongLongMap.class, map);
        assertEquals(10L, v);
        assertEquals(map.get(2L), map.getDefaultValue());
    }
}