package com.trivago.fastutilconcurrentwrapper.util;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
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

	/// re-hash
	/// 👀 hash4j
	///
	/// @see it.unimi.dsi.fastutil.HashCommon#murmurHash3
	/// @see org.springframework.util.ConcurrentReferenceHashMap#getHash(Object)
	/// @see org.jsr166.ConcurrentLinkedHashMap#hash(int)
	/// @see jdk.internal.classfile.impl.AbstractPoolEntry#phiMix
	/// @see io.vertx.core.json.jackson.HybridJacksonPool.XorShiftThreadProbe#probe
	/// @see com.google.common.util.concurrent.Striped#smear
	/// @see java.util.concurrent.ThreadLocalRandom#PROBE_INCREMENT
	/// @see java.util.concurrent.ThreadLocalRandom#advanceProbe
	/// @see #bucket
	/// @see #hash
	public static int hash (int hashOrKey) {
		return HashCommon.murmurHash3(hashOrKey);
	}

	public static int hash (long hashOrKey) {
		return hashOrKey < Integer.MIN_VALUE || hashOrKey > Integer.MAX_VALUE
				? Long.hashCode(HashCommon.murmurHash3(hashOrKey))
				: HashCommon.murmurHash3((int) hashOrKey);// if long uses low 32bit only
	}

	public static int hash (@Nullable Long hashOrKey) {
		if (hashOrKey == null)
				return 0;
		return hashOrKey < Integer.MIN_VALUE || hashOrKey > Integer.MAX_VALUE
			? Long.hashCode(HashCommon.murmurHash3(hashOrKey))
			: HashCommon.murmurHash3(hashOrKey.intValue());// if long uses low 32bit only
	}

	public static int hash (@Nullable Object object4hashCode) {
		if (object4hashCode == null)
			return 0;
		else if (object4hashCode instanceof Long n)
			return n < Integer.MIN_VALUE || n > Integer.MAX_VALUE
				? Long.hashCode(HashCommon.murmurHash3(n))
				: HashCommon.murmurHash3(n.intValue());// if long uses low 32bit only
		else
			return HashCommon.murmurHash3(object4hashCode.hashCode());
	}


	/** @see #bucket */
	public static @PositiveOrZero int bucket (int hashOrKey, @Positive int bucketSize) {
		return Math.abs(hash(hashOrKey) % bucketSize);
	}

	/// @see #bucket
	/// @see java.util.Objects#hashCode(Object)
	public static @PositiveOrZero int bucket (@Nullable Object object4hashCode, @Positive int bucketSize) {
		return Math.abs(hash(object4hashCode) % bucketSize);
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
		return Math.abs(hash(hashOrKey) % bucketSize);
	}
	public static @PositiveOrZero int bucket (@Nullable Long hashOrKey, @Positive int bucketSize) {
		return Math.abs(hash(hashOrKey) % bucketSize);
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

	/**
	 Is varargs empty?
	 @see #safeVarArgs
	 */
	public static boolean blankVarargs (@Nullable Object @Nullable [] args) {
		return args == null
				|| args.length == 0
				|| (args.length == 1 && args[0] == null);
	}

	/// Fix usual varargs mistakes (but type is lost)
	/// ~ Considers an Object array passed into a varargs parameter as collection of arguments rather than as single argument).
	/// @see #blankVarargs
	public static Object @Nullable[] safeVarArgs (@Nullable Object @Nullable[] varArgs) {
		if (varArgs == null)
				return ObjectArrays.EMPTY_ARRAY;// not some T[]!

		if (varArgs.length == 1 && varArgs[0] instanceof Object[] a)// antT[] instanceof Object[]!
				return a;

		return varArgs;// usual good varargs
	}
}