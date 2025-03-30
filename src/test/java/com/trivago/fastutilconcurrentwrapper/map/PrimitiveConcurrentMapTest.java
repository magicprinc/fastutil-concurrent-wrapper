package com.trivago.fastutilconcurrentwrapper.map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 @see PrimitiveConcurrentMap */
class PrimitiveConcurrentMapTest {
	@Test
	void basic () {
		assertEquals(50_880, PrimitiveConcurrentMap.bucket(Integer.MIN_VALUE, 100_000));
		assertEquals(47_470, PrimitiveConcurrentMap.bucket(-Integer.MAX_VALUE, 100_000));
		assertEquals(74_300, PrimitiveConcurrentMap.bucket(-100_000, 100_000));
		assertEquals(56_431, PrimitiveConcurrentMap.bucket(-1, 100_000));
		assertEquals(0, PrimitiveConcurrentMap.bucket(0, 100_000));
		assertEquals(3_410, PrimitiveConcurrentMap.bucket(1, 100_000));
		assertEquals(82_299, PrimitiveConcurrentMap.bucket(100_000, 100_000));
		assertEquals(59_985, PrimitiveConcurrentMap.bucket(Integer.MAX_VALUE, 100_000));
	}
}