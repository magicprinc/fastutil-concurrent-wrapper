package com.trivago.fastutilconcurrentwrapper.intkey;

import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectSet;

import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiFunction;
import java.util.function.IntFunction;

/**
 @see java.util.Collections#synchronizedMap(Map)
 @see java.util.LinkedHashMap
 @see it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap
*/
public class SynchronizedInt2ObjLinkedHashMap<V> implements Int2ObjectMap<V> {
	final Int2ObjectLinkedOpenHashMap<V> m;
	final ReadWriteLock lock = new ReentrantReadWriteLock();

	public SynchronizedInt2ObjLinkedHashMap (int expected, float f) {
		m = new Int2ObjectLinkedOpenHashMap<>(expected, f);
	}//new

	public SynchronizedInt2ObjLinkedHashMap () {
		m = new Int2ObjectLinkedOpenHashMap<>();
	}//new

	@Override
	public int size () {
		return 0;
	}

	@Override
	public void defaultReturnValue (V rv) {

	}

	@Override
	public V defaultReturnValue () {
		return null;
	}

	@Override
	public ObjectSet<Entry<V>> int2ObjectEntrySet () {
		return null;
	}

	@Override
	public IntSet keySet () {
		return null;
	}

	@Override
	public ObjectCollection<V> values () {
		return null;
	}

	@Override
	public boolean containsKey (int key) {
		return false;
	}

	@Override
	public V get (int key) {
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
	public void putAll (Map<? extends Integer,? extends V> m) {

	}

	@Override
	public void clear () {
		Int2ObjectMap.super.clear();
	}

	@Override
	public boolean remove (int key, Object value) {
		return Int2ObjectMap.super.remove(key, value);
	}

	@Override
	public boolean replace (int key, V oldValue, V newValue) {
		return Int2ObjectMap.super.replace(key, oldValue, newValue);
	}

	@Override
	public V replace (int key, V value) {
		return Int2ObjectMap.super.replace(key, value);
	}

	@Override
	public V computeIfAbsent (int key, IntFunction<? extends V> mappingFunction) {
		return Int2ObjectMap.super.computeIfAbsent(key, mappingFunction);
	}

	@Override
	public V computeIfAbsent (int key, Int2ObjectFunction<? extends V> mappingFunction) {
		return Int2ObjectMap.super.computeIfAbsent(key, mappingFunction);
	}

	@Override
	public V computeIfPresent (int key, BiFunction<? super Integer,? super V,? extends V> remappingFunction) {
		return Int2ObjectMap.super.computeIfPresent(key, remappingFunction);
	}

	@Override
	public V compute (int key, BiFunction<? super Integer,? super V,? extends V> remappingFunction) {
		return Int2ObjectMap.super.compute(key, remappingFunction);
	}

	@Override
	public V merge (int key, V value, BiFunction<? super V,? super V,? extends V> remappingFunction) {
		return Int2ObjectMap.super.merge(key, value, remappingFunction);
	}
}