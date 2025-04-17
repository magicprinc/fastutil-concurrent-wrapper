package com.trivago.fastutilconcurrentwrapper.util;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSpliterator;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 @see SmartIterator
 @see IntIterator
 @see it.unimi.dsi.fastutil.ints.IntIterators
 @see IntSpliterator
 @see it.unimi.dsi.fastutil.ints.IntSpliterators
*/
public interface SmartIntIterator extends SmartIterator<Integer>, IntIterator, IntSpliterator {
	@Override
	default @Nullable SmartIntIterator trySplit () {
		return null;// cannot be split
	}

	@Override  @Deprecated
	default void forEachRemaining (Consumer<? super Integer> action) {
		IntIterator.super.forEachRemaining(action);
	}

	@Override
	default void forEachRemaining (IntConsumer action) {
		IntSpliterator.super.forEachRemaining(action);
	}
	@Override
	default void forEachRemaining (it.unimi.dsi.fastutil.ints.IntConsumer action) {
		IntSpliterator.super.forEachRemaining((IntConsumer)action);
	}

	@Override
	default int skip (int n) {
		return IntIterator.super.skip(n);
	}
	@Override
	default long skip (long n) {
		return IntSpliterator.super.skip(n);
	}

	@Override
	default boolean tryAdvance (IntConsumer action) {
		if (hasNext()){
			action.accept(nextInt());
			return true;
		} else
				return false;
	}

	@Override  @Deprecated
	default boolean tryAdvance (Consumer<? super Integer> action) {
		return IntSpliterator.super.tryAdvance(action);
	}
}