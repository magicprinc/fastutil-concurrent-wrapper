package com.trivago.fastutilconcurrentwrapper.objkey;

import com.trivago.fastutilconcurrentwrapper.util.CloseableLock;
import com.trivago.fastutilconcurrentwrapper.util.PaddedReadWriteLock;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectFunction;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectSortedMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSortedSet;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 @see java.util.Collections#synchronizedMap(Map)
 @see java.util.LinkedHashMap
 @see Int2ObjectLinkedOpenHashMap
*/
public class SynchronizedObj2ObjLinkedHashMap<K,V> implements Object2ObjectSortedMap<K,V> {
	protected final Object2ObjectLinkedOpenHashMap<K,V> m;
	protected final PaddedReadWriteLock lock = new PaddedReadWriteLock();

	public SynchronizedObj2ObjLinkedHashMap (int expected, float f) {
		m = new Object2ObjectLinkedOpenHashMap<>(expected, f);
	}//new

	public SynchronizedObj2ObjLinkedHashMap (){ m = new Object2ObjectLinkedOpenHashMap<>(); }//new

	protected CloseableLock read () {
		return lock.read();
	}
	protected CloseableLock write () {
		return lock.write();
	}

	@Override
	public int size () {
		try (var __ = read()){ return m.size(); }
	}
	@Override
	public boolean isEmpty () {
		try (var __ = read()){ return m.isEmpty(); }
	}

	public K[] keyArray (K[] keyArray) {
		try (var __ = read()){ return m.keySet().toArray(keyArray); }
	}

	public V[] valueArray (V[] valueArray) {
		try (var __ = read()){ return m.values().toArray(valueArray); }
	}

	public void forEachKeyRead (Consumer<K> action) {
		try (var __ = read()){ m.keySet().forEach(action); }
	}

	public void forEachValueRead (Consumer<V> action) {
		try (var __ = read()){ m.values().forEach(action); }
	}

	public void forEachEntryRead (BiConsumer<K,V> action) {
		try (var __ = read()){
			m.forEach(action);
		}
	}

	@Override
	public K firstKey () {
		try (var __ = read()){ return m.firstKey(); }
	}

	@Override
	public K lastKey () {
		try (var __ = read()){ return m.lastKey(); }
	}

	@Override
	public boolean containsKey (Object key) {
		try (var __ = read()){ return m.containsKey(key); }
	}

	@Override
	public V get (Object key) {
		try (var __ = read()){ return m.get(key); }
	}

	@Override
	public boolean containsValue (Object value) {
		try (var __ = read()){ return m.containsValue(value); }
	}

	@Override public Comparator<? super K> comparator (){ return m.comparator(); }

	/** Full copy ~ snapshot! */
	@Override
	public ObjectSortedSet<K> keySet () {
		try (var __ = read()){
			return new ObjectLinkedOpenHashSet<>(m.keySet());
		}
	}

	/** Full copy ~ snapshot! */
	@Override
	public ObjectArrayList<V> values () {
		try (var __ = read()){
			return new ObjectArrayList<>(m.values());
		}
	}

	@Override public void defaultReturnValue (V rv){ throw new UnsupportedOperationException(); }
	@Override public @Nullable V defaultReturnValue (){ return null; }
	@Override public Object2ObjectSortedMap<K,V> subMap (K fromKey, K toKey){ throw new UnsupportedOperationException(); }
	@Override public Object2ObjectSortedMap<K,V> headMap (K toKey){ throw new UnsupportedOperationException(); }
	@Override public Object2ObjectSortedMap<K,V> tailMap (K fromKey){ throw new UnsupportedOperationException(); }
	@Override public ObjectSortedSet<Object2ObjectMap.Entry<K,V>> object2ObjectEntrySet (){ throw new UnsupportedOperationException(); }

	public void forEachKeyWrite (Consumer<K> action) {
		try (var __ = write()){ m.keySet().forEach(action); }
	}

	public void forEachValueWrite (Consumer<V> action) {
		try (var __ = write()){ m.values().forEach(action); }
	}

	public void forEachEntryWrite (BiConsumer<K,V> action) {
		try (var __ = write()){
			m.forEach(action);
		}
	}

	public <R> R withWriteLock (Function<Object2ObjectLinkedOpenHashMap<K,V>,R> exclusiveAccess) {
		try (var __ = write()){
			return exclusiveAccess.apply(m);
		}
	}

	@Override
	public void putAll (Map<? extends K,? extends V> from) {
		try (var __ = write()){ m.putAll(from); }
	}

	@Override
	public void clear () {
		try (var __ = write()){ m.clear(); }
	}

	@Override
	public V putIfAbsent (K key, V value) {
		try (var __ = write()){ return m.putIfAbsent(key, value); }
	}

	@Override
	public boolean remove (Object key, Object value) {
		try (var __ = write()){ return m.remove(key, value); }
	}

	@Override
	public boolean replace (K key, V oldValue, V newValue) {
		try (var __ = write()){ return m.replace(key, oldValue, newValue); }
	}

	@Override
	public V replace (K key, V value) {
		try (var __ = write()){ return m.replace(key, value); }
	}

	@Override
	public V computeIfAbsent (K key, Object2ObjectFunction<? super K, ? extends V> mappingFunction) {
		try (var __ = write()){ return m.computeIfAbsent(key, mappingFunction); }
	}

	@Override
	public V computeIfPresent (K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		try (var __ = write()){ return m.computeIfPresent(key, remappingFunction); }
	}

	@Override
	public V compute (K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		try (var __ = write()){ return m.compute(key, remappingFunction); }
	}

	@Override
	public V merge (K key, V value, BiFunction<? super V,? super V,? extends V> remappingFunction) {
		try (var __ = write()){ return m.merge(key, value, remappingFunction); }
	}

	@Override
	public V put (K key, V value) {
		try (var __ = write()){ return m.put(key, value); }
	}
}