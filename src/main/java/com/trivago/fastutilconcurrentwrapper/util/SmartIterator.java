package com.trivago.fastutilconcurrentwrapper.util;

import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSpliterator;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 Утилиты для работы с {@link Iterator}

 See {@link org.jooq.lambda.Seq} from https://github.com/jOOQ/jOOL
 		- https://blog.jooq.org/lesser-known-joo%ce%bb-features-useful-collectors/
 		- https://blog.jooq.org/how-to-translate-sql-group-by-and-aggregations-to-java-8/
 		- https://blog.jooq.org/using-joo%ce%bb-to-combine-several-java-8-collectors-into-one/

    ! https://blog.jooq.org/jooq-and-jooλ/ = https://blog.jooq.org/jooq-and-joo%ce%bb/

 		-	https://www.jooq.org/java-8-and-sql

 		- https://blog.jooq.org/java-8-friday-10-subtle-mistakes-when-using-the-streams-api/

 <p>PS: сделать Iterable из Iterator: {@code (Iterable<E>)()->iterator}

 @see com.google.common.collect.Iterators
 @see com.google.common.collect.Iterables
 @see com.google.common.collect.Streams
 @see org.jooq.lambda.util.PrimitiveArrayIterator

 @see it.unimi.dsi.fastutil.objects.ObjectIterators
 @see it.unimi.dsi.fastutil.objects.ObjectIterators.ArrayIterator
 @see Arrays
 @see it.unimi.dsi.fastutil.ints.IntIterator
 @see java.util.PrimitiveIterator.OfInt

 @see Spliterators
 @see Spliterators#iterator(Spliterator)
 @see Spliterators.IteratorSpliterator
 @see org.apache.commons.lang3.stream.Streams.EnumerationSpliterator

 @see com.google.common.collect.AbstractIterator
 @see com.google.common.collect.AbstractSequentialIterator
 @see com.google.common.collect.UnmodifiableIterator
*/
public interface SmartIterator<E> extends ObjectIterator<E>, ObjectSpliterator<E> {
	@Override
	default @Nullable ObjectSpliterator<E> trySplit () {
		return null;// cannot be split
	}

	@Override default long estimateSize (){ return Long.MAX_VALUE; }

	@Override default int characteristics (){ return Spliterator.ORDERED; }

	@Override
	default boolean tryAdvance (Consumer<? super E> action) {
		if (hasNext()){
			action.accept(next());
			return true;
		} else {
			return false;
		}
	}

	@Override
	default void forEachRemaining (Consumer<? super E> action) {
		ObjectIterator.super.forEachRemaining(action);
	}

	default Stream<E> stream () {
		return StreamSupport.stream(this, false/* not parallel*/);
	}

// todo review/test https://github.com/javadev/underscore-java ➕ https://github.com/rgmatute/lodash-java/blob/master/src/rgmatute/lodash/java/__.java
// https://github.com/lacuna/bifurcan/blob/master/doc/comparison.md
// https://github.com/GlenKPeterson/Paguro ➕ https://vavr.io/ + https://github.com/brianburton/java-immutable-collections ×https://github.com/hrldcpr/pcollections  @ https://www.reddit.com/r/java/comments/x4fvgp/what_is_the_best_persistent_collection_library/

	default void close (){}//~ AutoCloseable#close implements в CloseableIterator, чтобы IDEA не ругалась на не-закрытие JIterator
}