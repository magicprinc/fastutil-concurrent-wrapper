package com.trivago.fastutilconcurrentwrapper.intkey;

import com.trivago.fastutilconcurrentwrapper.PrimitiveConcurrentMap;
import com.trivago.fastutilconcurrentwrapper.util.CFUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 @see PrimitiveConcurrentMap */
class PrimitiveConcurrentMapTest {
	@Test
	void _hashInt () {
		assertEquals(50_880, CFUtil.bucket(Integer.MIN_VALUE, 100_000));
		assertEquals(47_470, CFUtil.bucket(-Integer.MAX_VALUE, 100_000));
		assertEquals(74_300, CFUtil.bucket(-100_000, 100_000));
		assertEquals(56_431, CFUtil.bucket(-1, 100_000));
		assertEquals(0, CFUtil.bucket(0, 100_000));
		assertEquals(3_410, CFUtil.bucket(1, 100_000));
		assertEquals(82_299, CFUtil.bucket(100_000, 100_000));
		assertEquals(59_985, CFUtil.bucket(Integer.MAX_VALUE, 100_000));
	}

	@Test
	void _hashLong () {
		assertEquals(50_880, CFUtil.bucket(Long.MIN_VALUE, 100_000));
		assertEquals(50_880, CFUtil.bucket(Integer.MIN_VALUE & 0xFFFFFFFFL, 100_000));
		assertEquals(59_985, CFUtil.bucket((long) Integer.MIN_VALUE, 100_000));
		assertEquals(47_470, CFUtil.bucket(-Integer.MAX_VALUE & 0xFFFFFFFFL, 100_000));
		assertEquals(96_447, CFUtil.bucket((long) -Integer.MAX_VALUE, 100_000));
		assertEquals(16_093, CFUtil.bucket(-100_000L, 100_000));
		assertEquals(0, CFUtil.bucket(-1L, 100_000));
		assertEquals(0, CFUtil.bucket(0L, 100_000));
		assertEquals(3_410, CFUtil.bucket(1L, 100_000));
		assertEquals(82_299, CFUtil.bucket(100_000L, 100_000));
		assertEquals(59_985, CFUtil.bucket((long) Integer.MAX_VALUE, 100_000));
		assertEquals(50_880, CFUtil.bucket(Long.MAX_VALUE, 100_000));
	}

	@Test  @Disabled("Total checks: 38510819404, time: 8551,564, op/sec: 189_126_936")
	void longsAreSame () {
		long total = 0, t = System.nanoTime();
		for (long i = Long.MIN_VALUE, max = Long.MAX_VALUE - 479001599-9; i < max; i += 479001599){
			assertEquals(CFUtil.bucket(Long.valueOf(i), 1_000_000), CFUtil.bucket(i, 1_000_000));
			total++;
		}
		t = System.nanoTime() - t;
		System.out.printf("\nTotal checks: %d, time: %.3f, op/sec: %d\n\n", total, t / 1_000 / 1000.0, total*1_000_000_000/t);
	}

	@Test
	void _hashObj () {
		assertEquals(0, CFUtil.bucket(null, 100_000));

		assertEquals(50_880, CFUtil.bucket(Long.valueOf(Long.MIN_VALUE), 100_000));
		assertEquals(50_880, CFUtil.bucket(Long.valueOf(Integer.MIN_VALUE & 0xFFFFFFFFL), 100_000));
		assertEquals(59_985, CFUtil.bucket(Long.valueOf(Integer.MIN_VALUE), 100_000));
		assertEquals(47_470, CFUtil.bucket(Long.valueOf(-Integer.MAX_VALUE & 0xFFFFFFFFL), 100_000));
		assertEquals(96_447, CFUtil.bucket(Long.valueOf(-Integer.MAX_VALUE), 100_000));
		assertEquals(16_093, CFUtil.bucket(Long.valueOf(-100_000L), 100_000));
		assertEquals(0, CFUtil.bucket(Long.valueOf(-1L), 100_000));
		assertEquals(0, CFUtil.bucket(Long.valueOf(0L), 100_000));
		assertEquals(3_410, CFUtil.bucket(Long.valueOf(1L), 100_000));
		assertEquals(82_299, CFUtil.bucket(Long.valueOf(100_000L), 100_000));
		assertEquals(59_985, CFUtil.bucket(Long.valueOf(Integer.MAX_VALUE), 100_000));
		assertEquals(50_880, CFUtil.bucket(Long.valueOf(Long.MAX_VALUE), 100_000));
	}

	@Test
	void buildLongKey () {
		assertEquals(0, CFUtil.compoundKey(0,0));
		assertEquals(Integer.MIN_VALUE & 0xFFFFFFFFL, CFUtil.compoundKey(0,Integer.MIN_VALUE));
		assertEquals(0x8000_0000_0000_0000L, CFUtil.compoundKey(Integer.MIN_VALUE,0));
	}
}