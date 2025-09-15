package com.trivago.fastutilconcurrentwrapper.util;

import it.unimi.dsi.fastutil.HashCommon;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

/**
 Concurrent Fast Util

 @see it.unimi.dsi.fastutil.HashCommon#mix
 @see it.unimi.dsi.fastutil.HashCommon#murmurHash3
 @see java.util.Objects#hashCode(Object)
 @see jdk.internal.util.random.RandomSupport#mixMurmur64
 @see org.springframework.util.ConcurrentReferenceHashMap#getHash(Object)
*/
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CFUtil {
	public static final StackTraceElement[] EMPTY_STACK_TRACE = new StackTraceElement[0];

	/** @see it.unimi.dsi.fastutil.HashCommon#murmurHash3 */
	public static int hash (int hash) {
		return HashCommon.murmurHash3(hash);
	}

	/** @see #bucket */
	public static @PositiveOrZero int bucket (int hashOrKey, @Positive int bucketSize) {
		int goodHash = HashCommon.murmurHash3(hashOrKey);
		return Math.abs(goodHash % bucketSize);
	}

	/**
	 @see #bucket
	 @see java.util.Objects#hashCode(Object)
	 */
	public static @PositiveOrZero int bucket (@Nullable Object object4hashCode, @Positive int bucketSize) {
		if (object4hashCode == null){
			return 0;
		} else if (object4hashCode instanceof Long n){
			return bucket(n.longValue(), bucketSize);
		} else {
			return bucket(object4hashCode.hashCode(), bucketSize);
		}
	}

	/**
	 Get positive, quite `random` bucket index between [0 and bucketSize-1] for any key.
	 Fast. Safe for negative keys (including Long.MIN_VALUE, Integer.MIN_VALUE)

	 FastUtil has ❌ HashCommon mix/murmurHash3(long), but we use ✅ Long.hashCode + murmurHash3(int) because (long):

	 mix(1L) ≠ mix(1) → it is against common knowledge and expectations
	 mix(1L) ≠ mix(Long.valueOf(1L)) → 😱

	 @see #hash(int)
	 @see Long#hashCode(long)
	*/
	public static @PositiveOrZero int bucket (long hashOrKey, @Positive int bucketSize) {
		return hashOrKey > Integer.MAX_VALUE || hashOrKey < Integer.MIN_VALUE
			? bucket(Long.hashCode(hashOrKey), bucketSize)
			: bucket((int) hashOrKey, bucketSize);// long uses low 32bit only
	}

	/**
	 * Combined two 32-bit keys into a 64-bit compound.

	 https://github.com/aeron-io/agrona/blob/master/agrona/src/main/java/org/agrona/collections/Hashing.java

	 * @param keyHi to make the upper bits
	 * @param keyLo to make the lower bits
	 * @return the compound key
	 */
	public static long compoundKey (int keyHi, int keyLo) {
		return ((long)keyHi << 32) | (keyLo & 0xFfFf_FfFfL);
	}
}