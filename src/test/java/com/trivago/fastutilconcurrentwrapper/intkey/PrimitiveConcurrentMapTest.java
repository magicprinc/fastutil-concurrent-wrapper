package com.trivago.fastutilconcurrentwrapper.intkey;

import com.trivago.fastutilconcurrentwrapper.PrimitiveConcurrentMap;
import com.trivago.fastutilconcurrentwrapper.PrimitiveKeyMap;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 @see PrimitiveConcurrentMap */
class PrimitiveConcurrentMapTest {
	@Test
	void _hashInt () {
		assertEquals(50_880, PrimitiveKeyMap.bucket(Integer.MIN_VALUE, 100_000));
		assertEquals(47_470, PrimitiveKeyMap.bucket(-Integer.MAX_VALUE, 100_000));
		assertEquals(74_300, PrimitiveKeyMap.bucket(-100_000, 100_000));
		assertEquals(56_431, PrimitiveKeyMap.bucket(-1, 100_000));
		assertEquals(0, PrimitiveKeyMap.bucket(0, 100_000));
		assertEquals(3_410, PrimitiveKeyMap.bucket(1, 100_000));
		assertEquals(82_299, PrimitiveKeyMap.bucket(100_000, 100_000));
		assertEquals(59_985, PrimitiveKeyMap.bucket(Integer.MAX_VALUE, 100_000));
	}

	@Test
	void _hashLong () {
		assertEquals(50_880, PrimitiveKeyMap.bucket(Long.MIN_VALUE, 100_000));
		assertEquals(50_880, PrimitiveKeyMap.bucket(Integer.MIN_VALUE & 0xFFFFFFFFL, 100_000));
		assertEquals(59_985, PrimitiveKeyMap.bucket((long) Integer.MIN_VALUE, 100_000));
		assertEquals(47_470, PrimitiveKeyMap.bucket(-Integer.MAX_VALUE & 0xFFFFFFFFL, 100_000));
		assertEquals(96_447, PrimitiveKeyMap.bucket((long) -Integer.MAX_VALUE, 100_000));
		assertEquals(16_093, PrimitiveKeyMap.bucket(-100_000L, 100_000));
		assertEquals(0, PrimitiveKeyMap.bucket(-1L, 100_000));
		assertEquals(0, PrimitiveKeyMap.bucket(0L, 100_000));
		assertEquals(3_410, PrimitiveKeyMap.bucket(1L, 100_000));
		assertEquals(82_299, PrimitiveKeyMap.bucket(100_000L, 100_000));
		assertEquals(59_985, PrimitiveKeyMap.bucket((long) Integer.MAX_VALUE, 100_000));
		assertEquals(50_880, PrimitiveKeyMap.bucket(Long.MAX_VALUE, 100_000));
	}

	@Test  @Disabled("Total checks: 38510819404, time: 8551,564, op/sec: 189_126_936")
	void longsAreSame () {
		long total = 0, t = System.nanoTime();
		for (long i = Long.MIN_VALUE, max = Long.MAX_VALUE - 479001599-9; i < max; i += 479001599){
			assertEquals(PrimitiveKeyMap.bucket(Long.valueOf(i), 1_000_000), PrimitiveKeyMap.bucket(i, 1_000_000));
			total++;
		}
		t = System.nanoTime() - t;
		System.out.printf("\nTotal checks: %d, time: %.3f, op/sec: %d\n\n", total, t / 1_000 / 1000.0, total*1_000_000_000/t);
	}

	@Test
	void _hashObj () {
		assertEquals(0, PrimitiveKeyMap.bucket(null, 100_000));

		assertEquals(50_880, PrimitiveKeyMap.bucket(Long.valueOf(Long.MIN_VALUE), 100_000));
		assertEquals(50_880, PrimitiveKeyMap.bucket(Long.valueOf(Integer.MIN_VALUE & 0xFFFFFFFFL), 100_000));
		assertEquals(59_985, PrimitiveKeyMap.bucket(Long.valueOf(Integer.MIN_VALUE), 100_000));
		assertEquals(47_470, PrimitiveKeyMap.bucket(Long.valueOf(-Integer.MAX_VALUE & 0xFFFFFFFFL), 100_000));
		assertEquals(96_447, PrimitiveKeyMap.bucket(Long.valueOf(-Integer.MAX_VALUE), 100_000));
		assertEquals(16_093, PrimitiveKeyMap.bucket(Long.valueOf(-100_000L), 100_000));
		assertEquals(0, PrimitiveKeyMap.bucket(Long.valueOf(-1L), 100_000));
		assertEquals(0, PrimitiveKeyMap.bucket(Long.valueOf(0L), 100_000));
		assertEquals(3_410, PrimitiveKeyMap.bucket(Long.valueOf(1L), 100_000));
		assertEquals(82_299, PrimitiveKeyMap.bucket(Long.valueOf(100_000L), 100_000));
		assertEquals(59_985, PrimitiveKeyMap.bucket(Long.valueOf(Integer.MAX_VALUE), 100_000));
		assertEquals(50_880, PrimitiveKeyMap.bucket(Long.valueOf(Long.MAX_VALUE), 100_000));
	}

	@Test
	void buildLongKey () {
		assertEquals(0, PrimitiveKeyMap.compoundKey(0,0));
		assertEquals(Integer.MIN_VALUE & 0xFFFFFFFFL, PrimitiveKeyMap.compoundKey(0,Integer.MIN_VALUE));
		assertEquals(0x8000_0000_0000_0000L, PrimitiveKeyMap.compoundKey(Integer.MIN_VALUE,0));
	}
}