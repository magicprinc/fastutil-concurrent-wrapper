package com.trivago.fastutilconcurrentwrapper.intkey;

import com.trivago.fastutilconcurrentwrapper.PrimitiveConcurrentMap;
import com.trivago.fastutilconcurrentwrapper.PrimitiveMapBuilder;
import it.unimi.dsi.fastutil.ints.Int2LongFunction;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;

import java.util.function.BiFunction;

public class ConcurrentIntLongMap extends PrimitiveConcurrentMap<Integer,Long> {
	protected final Int2LongOpenHashMap[] maps;
	protected final long defaultValue;

	public ConcurrentIntLongMap (
		int numBuckets,
		int initialCapacity,
		float loadFactor,
		long defaultValue
	){
		super(numBuckets);

		this.maps = new Int2LongOpenHashMap[numBuckets];
		this.defaultValue = defaultValue;
		for (int i = 0; i < numBuckets; i++)
				maps[i] = new Int2LongOpenHashMap(initialCapacity, loadFactor);
	}

	@Override protected Int2LongOpenHashMap mapAt (int index){ return maps[index]; }

	public boolean containsKey (int key) {
		int bucket = getBucket(key);
		try (var __ = readAt(bucket)){
			return maps[bucket].containsKey(key);
		}
	}

	public long get (int intKey) {
		int bucket = getBucket(intKey);
		try (var __ = readAt(bucket)){
			return maps[bucket].getOrDefault(intKey, defaultValue);
		}
	}

	public long put (int intKey, long value) {
		int bucket = getBucket(intKey);
		try (var __ = writeAt(bucket)){
			return maps[bucket].put(intKey, value);
		}
	}

	public long getDefaultValue (){ return defaultValue; }

	public long remove (int intKey) {
		int bucket = getBucket(intKey);
		try (var __ = writeAt(bucket)){
			return maps[bucket].remove(intKey);
		}
	}

	public boolean remove (int key, long value) {
		int bucket = getBucket(key);
		try (var __ = writeAt(bucket)){
			return maps[bucket].remove(key, value);
		}
	}

	public long computeIfAbsent (int key, Int2LongFunction mappingFunction) {
		int bucket = getBucket(key);
		try (var __ = writeAt(bucket)){
			return maps[bucket].computeIfAbsent(key, mappingFunction);
		}
	}

	public long computeIfPresent(int key, BiFunction<Integer, Long, Long> mappingFunction) {
		int bucket = getBucket(key);
		try (var __ = writeAt(bucket)){
			return maps[bucket].computeIfPresent(key, mappingFunction);
		}
	}

	public static PrimitiveMapBuilder<ConcurrentIntLongMap,Long> newBuilder () {
		return new PrimitiveMapBuilder<>(){
			@Override
			public ConcurrentIntLongMap build () {
				long def = super.defaultValue != null ? super.defaultValue : 0;
				return switch (mapMode){
					case BUSY_WAITING -> new ConcurrentBusyWaitingIntLongMap(buckets, initialCapacity, loadFactor, def);
					case BLOCKING -> new ConcurrentIntLongMap(buckets, initialCapacity, loadFactor, def);
				};
			}
		};
	}
}