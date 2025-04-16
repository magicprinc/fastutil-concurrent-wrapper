package com.trivago.fastutilconcurrentwrapper.util;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 @see java.util.Collections#synchronizedCollection(Collection)
*/
public class ReadWriteLockCollection<E> implements Collection<E>, Serializable {
	@Serial private static final long serialVersionUID = -2261498858663173273L;
	final Collection<E> c;  // Backing Collection
	final ReadWriteLock lock;

	public ReadWriteLockCollection (Collection<E> c) {
		this.c = Objects.requireNonNull(c);
		lock = new ReentrantReadWriteLock();
	}//new

	public ReadWriteLockCollection (Collection<E> c, ReadWriteLock mutex) {
		this.c = Objects.requireNonNull(c);
		this.lock = Objects.requireNonNull(mutex);
	}//new

	private void unlockReadLock () {
		lock.readLock().unlock();
	}
	@SuppressWarnings("LockAcquiredButNotSafelyReleased")
	protected CloseableLock read () {
		lock.readLock().lock();
		return this::unlockReadLock;
	}

	private void unlockWriteLock () {
		lock.writeLock().unlock();
	}
	@SuppressWarnings("LockAcquiredButNotSafelyReleased")
	protected CloseableLock write () {
		lock.writeLock().lock();
		return this::unlockWriteLock;
	}

	@Override
	public int size () {
		try (var __ = read()){ return c.size(); }
	}

	@Override
	public boolean isEmpty () {
		try (var __ = read()){ return c.isEmpty(); }
	}

	@Override
	public boolean contains (Object o) {
		try (var __ = read()){ return c.contains(o); }
	}

	@Override
	public SmartIterator<E> iterator () {
		return new SmartIterator<>(){
			private Iterator<E> src;
			private Iterator<E> it () {
				if (src == null){ src = c.iterator(); }
				return src;
			}
			@Override
			public boolean hasNext () {
				try (var __ = read()){
					return it().hasNext();
				}
			}
			@Override
			public E next () {
				try (var __ = read()){
					if (!hasNext())
							throw new NoSuchElementException("hasNext returned false");
					return it().next();
				}
			}
			@Override
			public void remove () {
				try (var __ = write()){
					it().remove();
				}
			}

			@Override
			public boolean tryAdvance (Consumer<? super E> action) {
				E v;
				try (var __ = read()){
					var i = it();
					if (i.hasNext()){
						v = i.next();
					} else {
						return false;
					}
				}
				action.accept(v);
				return true;
			}

			@Override
			public long estimateSize () {
				return ReadWriteLockCollection.this.size();
			}

			@Override
			public long getExactSizeIfKnown () {
				return ReadWriteLockCollection.this.size();
			}
		};
	}

	@Override
	public Object[] toArray () {
		try (var __ = read()){ return c.toArray(); }
	}

	@Override
	public <T> T[] toArray (T[] a) {
		try (var __ = read()){ return c.toArray(a); }
	}

	@Override
	public <T> T[] toArray (IntFunction<T[]> f) {
		try (var __ = read()){ return c.toArray(f); }
	}

	@Override
	public boolean containsAll (Collection<?> coll) {
		try (var __ = read()){ return c.containsAll(coll); }
	}

	@Override
	public String toString () {
		try (var __ = read()){ return c.toString(); }
	}

	@Override
	public void forEach (Consumer<? super E> consumer) {
		try (var __ = read()){ c.forEach(consumer); }
	}

	@Serial
	private void writeObject (ObjectOutputStream s) throws IOException {
		try (var __ = read()){ s.defaultWriteObject(); }
	}

	@Override
	public int hashCode () {
		try (var __ = read()){ return c.hashCode(); }
	}

	@Override
	public boolean equals (Object obj) {
		if (this == obj){ return true; }
		try (var __ = read()){
			return obj instanceof ReadWriteLockCollection<?> x ? c.equals(x.c)
					: c.equals(obj);
		}
	}

	@Override
	public boolean add (E e) {
		try (var __ = write()){ return c.add(e); }
	}

	@Override
	public boolean remove (Object o) {
		try (var __ = write()){ return c.remove(o); }
	}

	@Override
	public boolean addAll (Collection<? extends E> coll) {
		try (var __ = write()){ return c.addAll(coll); }
	}

	@SafeVarargs
	public final boolean addAll (E... elements) {
		try (var __ = write()){ return Collections.addAll(c, elements); }
	}

	@Override
	public boolean removeAll (Collection<?> coll) {
		try (var __ = write()){ return c.removeAll(coll); }
	}

	@Override
	public boolean removeIf (Predicate<? super E> filter) {
		try (var __ = write()){ return c.removeIf(filter); }
	}

	@Override
	public boolean retainAll (Collection<?> coll) {
		try (var __ = write()){ return c.retainAll(coll); }
	}

	@Override
	public void clear () {
		try (var __ = write()){ c.clear(); }
	}

	@Override
	public Spliterator<E> spliterator () {
		return iterator();
	}

	@Override
	public Stream<E> stream () {
		return iterator().stream();
	}

	@Override
	public Stream<E> parallelStream () {
		return iterator().stream().parallel();
	}
}