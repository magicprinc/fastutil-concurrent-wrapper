package com.trivago.fastutilconcurrentwrapper.longkey;

import com.trivago.fastutilconcurrentwrapper.PrimitiveConcurrentMap;
import com.trivago.fastutilconcurrentwrapper.PrimitiveKeyMap;
import com.trivago.fastutilconcurrentwrapper.util.PaddedLock;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.jctools.maps.NonBlockingHashMapLong;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongConsumer;

/**
 Similar to {@link ConcurrentLongObjectMap}, but backed with NonBlockingHashMapLong ⇒ non-blocking reads 🚀
 https://github.com/JCTools/JCTools/blob/master/jctools-core/src/main/java/org/jctools/maps/NonBlockingHashMapLong.java
 https://stackoverflow.com/questions/61721386/caffeine-cache-specify-expiry-for-an-entry

 @see org.jctools.maps.NonBlockingHashMapLong
 @see it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 @see com.google.common.util.concurrent.Striped

 @see NBHMLCacheExpirer
*/
@SuppressWarnings("LockAcquiredButNotSafelyReleased")
public class StripedNonBlockingHashMapLong<E> implements ConcurrentMap<Long,E>, Long2ObjectMap<E>, PrimitiveKeyMap {
	final NonBlockingHashMapLong<E> m;
	/** @see com.google.common.util.concurrent.Striped#lock(int) */
	final PaddedLock[] s;

	@SuppressWarnings("resource")
	public StripedNonBlockingHashMapLong (int initialSize, boolean optForSpace, int stripes) {
		assert stripes > 0 : "Stripes must be positive, but "+stripes;
		assert stripes < 100_000_000 : "Too much Stripes: "+stripes;
		m = new NonBlockingHashMapLong<>(Math.max(initialSize, stripes), optForSpace);
		s = new PaddedLock[stripes];
		for (int i = 0; i < stripes; i++)
				s[i] = new PaddedLock();
	}//new

	/** @see com.google.common.util.concurrent.Striped#get(Object) */
	protected PaddedLock write (long key) {
		var lock = s[PrimitiveConcurrentMap.bucket(Long.hashCode(key), s.length)];
		lock.lock();
		return lock;
	}

	@Override public int size (){ return m.size(); }
	@Override public boolean isEmpty (){ return m.isEmpty(); }

	@Override
	public synchronized void clear () {
		withAllKeysWriteLock(NonBlockingHashMapLong::clear);
	}
	public synchronized void clear (boolean large) {
		withAllKeysWriteLock(map->map.clear(large));
	}

	public void withAllKeysWriteLock (Consumer<NonBlockingHashMapLong<E>> singleThreadMapModifier) {
		for (PaddedLock paddedLock : s)
				paddedLock.lock();
		try {
			singleThreadMapModifier.accept(m);
		} finally {
			for (Lock lock : s)
					lock.unlock();
		}
	}

	@Override  @Deprecated
	public E putIfAbsent (Long key, E value) {
		return putIfAbsent(key.longValue(), value);
	}
	@Override
	public E putIfAbsent (long key, E value) {
		try (var __ = write(key)){
			return m.putIfAbsent(key, value);
		}
	}

	@Override  @Deprecated
	public boolean remove (/*Long*/Object key, /*E*/ Object value) {
		return remove(((Long)key).longValue(), value);
	}
	@Override
	public boolean remove (long key, /*E*/ Object value) {
		try (var __ = write(key)){
			return m.remove(key, value);
		}
	}

	@Override  @Deprecated
	public boolean replace (Long key, E oldValue, E newValue) {
		return replace(key.longValue(), oldValue, newValue);
	}
	@Override
	public boolean replace (long key, E oldValue, E newValue) {
		try (var __ = write(key)){
			return m.replace(key, oldValue, newValue);
		}
	}

	@Override  @Deprecated
	public E replace (Long key, E value) {
		return replace(key.longValue(), value);
	}
	@Override
	public E replace (long key, E value) {
		try (var __ = write(key)){
			return m.replace(key, value);
		}
	}

	@Override  @Deprecated
	public boolean containsKey (Object key) {
		return m.containsKey(key);
	}
	@Override
	public boolean containsKey (long key) {
		return m.containsKey(key);
	}

	@Override
	public boolean containsValue (Object value) {
		return m.containsValue(value);
	}

	@Override  @Deprecated
	public E get (Object key) {
		return m.get(key);
	}
	@Override
	public E get (long key) {
		return m.get(key);
	}

	@Override  @Deprecated
	public E put (Long key, E value) {
		return put(key.longValue(), value);
	}
	@Override
	public E put (long key, E value) {
		try (var __ = write(key)){
			return m.put(key, value);
		}
	}

	@Override  @Deprecated
	public E remove (Object key) {
		return remove(((Long)key).longValue());
	}
	@Override
	public E remove (long key) {
		try (var __ = write(key)){
			return m.remove(key);
		}
	}

	/** @see NonBlockingHashMapLong#putAll */
	@Override
	public void putAll (Map<? extends Long,? extends E> fromMap) {
		for (var e : m.entrySet())
				put(e.getKey(), e.getValue());
	}

	/** @see NonBlockingHashMapLong.IteratorLong */
	public static class StripedLongIterator implements LongIterator {
		private final StripedNonBlockingHashMapLong<?> owner;
		private final NonBlockingHashMapLong<?>.IteratorLong it;
		private long seenKey;// ^ safe for concurrent

		public StripedLongIterator (StripedNonBlockingHashMapLong<?> owner) {
			this.owner = owner;
			it = (NonBlockingHashMapLong<?>.IteratorLong) owner.m.keys();
		}//new

		/** Remove last key returned by {@link #next} or {@link #nextLong}. */
		@Override
		public void remove () {
			try (var __ = owner.write(seenKey)){
				it.remove();
			}
		}
		/** <strong>Auto-box</strong> and return the next key. */
		@Override @Deprecated public Long next (){ return nextLong(); }
		/** Return the next key as a primitive {@code long}. */
		@Override
		public long nextLong () {
			seenKey = it.nextLong();
			return seenKey;
		}
		/** True if there are more keys to iterate over. */
		@Override public boolean hasNext (){ return it.hasNext(); }
	}//StripedLongIterator

	public StripedLongIterator keys (){ return new StripedLongIterator(this); }

	public void forEachKey (LongConsumer action) {
		var it = (NonBlockingHashMapLong<E>.IteratorLong) m.keys();
		try {
			while (it.hasNext())
					action.accept(it.nextLong());
		} catch (CancellationException ignored){}
	}

	@Override
	public LongSet keySet () {
		throw new UnsupportedOperationException("keySet");
	}
	public long[] keySetLong (){ return m.keySetLong(); }

	/** @see NonBlockingHashMapLong#values()  */
	@Override
	public ObjectCollection<E> values () {
		throw new UnsupportedOperationException();
	}

	@Override
	public ObjectSet<Map.Entry<Long,E>> entrySet () {
		throw new UnsupportedOperationException("entrySet");
	}

	@Override
	public E merge (Long key, E value, BiFunction<? super E,? super E,? extends E> remappingFunction) {
		try (var __ = write(key)){
			return m.merge(key, value, remappingFunction);
		}
	}

	@Override
	public E compute (Long key, BiFunction<? super Long,? super E,? extends E> remappingFunction) {
		try (var __ = write(key)){
			return m.compute(key, remappingFunction);
		}
	}

	@Override
	public E computeIfPresent (Long key, BiFunction<? super Long,? super E,? extends E> remappingFunction) {
		try (var __ = write(key)){
			return m.computeIfPresent(key, remappingFunction);
		}
	}

	@Override
	public E computeIfAbsent (Long key, Function<? super Long,? extends E> mappingFunction) {
		try (var __ = write(key)){
			return m.computeIfAbsent(key, mappingFunction);
		}
	}

	@Override
	public void replaceAll (BiFunction<? super Long,? super E,? extends E> function) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void forEach (BiConsumer<? super Long,? super E> action) {
		m.forEach(action);
	}

	@Override  @Deprecated
	public E getOrDefault (Object key, E defaultValue) {
		return m.getOrDefault(key, defaultValue);
	}
	@Override
	public E getOrDefault (long key, E defaultValue) {
		E v;
		return (v = get(key)) != null ? v : defaultValue;
	}

	@Override
	public void defaultReturnValue (E rv) {
		throw new UnsupportedOperationException();
	}

	@Override public @Nullable E defaultReturnValue (){ return null; }

	@Override
	public ObjectSet<Long2ObjectMap.Entry<E>> long2ObjectEntrySet () {
		throw new UnsupportedOperationException();
	}

	public <R> R withLock (long key, Function<Long2ObjectMap.Entry<E>,R> withLock) {
		try (var __ = write(key)){
			var x = new Long2ObjectMap.Entry<E>() {
				@Override public long getLongKey (){ return key; }
				@Override public E getValue (){ return m.get(key); }// can be changed inside withLock
				@Override public E setValue (E value){
					return value != null ? m.put(key, value)
							: m.remove(key);
				}
			};
			return withLock.apply(x);
		}
	}


	public abstract static class NBHMLCacheExpirer<E> implements LongConsumer, Function<Long2ObjectMap.Entry<E>,Object>{
		protected final StripedNonBlockingHashMapLong<E> cacheMap;

		public NBHMLCacheExpirer (StripedNonBlockingHashMapLong<E> cacheMap){ this.cacheMap = cacheMap; }//new

		/** Heuristic: {@link #expire()} is called from a single thread (scheduler) when working correctly: Atomic/volatile is not needed */
		protected long expiredCount;

		/** Processing of a deleted (evicted, expired) entry  */
		protected void postProcessExpiredEntry (long key, E value){}

		/** ~ value.getDeadline() ≤ now */
		protected abstract boolean isExpired (long key, E value);

		protected void beforeExpire () {
			// now = System.nanoTime();
		}

		/**
		 Example: how to make "true cache" with expiration.

		 https://github.com/JCTools/JCTools/blob/master/jctools-core/src/main/java/org/jctools/maps/NonBlockingHashMapLong.java
		 https://stackoverflow.com/questions/61721386/caffeine-cache-specify-expiry-for-an-entry
		 @see com.github.benmanes.caffeine.cache.Cache#policy()
		 @see com.github.benmanes.caffeine.cache.Policy#expireVariably()

		 @see org.springframework.scheduling.annotation.Scheduled
		 @see java.util.Set#removeIf
		*/
		public void expire () {
			long initialExpiredCount = expiredCount;
			beforeExpire();
			cacheMap.forEachKey(this);// see accept(long longKey)
			reportExpireResult(initialExpiredCount);
		}

		/** @see #forEachKey(LongConsumer) */
		@Override
		public void accept (long longKey) {
			E value = cacheMap.get(longKey);// can be gone already
			if (value != null && isExpired(longKey, value))// first "light" check
					cacheMap.withLock(longKey, this);
		}//LongConsumer#accept for expire → forEachKey

		/** @see #withLock(long, Function)  */
		@Override
		public @Nullable Object apply (Long2ObjectMap.Entry<E> e) {
			if (e.getValue() != null && isExpired(e.getLongKey(), e.getValue())){// double check idiom
				expiredCount++;
				postProcessExpiredEntry(e.getLongKey(), e.getValue());
				e.setValue(null);// expired ⇒ remove
			}
			return null;
		}//Function#apply for LongConsumer#accept → withLock

		private void reportExpireResult (long initialExpiredCount) {
			//LOGGER.debug("{}.expire entries: {} / {}, expiredSinceStart: {}", beanName, expiredCount - initialExpiredCount, cacheMap.size(), expiredCount);
		}
	}
}