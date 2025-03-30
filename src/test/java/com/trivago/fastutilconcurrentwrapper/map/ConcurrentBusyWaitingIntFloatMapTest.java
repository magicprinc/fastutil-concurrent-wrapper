package com.trivago.fastutilconcurrentwrapper.map;

import com.trivago.fastutilconcurrentwrapper.IntFloatMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 @see ConcurrentBusyWaitingIntFloatMap */
class ConcurrentBusyWaitingIntFloatMapTest {
	@Test
	void basic () {
		for (var m : new IntFloatMap[]{new ConcurrentBusyWaitingIntFloatMap(10, 11, 0.5f, -1), new ConcurrentIntFloatMap(10, 11, 0.5f, -1)}){
			assertEquals(-1.0f, m.getDefaultValue());
			assertEquals(0, m.size());
			assertFalse(m.containsKey(42));
			assertEquals(-1, m.get(42));
			assertEquals(0, m.computeIfPresent(42, (k,v)->{
				fail();
				return -9.0f;
			}));
			assertEquals(0, m.remove(42));
			assertFalse(m.remove(42, 1));

			assertEquals(0, m.put(42, 3.14f));
			assertEquals(3.14f, m.get(42));
			assertTrue(m.remove(42, 3.14f));

			assertEquals(0, m.size());

			assertEquals(3.14f, m.computeIfAbsent(42, k->{
				assertEquals(42, k);
				return 3.14f;
			}));
			assertEquals(3.14f, m.remove(42));
		}
	}
}