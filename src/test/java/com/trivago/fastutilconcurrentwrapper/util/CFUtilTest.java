package com.trivago.fastutilconcurrentwrapper.util;

import it.unimi.dsi.fastutil.objects.ObjectArrays;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 @see CFUtil */
class CFUtilTest {
	@Test
	void _hash () {
		assertEquals(1832674720, CFUtil.hash(Integer.MIN_VALUE));
		assertEquals(-2114883783, CFUtil.hash(-1));
		assertEquals(0, CFUtil.hash(0));
		assertEquals(1364076727, CFUtil.hash(1));
		assertEquals(-104067416, CFUtil.hash(Integer.MAX_VALUE));
	}

	@Test
	void _blankVarargs () {
		assertTrue(CFUtil.blankVarargs(null));
		assertTrue(CFUtil.blankVarargs((Object[])null));
		assertTrue(CFUtil.blankVarargs(ObjectArrays.EMPTY_ARRAY));
		assertTrue(CFUtil.blankVarargs(new String[]{null}));
		assertTrue(CFUtil.blankVarargs(new Object[]{null}));

		assertFalse(CFUtil.blankVarargs(new Object[]{null,null}));
		assertFalse(CFUtil.blankVarargs(new Integer[]{1}));
	}

	@Test
	void _safeVarArgs () {
		Object[] a = null;

		assertSame(ObjectArrays.EMPTY_ARRAY, CFUtil.safeVarArgs(null));
		assertSame(ObjectArrays.EMPTY_ARRAY, CFUtil.safeVarArgs(a));
		assertSame(ObjectArrays.EMPTY_ARRAY, CFUtil.safeVarArgs(ObjectArrays.EMPTY_ARRAY));

		a = new Object[]{null};
		assertSame(a, CFUtil.safeVarArgs(a));
		a = new Integer[]{1};
		assertSame(a, CFUtil.safeVarArgs(a));

		a = new Integer[0];
		assertSame(a, CFUtil.safeVarArgs(new Object[]{a}));
	}
}