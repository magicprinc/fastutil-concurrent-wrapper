package com.trivago.fastutilconcurrentwrapper.intkey;

import com.trivago.fastutilconcurrentwrapper.util.CloseableLock;
import com.trivago.fastutilconcurrentwrapper.util.PaddedReadWriteLock;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectSortedSet;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.ObjIntConsumer;

/**
 @see java.util.Collections#synchronizedMap(Map)
 @see java.util.LinkedHashMap
 @see it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap
*/
public class SynchronizedInt2ObjLinkedHashMap<V> implements Int2ObjectSortedMap<V> {
	protected final Int2ObjectLinkedOpenHashMap<V> m;
	protected final PaddedReadWriteLock lock = new PaddedReadWriteLock();

	public SynchronizedInt2ObjLinkedHashMap (int expected, float f) {
		m = new Int2ObjectLinkedOpenHashMap<>(expected, f);
	}//new

	public SynchronizedInt2ObjLinkedHashMap () {
		m = new Int2ObjectLinkedOpenHashMap<>();
	}//new

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

	public int[] keyArray () {
		try (var __ = read()){ return m.keySet().toIntArray(); }
	}

	public V[] valueArray (V[] valueArray) {
		try (var __ = read()){ return m.values().toArray(valueArray); }
	}

	public void forEachKeyRead (IntConsumer action) {
		try (var __ = read()){ m.keySet().forEach(action); }
	}

	public void forEachValueRead (Consumer<V> action) {
		try (var __ = read()){ m.values().forEach(action); }
	}

	public void forEachEntryRead (ObjIntConsumer<V> action) {
		try (var __ = read()){
			for (var e : m.int2ObjectEntrySet())
				action.accept(e.getValue(), e.getIntKey());
		}
	}

	@Override
	public int firstIntKey () {
		try (var __ = read()){ return m.firstIntKey(); }
	}

	@Override
	public int lastIntKey () {
		try (var __ = read()){ return m.lastIntKey(); }
	}

	@Override
	public boolean containsKey (int key) {
		try (var __ = read()){ return m.containsKey(key); }
	}

	@Override
	public V get (int key) {
		try (var __ = read()){ return m.get(key); }
	}

	@Override
	public boolean containsValue (Object value) {
		try (var __ = read()){ return m.containsValue(value); }
	}

	@Override public IntComparator comparator (){ return m.comparator(); }

	/** Full copy ~ snapshot! */
	@Override
	public IntSortedSet keySet () {
		try (var __ = read()){
			return new IntLinkedOpenHashSet(m.keySet());
		}
	}

	/** Full copy ~ snapshot! */
	@Override
	public ObjectCollection<V> values () {
		try (var __ = read()){
			return new ObjectArrayList<>(m.values());
		}
	}

	@Override public void defaultReturnValue (V rv){ throw new UnsupportedOperationException(); }
	@Override public @Nullable V defaultReturnValue (){ return null; }
	@Override public Int2ObjectSortedMap<V> subMap (int fromKey, int toKey){ throw new UnsupportedOperationException(); }
	@Override public Int2ObjectSortedMap<V> headMap (int toKey){ throw new UnsupportedOperationException(); }
	@Override public Int2ObjectSortedMap<V> tailMap (int fromKey){ throw new UnsupportedOperationException(); }
	@Override public ObjectSortedSet<Int2ObjectMap.Entry<V>> int2ObjectEntrySet (){ throw new UnsupportedOperationException(); }

	public void forEachKeyWrite (IntConsumer action) {
		try (var __ = write()){ m.keySet().forEach(action); }
	}

	public void forEachValueWrite (Consumer<V> action) {
		try (var __ = write()){ m.values().forEach(action); }
	}

	public void forEachEntryWrite (ObjIntConsumer<V> action) {
		try (var __ = write()){
			for (var e : m.int2ObjectEntrySet())
				action.accept(e.getValue(), e.getIntKey());
		}
	}

	public <R> R withWriteLock (Function<Int2ObjectLinkedOpenHashMap<V>,R> exclusiveAccess) {
		try (var __ = write()){
			return exclusiveAccess.apply(m);
		}
	}

	@Override
	public void putAll (Map<? extends Integer,? extends V> from) {
		try (var __ = write()){ m.putAll(from); }
	}

	@Override
	public void clear () {
		try (var __ = write()){ m.clear(); }
	}

	@Override
	public V putIfAbsent (int key, V value) {
		try (var __ = write()){ return m.putIfAbsent(key, value); }
	}

	@Override
	public boolean remove (int key, Object value) {
		try (var __ = write()){ return m.remove(key, value); }
	}

	@Override
	public boolean replace (int key, V oldValue, V newValue) {
		try (var __ = write()){ return m.replace(key, oldValue, newValue); }
	}

	@Override
	public V replace (int key, V value) {
		try (var __ = write()){ return m.replace(key, value); }
	}

	@Override
	public V computeIfAbsent (int key, IntFunction<? extends V> mappingFunction) {
		try (var __ = write()){ return m.computeIfAbsent(key, mappingFunction); }
	}

	@Override
	public V computeIfAbsent (int key, Int2ObjectFunction<? extends V> mappingFunction) {
		try (var __ = write()){ return m.computeIfAbsent(key, mappingFunction); }
	}

	@Override
	public V computeIfPresent (int key, BiFunction<? super Integer,? super V,? extends V> remappingFunction) {
		try (var __ = write()){ return m.computeIfPresent(key, remappingFunction); }
	}

	@Override
	public V compute (int key, BiFunction<? super Integer,? super V,? extends V> remappingFunction) {
		try (var __ = write()){ return m.compute(key, remappingFunction); }
	}

	@Override
	public V merge (int key, V value, BiFunction<? super V,? super V,? extends V> remappingFunction) {
		try (var __ = write()){ return m.merge(key, value, remappingFunction); }
	}

	@Override
	public V put (int key, V value) {
		try (var __ = write()){ return m.put(key, value); }
	}
}