package com.trivago.fastutilconcurrentwrapper.util;

import java.util.concurrent.CancellationException;
import java.util.function.Consumer;

/**
 JDK {@link Iterable#iterator()} is an external iterator.
 {{@link Iterable#forEach(Consumer)}} is an example of an internal iterator.
 Both are equivalent with one exception: internal iterator can't stop and remove current element.
 InternalIterableCallback helps to solve this problem.
 It is hard to implement Iterator for external iteration (it has state, can't have lock).
 It is much easier to implement an internal one.

 @see java.util.Iterator
 @see java.lang.Iterable
 @see java.util.Spliterator
 @see java.util.function.BiConsumer
*/
@FunctionalInterface
public interface InternalIterable extends AutoCloseable {

	CancellationException STOP = new CancellationException("InternalIterable.STOP"){
		{
			initCause(null);
			setStackTrace(CFUtil.EMPTY_STACK_TRACE);
		}
		@Override public String toString (){ return "CancellationException: "+getMessage(); }
	};

	IllegalArgumentException REMOVE = new IllegalArgumentException("InternalIterable.REMOVE", null){
		{
			setStackTrace(CFUtil.EMPTY_STACK_TRACE);
		}
		@Override public String toString (){ return "IllegalArgumentException: "+getMessage(); }
	};

	/**
	 Stop iteration loop
	 Other interesting alternative would be throwing and exception, e.g: {@link java.util.concurrent.CancellationException}
	*/
	@Override void close ();

	/**
	 Remove current item
	 @see java.util.Iterator#remove()
	 */
	default void remove () {
		throw new UnsupportedOperationException("remove");
	}

	default long index () {
		throw new UnsupportedOperationException("index");
	}

	default long size () {
		throw new UnsupportedOperationException("size");
	}
}