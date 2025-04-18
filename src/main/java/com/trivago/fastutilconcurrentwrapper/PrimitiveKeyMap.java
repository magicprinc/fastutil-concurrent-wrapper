package com.trivago.fastutilconcurrentwrapper;

import it.unimi.dsi.fastutil.HashCommon;
import jakarta.validation.constraints.Positive;
import org.jspecify.annotations.Nullable;

/**
 @see it.unimi.dsi.fastutil.Function
 */
public interface PrimitiveKeyMap {

	int size ();

	boolean isEmpty ();

	void clear ();

	/** @see #bucket */
	static int bucket (int hashOrKey, @Positive int bucketSize) {
		return Math.abs(HashCommon.mix(hashOrKey) % bucketSize);
	}

	/** @see #bucket */
	static int bucket (@Nullable Object object4hashCode, @Positive int bucketSize) {
		return object4hashCode != null ? bucket(object4hashCode.hashCode(), bucketSize)
				: 0;
	}

	/**
	 Get positive, quite `random` bucket index between 0 and bucketSize-1 for any key
	 Fast. Safe for negative keys (including Long.MIN_VALUE, Integer.MIN_VALUE)

	 FastUtil has ❌ HashCommon#mix(long), but we use ✅ Long.hashCode + mix(int) because:

	 mix(1L) ≠ mix(1) → it is against common knowledge and expectations
	 mix(1L) ≠ mix(Long.valueOf(1L)) → 😱

	 @see it.unimi.dsi.fastutil.HashCommon#mix(long)
	 @see it.unimi.dsi.fastutil.HashCommon#mix(int)
	 @see Long#hashCode(long)
	*/
	static int bucket (long hashOrKey, @Positive int bucketSize) {
		//× return (int) Math.abs(HashCommon.mix(hashOrKey) % bucketSize);
		int intHash = Long.hashCode(hashOrKey);
		return Math.abs(HashCommon.mix(intHash) % bucketSize);
	}
}