package com.trivago.fastutilconcurrentwrapper.util;

import it.unimi.dsi.fastutil.HashCommon;
import jakarta.validation.constraints.Positive;
import org.jspecify.annotations.Nullable;

/**
 Concurrent Fast Util
 @see  */
public final class CFUtil {
	public static final StackTraceElement[] EMPTY_STACK_TRACE = new StackTraceElement[0];

	/** @see #bucket */
	public static int bucket (int hashOrKey, @Positive int bucketSize) {
		return Math.abs(HashCommon.mix(hashOrKey) % bucketSize);
	}

	/** @see #bucket */
	public static int bucket (@Nullable Object object4hashCode, @Positive int bucketSize) {
		return object4hashCode != null ? bucket(object4hashCode.hashCode(), bucketSize)
			: 0;
	}

	/**
	 Get positive, quite `random` bucket index between 0 and bucketSize-1 for any key
	 Fast. Safe for negative keys (including Long.MIN_VALUE, Integer.MIN_VALUE)

	 FastUtil has ❌ HashCommon#mix(long), but we use ✅ Long.hashCode + mix(int) because with mix(long):

	 mix(1L) ≠ mix(1) → it is against common knowledge and expectations
	 mix(1L) ≠ mix(Long.valueOf(1L)) → 😱

	 @see it.unimi.dsi.fastutil.HashCommon#mix(long)
	 @see it.unimi.dsi.fastutil.HashCommon#mix(int)
	 @see Long#hashCode(long)
	 */
	public static int bucket (long hashOrKey, @Positive int bucketSize) {
		//× return (int) Math.abs(HashCommon.mix(hashOrKey) % bucketSize);
		int intHash = Long.hashCode(hashOrKey);
		return Math.abs(HashCommon.mix(intHash) % bucketSize);
	}

	/**
	 * Combined two 32-bit keys into a 64-bit compound.

	 https://github.com/aeron-io/agrona/blob/master/agrona/src/main/java/org/agrona/collections/Hashing.java

	 * @param keyHi to make the upper bits
	 * @param keyLo to make the lower bits
	 * @return the compound key
	 */
	public static long compoundKey (int keyHi, final int keyLo) {
		return ((long)keyHi << 32) | (keyLo & 0xFfFf_FfFfL);
	}

	private CFUtil (){ throw new AssertionError(); }//new
}