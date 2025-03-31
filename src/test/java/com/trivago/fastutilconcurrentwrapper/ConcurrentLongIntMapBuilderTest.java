package com.trivago.fastutilconcurrentwrapper;

import com.trivago.fastutilconcurrentwrapper.longkey.ConcurrentBusyWaitingLongIntMap;
import com.trivago.fastutilconcurrentwrapper.longkey.ConcurrentLongIntMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ConcurrentLongIntMapBuilderTest {
    private static final int DEFAULT_VALUE = -1;

    @Test
    public void buildsBusyWaitingMap() {
        var b = ConcurrentLongIntMap.newBuilder()
                .withBuckets(2)
                .withDefaultValue(DEFAULT_VALUE)
                .withInitialCapacity(100)
                .withMode(PrimitiveMapBuilder.MapMode.BUSY_WAITING)
                .withLoadFactor(0.9f);

        ConcurrentLongIntMap map = b.build();

        map.put(1L, 10);
        int v = map.get(1L);

			  assertInstanceOf(ConcurrentBusyWaitingLongIntMap.class, map);
        assertEquals(10, v);
        assertEquals(map.get(2L), map.getDefaultValue());
    }

    @Test
    public void buildsBlockingMap() {
        var b = ConcurrentLongIntMap.newBuilder()
                .withBuckets(2)
                .withDefaultValue(DEFAULT_VALUE)
                .withInitialCapacity(100)
                .withMode(PrimitiveMapBuilder.MapMode.BLOCKING)
                .withLoadFactor(0.9f);

        ConcurrentLongIntMap map = b.build();

        map.put(1L, 10);
        long v = map.get(1L);

			  assertInstanceOf(ConcurrentLongIntMap.class, map);
        assertEquals(10, v);
        assertEquals(map.get(2L), map.getDefaultValue());
    }
}