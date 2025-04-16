package com.trivago.fastutilconcurrentwrapper.objkey;

import com.trivago.fastutilconcurrentwrapper.util.CloseableLock;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectFunction;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiFunction;

/**
 @see java.util.Collections#synchronizedMap(Map)
 @see java.util.LinkedHashMap
 @see Int2ObjectLinkedOpenHashMap
*/
public class SynchronizedObj2ObjLinkedHashMap<K,V> implements Object2ObjectMap<K, V> {
	final Object2ObjectLinkedOpenHashMap<K,V> m;
	final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	public SynchronizedObj2ObjLinkedHashMap (int expected, float f) {
		m = new Object2ObjectLinkedOpenHashMap<>(expected, f);
	}//new

	public SynchronizedObj2ObjLinkedHashMap (){ m = new Object2ObjectLinkedOpenHashMap<>(); }//new

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
		try (var __ = read()) {
			return m.size();
		}
	}

	@Override
	public void defaultReturnValue (V rv){
		throw new UnsupportedOperationException();
	}

	@Override public @Nullable V defaultReturnValue (){ return null; }

	@Override
	public ObjectSet<Entry<K,V>> object2ObjectEntrySet () {
		return null;
	}

	@Override
	public ObjectSet<K> keySet () {
		return null;
	}

	@Override
	public ObjectCollection<V> values () {
		return null;
	}

	@Override
	public boolean containsKey (Object key) {
		return false;
	}

	@Override
	public V get (Object key) {
		return null;
	}

	@Override
	public boolean isEmpty () {
		return false;
	}

	@Override
	public boolean containsValue (Object value) {
		return false;
	}

	@Override
	public void putAll (Map<? extends K,? extends V> m) {

	}

	@Override
	public void clear () {
		Object2ObjectMap.super.clear();
	}

	@Override
	public V put (K key, V value) {
		return Object2ObjectMap.super.put(key, value);
	}

	@Override
	public V remove (Object key) {
		return Object2ObjectMap.super.remove(key);
	}

	@Override
	public boolean replace (K key, V oldValue, V newValue) {
		return Object2ObjectMap.super.replace(key, oldValue, newValue);
	}

	@Override
	public boolean remove (Object key, Object value) {
		return Object2ObjectMap.super.remove(key, value);
	}

	@Override
	public V replace (K key, V value) {
		return Object2ObjectMap.super.replace(key, value);
	}

	@Override
	public V computeIfAbsent (K key, Object2ObjectFunction<? super K,? extends V> mappingFunction) {
		return Object2ObjectMap.super.computeIfAbsent(key, mappingFunction);
	}

	@Override
	public V computeIfPresent (K key, BiFunction<? super K,? super V,? extends V> remappingFunction) {
		return Object2ObjectMap.super.computeIfPresent(key, remappingFunction);
	}

	@Override
	public V compute (K key, BiFunction<? super K,? super V,? extends V> remappingFunction) {
		return Object2ObjectMap.super.compute(key, remappingFunction);
	}

	@Override
	public V merge (K key, V value, BiFunction<? super V,? super V,? extends V> remappingFunction) {
		return Object2ObjectMap.super.merge(key, value, remappingFunction);
	}
}