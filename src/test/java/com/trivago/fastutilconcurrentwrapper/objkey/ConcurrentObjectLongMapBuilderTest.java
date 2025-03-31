package com.trivago.fastutilconcurrentwrapper.objkey;

import com.trivago.fastutilconcurrentwrapper.PrimitiveMapBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ConcurrentObjectLongMapBuilderTest {
    private static final long DEFAULT_VALUE = -1L;

    @Test
    public void simpleBuilderTest() {
        var b = ConcurrentObjectLongMap.<Short>newBuilder()
                .withBuckets(2)
                .withDefaultValue(DEFAULT_VALUE)
                .withInitialCapacity(100)
                .withMode(PrimitiveMapBuilder.MapMode.BUSY_WAITING)
                .withLoadFactor(0.9f);

        ConcurrentObjectLongMap<Short> map = b.build();

        map.put((short) 17, 10L);
        long v = map.get((short) 17);

        assertInstanceOf(ConcurrentBusyWaitingObjectLongMap.class, map);
        assertEquals(10L, v);
        assertEquals(map.get((short) 99), map.getDefaultValue());
    }

    @Test
    public void buildsBlockingMap() {
        var b = ConcurrentObjectLongMap.<Short>newBuilder()
                .withBuckets(2)
                .withDefaultValue(DEFAULT_VALUE)
                .withInitialCapacity(100)
                .withMode(PrimitiveMapBuilder.MapMode.BLOCKING)
                .withLoadFactor(0.9f);

        ConcurrentObjectLongMap<Short> map = b.build();

        map.put((short)41, 10L);
        long v = map.get((short)41);

        assertInstanceOf(ConcurrentObjectLongMap.class, map);
        assertEquals(10L, v);
        assertEquals(map.get((short)47), map.getDefaultValue());
    }
}