package com.trivago.fastutilconcurrentwrapper;

import com.trivago.fastutilconcurrentwrapper.map.ConcurrentBusyWaitingIntIntMap;
import com.trivago.fastutilconcurrentwrapper.map.ConcurrentIntIntMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ConcurrentIntIntMapBuilderTest {
    private static final int DEFAULT_VALUE = -1;

    @Test
    public void buildsBusyWaitingMap() {
        var b = ConcurrentIntIntMap.newBuilder()
                .withBuckets(2)
                .withDefaultValue(DEFAULT_VALUE)
                .withInitialCapacity(100)
                .withMode(PrimitiveMapBuilder.MapMode.BUSY_WAITING)
                .withLoadFactor(0.8f);

        ConcurrentIntIntMap map = b.build();

        map.put(1, 10);
        int v = map.get(1);

			  assertInstanceOf(ConcurrentBusyWaitingIntIntMap.class, map);
        assertEquals(10, v);
        assertEquals(map.get(2), map.getDefaultValue());
    }

    @Test
    public void buildsBlockingMap() {
        var b = ConcurrentIntIntMap.newBuilder()
                .withBuckets(2)
                .withDefaultValue(DEFAULT_VALUE)
                .withInitialCapacity(100)
                .withMode(PrimitiveMapBuilder.MapMode.BLOCKING)
                .withLoadFactor(0.8f);

        ConcurrentIntIntMap map = b.build();

        map.put(1, 10);
        int v = map.get(1);

			  assertInstanceOf(ConcurrentIntIntMap.class, map);
			  assertSame(ConcurrentIntIntMap.class, map.getClass());
        assertEquals(10, v);
        assertEquals(map.get(2), map.getDefaultValue());
    }
}