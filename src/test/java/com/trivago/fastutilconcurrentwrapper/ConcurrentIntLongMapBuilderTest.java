package com.trivago.fastutilconcurrentwrapper;

import com.trivago.fastutilconcurrentwrapper.intkey.ConcurrentBusyWaitingIntLongMap;
import com.trivago.fastutilconcurrentwrapper.intkey.ConcurrentIntLongMap;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ConcurrentIntLongMapBuilderTest {
	private static final long DEFAULT_VALUE = -1L;

	@Test
	public void simpleBuilderTest() {
		val b = ConcurrentIntLongMap.newBuilder()
				.withBuckets(2)
				.withDefaultValue(DEFAULT_VALUE)
				.withInitialCapacity(100)
				.withMode(PrimitiveMapBuilder.MapMode.BUSY_WAITING)
				.withLoadFactor(0.9f);

		val map = (ConcurrentBusyWaitingIntLongMap) b.build();

		map.put(1, 10L);
		long v = map.get(1);

		assertInstanceOf(ConcurrentBusyWaitingIntLongMap.class, map);
		assertEquals(10L, v);
		assertEquals(map.get(2), map.getDefaultValue());
	}

	@Test
	public void buildsBlockingMap() {
		val b = ConcurrentIntLongMap.newBuilder()
				.withBuckets(2)
				.withDefaultValue(DEFAULT_VALUE)
				.withInitialCapacity(100)
				.withMode(PrimitiveMapBuilder.MapMode.BLOCKING)
				.withLoadFactor(0.9f);

		ConcurrentIntLongMap map = b.build();

		map.put(1, 10L);
		long v = map.get(1);

		assertInstanceOf(ConcurrentIntLongMap.class, map);
		assertEquals(10L, v);
		assertEquals(-1, map.getDefaultValue());
		assertEquals(-1, map.get(2));
		assertEquals(map.get(2), map.getDefaultValue());
	}
}