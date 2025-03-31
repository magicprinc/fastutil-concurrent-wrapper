package com.trivago.fastutilconcurrentwrapper;

import com.trivago.fastutilconcurrentwrapper.map.ConcurrentBusyWaitingIntFloatMap;
import com.trivago.fastutilconcurrentwrapper.map.ConcurrentIntFloatMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ConcurrentIntFloatMapBuilderTest {
    private static final float DEFAULT_VALUE = -1f;

    @Test
    public void buildsBusyWaitingMap() {
        var b = ConcurrentIntFloatMap.newBuilder()
                .withBuckets(2)
                .withDefaultValue(DEFAULT_VALUE)
                .withInitialCapacity(100)
                .withMode(PrimitiveMapBuilder.MapMode.BUSY_WAITING)
                .withLoadFactor(0.8f);

        ConcurrentIntFloatMap map = b.build();

        map.put(1, 10.1f);
        float v = map.get(1);

			  assertInstanceOf(ConcurrentBusyWaitingIntFloatMap.class, map);
        assertEquals(10.1f, v);
        assertEquals(map.get(2), map.getDefaultValue());
    }

    @Test
    public void buildsBlockingMap() {
        var b = ConcurrentIntFloatMap.newBuilder()
                .withBuckets(2)
                .withDefaultValue(DEFAULT_VALUE)
                .withInitialCapacity(100)
                .withMode(PrimitiveMapBuilder.MapMode.BLOCKING)
                .withLoadFactor(0.8f);

        ConcurrentIntFloatMap map = b.build();

        map.put(1, 10.1f);
        float v = map.get(1);

			  assertInstanceOf(ConcurrentIntFloatMap.class, map);
			  assertSame(ConcurrentIntFloatMap.class, map.getClass());
        assertEquals(10.1f, v);
        assertEquals(map.get(2), map.getDefaultValue());
    }
}