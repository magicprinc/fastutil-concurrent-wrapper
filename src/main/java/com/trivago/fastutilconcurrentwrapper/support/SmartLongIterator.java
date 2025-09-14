package com.trivago.fastutilconcurrentwrapper.support;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSpliterator;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.LongConsumer;

/**
 @see SmartIterator
 @see it.unimi.dsi.fastutil.longs.LongIterator
 @see it.unimi.dsi.fastutil.longs.LongSpliterator
*/
public interface SmartLongIterator extends SmartIterator<Long>, LongIterator, LongSpliterator {
	@Override
	default @Nullable SmartLongIterator trySplit () {
		return null;// cannot be split
	}

	@Override  @Deprecated
	default void forEachRemaining (Consumer<? super Long> action) {
		LongIterator.super.forEachRemaining(action);
	}

	@Override
	default void forEachRemaining (LongConsumer action) {
		LongSpliterator.super.forEachRemaining(action);
	}
	@Override
	default void forEachRemaining (it.unimi.dsi.fastutil.longs.LongConsumer action) {
		LongSpliterator.super.forEachRemaining((LongConsumer)action);
	}

	@Override
	default int skip (int n) {
		return LongIterator.super.skip(n);
	}
	@Override
	default long skip (long n) {
		return LongSpliterator.super.skip(n);
	}

	@Override
	default boolean tryAdvance (LongConsumer action) {
		if (hasNext()){
			action.accept(nextLong());
			return true;
		} else
				return false;
	}

	@Override  @Deprecated
	default boolean tryAdvance (Consumer<? super Long> action) {
		return LongSpliterator.super.tryAdvance(action);
	}
}