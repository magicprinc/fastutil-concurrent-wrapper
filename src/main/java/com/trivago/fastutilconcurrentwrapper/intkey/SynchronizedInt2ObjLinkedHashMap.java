package com.trivago.fastutilconcurrentwrapper.intkey;

import com.trivago.fastutilconcurrentwrapper.objkey.SynchronizedObj2ObjLinkedHashMap;
import com.trivago.fastutilconcurrentwrapper.util.CloseableLock;
import com.trivago.fastutilconcurrentwrapper.util.CloseableReadWriteLock;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSortedSet;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;
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
	protected final CloseableReadWriteLock lock = new CloseableReadWriteLock();

	public SynchronizedInt2ObjLinkedHashMap (int expected, float f) {
		m = new Int2ObjectLinkedOpenHashMap<>(expected, f);
	}//new

	public SynchronizedInt2ObjLinkedHashMap (){ m = new Int2ObjectLinkedOpenHashMap<>(); }//new

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

	public void forEachKey (IntConsumer action) {
		try (var __ = read()){ m.keySet().forEach(action); }
	}

	public void forEachValue (Consumer<V> action) {
		try (var __ = read()){ m.values().forEach(action); }
	}

	public void forEachEntry (ObjIntConsumer<V> action) {
		try (var __ = read()){
			for (var e : m.int2ObjectEntrySet())
				action.accept(e.getValue(), e.getIntKey());
		}
	}

	@Override
	public void forEach (BiConsumer<? super Integer,? super V> action) {
		try (var __ = read()){
			m.forEach(action);
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

	@Override  @Deprecated
	public V get (Object key) {
		try (var __ = read()){ return m.get(key); }
	}

	@Override  @Deprecated
	public boolean containsKey (Object key) {
		try (var __ = read()){ return m.containsKey(key); }
	}

	@Override
	public String toString () {
		try (var __ = read()){ return m.toString(); }
	}
	@Override
	public int hashCode () {
		try (var __ = read()){ return m.hashCode(); }
	}
	@Override
	public boolean equals (Object obj) {
		if (this == obj){ return true; }
		try (var __ = read()){
			return obj instanceof SynchronizedObj2ObjLinkedHashMap<?,?> x
					? m.equals(x)
					: m.equals(obj);
		}
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
	public ObjectArrayList<V> values () {
		try (var __ = read()){
			return new ObjectArrayList<>(m.values());
		}
	}

	/** Full copy ~ snapshot! */
	@Override
	public ObjectSortedSet<Int2ObjectMap.Entry<V>> int2ObjectEntrySet (){
		return new ObjectLinkedOpenHashSet<>(m.int2ObjectEntrySet());
	}

	@Override public void defaultReturnValue (V rv){ throw new UnsupportedOperationException(); }
	@Override public @Nullable V defaultReturnValue (){ return null; }
	@Override public Int2ObjectSortedMap<V> subMap (int fromKey, int toKey){ throw new UnsupportedOperationException(); }
	@Override public Int2ObjectSortedMap<V> headMap (int toKey){ throw new UnsupportedOperationException(); }
	@Override public Int2ObjectSortedMap<V> tailMap (int fromKey){ throw new UnsupportedOperationException(); }

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

	@Override
	public V remove (int key) {
		try (var __ = write()){ return m.remove(key); }
	}

	@Override  @Deprecated
	public V remove (Object key) {
		try (var __ = write()){ return m.remove(key); }
	}

	@Override  @Deprecated
	public V put (Integer key, V value) {
		try (var __ = write()){ return m.put(key, value); }
	}

	@Override
	public void replaceAll (BiFunction<? super Integer,? super V,? extends V> function) {
		try (var __ = write()){ m.replaceAll(function); }
	}

	@Override
	public V putIfAbsent (Integer key, V value) {
		try (var __ = write()){ return m.putIfAbsent(key, value); }
	}

	@Override
	public boolean remove (Object key, Object value) {
		try (var __ = write()){ return m.remove(key, value); }
	}

	@Override
	public boolean replace (Integer key, V oldValue, V newValue) {
		try (var __ = write()){ return m.replace(key, oldValue, newValue); }
	}

	@Override
	public V replace (Integer key, V value) {
		try (var __ = write()){ return m.replace(key, value); }
	}

	@Override
	public V computeIfAbsent (Integer key, Function<? super Integer,? extends V> mappingFunction) {
		try (var __ = write()){ return m.computeIfAbsent(key, mappingFunction); }
	}

	@Override
	public V computeIfPresent (Integer key, BiFunction<? super Integer,? super V,? extends V> remappingFunction) {
		try (var __ = write()){ return m.computeIfPresent(key, remappingFunction); }
	}

	@Override
	public V compute (Integer key, BiFunction<? super Integer,? super @Nullable V,? extends V> remappingFunction) {
		try (var __ = write()){ return m.compute(key, remappingFunction); }
	}

	@Override
	public V merge (Integer key, V value, BiFunction<? super V,? super V,? extends V> remappingFunction) {
		try (var __ = write()){ return m.merge(key, value, remappingFunction); }
	}
}