package com.trivago.fastutilconcurrentwrapper.intkey;

import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class SynchronizedInt2ObjLinkedHashMapTest {
	private SynchronizedInt2ObjLinkedHashMap<String> map;

	@BeforeEach
	void setUp() {
		map = new SynchronizedInt2ObjLinkedHashMap<>();
	}

	@Test
	void testConstructorWithParameters() {
		SynchronizedInt2ObjLinkedHashMap<String> customMap =
			new SynchronizedInt2ObjLinkedHashMap<>(100, 0.75f);
		assertNotNull(customMap);
		assertEquals(0, customMap.size());
	}

	@Test
	void testSize() {
		assertEquals(0, map.size());
		map.put(1, "one");
		assertEquals(1, map.size());
	}

	@Test
	void testIsEmpty() {
		assertTrue(map.isEmpty());
		map.put(1, "one");
		assertFalse(map.isEmpty());
	}

	@Test
	void testKeyArray() {
		map.put(1, "one");
		map.put(2, "two");
		int[] keys = map.keyArray();
		assertEquals(2, keys.length);
		assertTrue(keys[0] == 1 || keys[1] == 1);
		assertTrue(keys[0] == 2 || keys[1] == 2);
	}

	@Test
	void testValueArray() {
		map.put(1, "one");
		map.put(2, "two");
		String[] values = map.valueArray(new String[0]);
		assertEquals(2, values.length);
		assertTrue(values[0].equals("one") || values[1].equals("one"));
		assertTrue(values[0].equals("two") || values[1].equals("two"));
	}

	@Test
	void testForEachKey() {
		map.put(1, "one");
		map.put(2, "two");
		AtomicInteger count = new AtomicInteger(0);
		map.forEachKey(key -> count.incrementAndGet());
		assertEquals(2, count.get());
	}

	@Test
	void testForEachValue() {
		map.put(1, "one");
		map.put(2, "two");
		AtomicInteger count = new AtomicInteger(0);
		map.forEachValue(value -> count.incrementAndGet());
		assertEquals(2, count.get());
	}

	@Test
	void testForEachEntry() {
		map.put(1, "one");
		map.put(2, "two");
		AtomicInteger sum = new AtomicInteger(0);
		map.forEachEntry((value, key) -> sum.addAndGet(key));
		assertEquals(3, sum.get());
	}

	@Test
	void testFirstIntKey() {
		map.put(1, "one");
		map.put(2, "two");
		assertEquals(1, map.firstIntKey());
	}

	@Test
	void testLastIntKey() {
		map.put(1, "one");
		map.put(2, "two");
		assertEquals(2, map.lastIntKey());
	}

	@Test
	void testContainsKey() {
		assertFalse(map.containsKey(1));
		map.put(1, "one");
		assertTrue(map.containsKey(1));
	}

	@Test
	void testGet() {
		assertNull(map.get(1));
		map.put(1, "one");
		assertEquals("one", map.get(1));
	}

	@Test
	void testContainsValue() {
		assertFalse(map.containsValue("one"));
		map.put(1, "one");
		assertTrue(map.containsValue("one"));
	}

	@Test
	void testToString() {
		map.put(1, "one");
		assertNotNull(map.toString());
		assertEquals("{1=>one}", map.toString());
	}

	@Test
	void testHashCode() {
		map.put(1, "one");
		int hashCode = map.hashCode();
		assertNotEquals(0, hashCode);
	}

	@Test
	void testEquals() {
		map.put(1, "one");

		// Test equality with same instance
		assertTrue(map.equals(map));

		// Test equality with different type
		assertFalse(map.equals("not a map"));

		// Test equality with equivalent map
		SynchronizedInt2ObjLinkedHashMap<String> otherMap = new SynchronizedInt2ObjLinkedHashMap<>();
		otherMap.put(1, "one");
		assertTrue(map.equals(otherMap));

		// Test inequality
		otherMap.put(2, "two");
		assertFalse(map.equals(otherMap));
	}

	@Test
	void testKeySet() {
		map.put(1, "one");
		map.put(2, "two");
		IntSortedSet keySet = map.keySet();
		assertInstanceOf(IntLinkedOpenHashSet.class, keySet);
		assertEquals(2, keySet.size());
		assertTrue(keySet.contains(1));
		assertTrue(keySet.contains(2));
	}

	@Test
	void testValues() {
		map.put(1, "one");
		map.put(2, "two");
		ObjectArrayList<String> values = map.values();
		assertEquals(2, values.size());
		assertTrue(values.contains("one"));
		assertTrue(values.contains("two"));
	}

	@Test
	void testDefaultReturnValue() {
		assertNull(map.defaultReturnValue());
		assertThrows(UnsupportedOperationException.class, () -> map.defaultReturnValue("default"));
	}

	@Test
	void testForEachKeyWrite() {
		map.put(1, "one");
		map.put(2, "two");
		AtomicInteger count = new AtomicInteger(0);
		map.forEachKeyWrite(key -> count.incrementAndGet());
		assertEquals(2, count.get());
	}

	@Test
	void testForEachValueWrite() {
		map.put(1, "one");
		map.put(2, "two");
		AtomicInteger count = new AtomicInteger(0);
		map.forEachValueWrite(value -> count.incrementAndGet());
		assertEquals(2, count.get());
	}

	@Test
	void testForEachEntryWrite() {
		map.put(1, "one");
		map.put(2, "two");
		AtomicInteger sum = new AtomicInteger(0);
		map.forEachEntryWrite((value, key) -> sum.addAndGet(key));
		assertEquals(3, sum.get());
	}

	@Test
	void testWithWriteLock() {
		String result = map.withWriteLock(m -> {
			m.put(1, "one");
			return m.get(1);
		});
		assertEquals("one", result);
		assertEquals("one", map.get(1));
	}

	@Test
	void testPutAll() {
		Map<Integer, String> input = new HashMap<>();
		input.put(1, "one");
		input.put(2, "two");
		map.putAll(input);
		assertEquals(2, map.size());
		assertEquals("one", map.get(1));
		assertEquals("two", map.get(2));
	}

	@Test
	void testClear() {
		map.put(1, "one");
		map.clear();
		assertEquals(0, map.size());
		assertTrue(map.isEmpty());
	}

	@Test
	void testPutIfAbsent() {
		assertNull(map.putIfAbsent(1, "one"));
		assertEquals("one", map.putIfAbsent(1, "two"));
		assertEquals("one", map.get(1));
	}

	@Test
	void testRemove() {
		map.put(1, "one");
		assertFalse(map.remove(1, "two"));
		assertTrue(map.remove(1, "one"));
		assertFalse(map.containsKey(1));
	}

	@Test
	void testReplace() {
		map.put(1, "one");
		assertFalse(map.replace(1, "two", "three"));
		assertTrue(map.replace(1, "one", "three"));
		assertEquals("three", map.get(1));
	}

	@Test
	void testReplaceSingleArg() {
		map.put(1, "one");
		assertEquals("one", map.replace(1, "two"));
		assertEquals("two", map.get(1));
	}

	@Test
	void testComputeIfAbsentWithIntFunction() {
		assertEquals("one", map.computeIfAbsent(1, k -> "one"));
		assertEquals("one", map.computeIfAbsent(1, k -> "two"));
	}

	@Test
	void testComputeIfAbsentWithInt2ObjectFunction() {
		Int2ObjectFunction<String> mappingFunction = k -> "one";
		assertEquals("one", map.computeIfAbsent(1, mappingFunction));
		assertEquals("one", map.computeIfAbsent(1, mappingFunction));
	}

	@Test
	void testComputeIfPresent() {
		assertNull(map.computeIfPresent(1, (k, v) -> v + "!"));
		map.put(1, "one");
		assertEquals("one!", map.computeIfPresent(1, (k, v) -> v + "!"));
	}

	@Test
	void testCompute() {
		assertEquals("one", map.compute(1, (k, v) -> "one"));
		assertEquals("one!", map.compute(1, (k, v) -> v + "!"));
	}

	@Test
	void testMerge() {
		assertEquals("one", map.merge(1, "one", (oldVal, newVal) -> oldVal + newVal));
		assertEquals("onetwo", map.merge(1, "two", (oldVal, newVal) -> oldVal + newVal));
	}

	@Test
	void testPut() {
		assertNull(map.put(1, "one"));
		assertEquals("one", map.put(1, "two"));
	}

	@Test
	void testRemoveDeprecated() {
		map.put(1, "one");
		assertEquals("one", map.remove(1));
		assertNull(map.get(1));
	}

	@Test
	void testConcurrentAccess() throws InterruptedException {
		int threadCount = 10;
		int iterations = 1000;
		var executor = Executors.newFixedThreadPool(threadCount);
		try {
			var latch = new CountDownLatch(threadCount);

			for (int i = 0; i < threadCount; i++){
				final int threadId = i;
				executor.execute(()->{
					try {
						for (int j = 0; j < iterations; j++){
							int key = threadId * iterations + j;
							map.put(key, "value-" + key);
							assertEquals("value-" + key, map.get(key));
							map.remove(key);
						}
					} finally {
						latch.countDown();
					}
				});
			}

			latch.await(5, TimeUnit.SECONDS);
			executor.shutdown();
			assertTrue(executor.awaitTermination(1, TimeUnit.SECONDS));
			assertEquals(0, map.size());
		} finally {
			executor.shutdownNow();
		}
	}

	@Test
	void testUnsupportedOperations() {
		assertThrows(UnsupportedOperationException.class, () -> map.subMap(1, 2));
		assertThrows(UnsupportedOperationException.class, () -> map.headMap(1));
		assertThrows(UnsupportedOperationException.class, () -> map.tailMap(1));
	}
}