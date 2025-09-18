package com.trivago.fastutilconcurrentwrapper.intkey;

import it.unimi.dsi.fastutil.ints.Int2LongFunction;

import java.util.concurrent.locks.Lock;
import java.util.function.BiFunction;

public class ConcurrentBusyWaitingIntLongMap extends ConcurrentIntLongMap {
	public ConcurrentBusyWaitingIntLongMap (
		int numBuckets,
		int initialCapacity,
		float loadFactor,
		long defaultValue
	){
		super(numBuckets, initialCapacity, loadFactor, defaultValue);
	}

	@Override
	public boolean containsKey (int key) {
		int bucket = getBucket(key);
		Lock readLock = readLock(bucket);

		while (true){
			if (readLock.tryLock()){
				try {
					return maps[bucket].containsKey(key);
				} finally {
					readLock.unlock();
				}
			}
			Thread.onSpinWait();
		}
	}

	@Override
	public long get (int intKey) {
		int bucket = getBucket(intKey);
		Lock readLock = readLock(bucket);

		while (true){
			if (readLock.tryLock()){
				try {
					return maps[bucket].getOrDefault(intKey, defaultValue);
				} finally {
					readLock.unlock();
				}
			}
			Thread.onSpinWait();
		}
	}

	@Override
	public long put (int key, long value) {
		int bucket = getBucket(key);
		Lock writeLock = writeLock(bucket);

		while (true){
			if (writeLock.tryLock()){
				try {
					return maps[bucket].put(key, value);
				} finally {
					writeLock.unlock();
				}
			}
			Thread.onSpinWait();
		}
	}

	@Override
	public long remove (int key) {
		int bucket = getBucket(key);
		Lock writeLock = writeLock(bucket);

		while (true){
			if (writeLock.tryLock()){
				try {
					return maps[bucket].remove(key);
				} finally {
					writeLock.unlock();
				}
			}
			Thread.onSpinWait();
		}
	}

	@Override
	public boolean remove (int key, long value) {
		int bucket = getBucket(key);
		Lock writeLock = writeLock(bucket);

		while (true){
			if (writeLock.tryLock()){
				try {
					return maps[bucket].remove(key, value);
				} finally {
					writeLock.unlock();
				}
			}
			Thread.onSpinWait();
		}
	}

	@Override
	public long computeIfAbsent (int key, Int2LongFunction mappingFunction) {
		int bucket = getBucket(key);
		Lock writeLock = writeLock(bucket);

		while (true){
			if (writeLock.tryLock()){
				try {
					return maps[bucket].computeIfAbsent(key, mappingFunction);
				} finally {
					writeLock.unlock();
				}
			}
			Thread.onSpinWait();
		}
	}

	@Override
	public long computeIfPresent(int key, BiFunction<Integer, Long, Long> mappingFunction) {
		int bucket = getBucket(key);
		Lock writeLock = writeLock(bucket);

		while (true){
			if (writeLock.tryLock()){
				try {
					return maps[bucket].computeIfPresent(key, mappingFunction);
				} finally {
					writeLock.unlock();
				}
			}
			Thread.onSpinWait();
		}
	}
}