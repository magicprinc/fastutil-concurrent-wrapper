package com.trivago.fastutilconcurrentwrapper.intkey;

import com.trivago.fastutilconcurrentwrapper.PrimitiveConcurrentMap;
import com.trivago.fastutilconcurrentwrapper.util.CFUtil;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

/**
 @see PrimitiveConcurrentMap
*/
class PrimitiveConcurrentMapTest {
	@Test
	void _hashInt () {
		assertEquals(74720, CFUtil.bucket(Integer.MIN_VALUE, 100_000));
		assertEquals(79862, CFUtil.bucket(-Integer.MAX_VALUE, 100_000));
		assertEquals(3539, CFUtil.bucket(-100_000, 100_000));
		assertEquals(57924, CFUtil.bucket(-2, 100_000));
		assertEquals(83783, CFUtil.bucket(-1, 100_000));
		assertEquals(0, CFUtil.bucket(0, 100_000));
		assertEquals(76727, CFUtil.bucket(1, 100_000));
		assertEquals(47078, CFUtil.bucket(2, 100_000));
		assertEquals(55934, CFUtil.bucket(100_000, 100_000));
		assertEquals(67416, CFUtil.bucket(Integer.MAX_VALUE, 100_000));
	}

	@Test
	void _hashLong () {
		assertEquals(74720, CFUtil.bucket(Long.MIN_VALUE, 100_000));
		assertEquals(79862, CFUtil.bucket(Long.MIN_VALUE+1, 100_000));
		assertEquals(74720, CFUtil.bucket(Integer.MIN_VALUE & 0xFFFFFFFFL, 100_000));
		assertEquals(74720, CFUtil.bucket((long) Integer.MIN_VALUE, 100_000));
		assertEquals(79862, CFUtil.bucket(-Integer.MAX_VALUE & 0xFFFFFFFFL, 100_000));
		assertEquals(79862, CFUtil.bucket((long) -Integer.MAX_VALUE, 100_000));
		assertEquals(3539, CFUtil.bucket(-100_000L, 100_000));
		assertEquals(83783, CFUtil.bucket(-1L, 100_000));
		assertEquals(0, CFUtil.bucket(0L, 100_000));
		assertEquals(76727, CFUtil.bucket(1L, 100_000));
		assertEquals(55934, CFUtil.bucket(100_000L, 100_000));
		assertEquals(67416, CFUtil.bucket((long) Integer.MAX_VALUE, 100_000));
		assertEquals(74720, CFUtil.bucket(Long.MAX_VALUE, 100_000));
	}

	@Test
	void longsAreSame () {
		long total = 0, t = System.nanoTime();
		for (long i = Integer.MIN_VALUE; i <= Integer.MAX_VALUE; i++){
			assertEquals(CFUtil.bucket(Long.valueOf(i), 4_000_000), CFUtil.bucket(i, 4_000_000));
			assertEquals(CFUtil.bucket((int)i, 4_000_000), CFUtil.bucket(i, 4_000_000));
			total++;
		}
		t = System.nanoTime() - t;
		System.out.printf("\nTotal checks (inside int): %d, time: %.3f, op/sec: %d\n\n", total, t / 1_000 / 1000.0, total*1_000_000_000/t);

		long x = Long.MIN_VALUE;
		assertEquals(CFUtil.bucket(Long.valueOf(x), 4_000_000), CFUtil.bucket(x, 4_000_000));
		x = Long.MAX_VALUE;
		assertEquals(CFUtil.bucket(Long.valueOf(x), 4_000_000), CFUtil.bucket(x, 4_000_000));
		assertEquals(CFUtil.bucket(Long.hashCode(x), 4_000_000), CFUtil.bucket(x, 4_000_000));

		total = 0;
		var r = ThreadLocalRandom.current();
		for (int z = 0; z < 4_000_000; z++){
			long i = r.nextLong();
			assertEquals(CFUtil.bucket(Long.valueOf(i), 4_000_000), CFUtil.bucket(i, 4_000_000));
			i = Long.MIN_VALUE + z;
			assertEquals(CFUtil.bucket(Long.valueOf(i), 4_000_000), CFUtil.bucket(i, 4_000_000));
			assertEquals(CFUtil.bucket(Long.hashCode(i), 4_000_000), CFUtil.bucket(i, 4_000_000));
			i = Long.MAX_VALUE - z;
			assertEquals(CFUtil.bucket(Long.valueOf(i), 4_000_000), CFUtil.bucket(i, 4_000_000));
			assertEquals(CFUtil.bucket(Long.hashCode(i), 4_000_000), CFUtil.bucket(i, 4_000_000));
			total++;
		}
		assertEquals(4_000_000, total);
	}

	@Test
	void _hashObj () {
		assertEquals(0, CFUtil.bucket(null, 100_000));

		assertEquals(74720, CFUtil.bucket(Long.valueOf(Long.MIN_VALUE), 100_000));
		assertEquals(74720, CFUtil.bucket(Long.valueOf(Integer.MIN_VALUE & 0xFFFFFFFFL), 100_000));
		assertEquals(74720, CFUtil.bucket(Long.valueOf(Integer.MIN_VALUE), 100_000));

		assertEquals(79862, CFUtil.bucket(Long.valueOf(-Integer.MAX_VALUE & 0xFFFFFFFFL), 100_000));
		assertEquals(79862, CFUtil.bucket(Long.valueOf(-Integer.MAX_VALUE), 100_000));
		assertEquals(3539, CFUtil.bucket(Long.valueOf(-100_000L), 100_000));
		assertEquals(57924, CFUtil.bucket(Long.valueOf(-2L), 100_000));
		assertEquals(57924, CFUtil.bucket(Integer.valueOf(-2), 100_000));
		assertEquals(83783, CFUtil.bucket(Long.valueOf(-1L), 100_000));
		assertEquals(0, CFUtil.bucket(Long.valueOf(0L), 100_000));
		assertEquals(76727, CFUtil.bucket(Long.valueOf(1L), 100_000));
		assertEquals(47078, CFUtil.bucket(Long.valueOf(2L), 100_000));
		assertEquals(47078, CFUtil.bucket(Integer.valueOf(2), 100_000));
		assertEquals(55934, CFUtil.bucket(Long.valueOf(100_000L), 100_000));
		assertEquals(67416, CFUtil.bucket(Long.valueOf(Integer.MAX_VALUE), 100_000));
		assertEquals(74720, CFUtil.bucket(Long.valueOf(Long.MAX_VALUE), 100_000));
	}

	@Test
	void buildLongKey () {
		assertEquals(0, CFUtil.compoundKey(0,0));
		assertEquals(Integer.MIN_VALUE & 0xFFFFFFFFL, CFUtil.compoundKey(0,Integer.MIN_VALUE));
		assertEquals(0x8000_0000_0000_0000L, CFUtil.compoundKey(Integer.MIN_VALUE,0));
	}
}