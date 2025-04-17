package com.trivago.fastutilconcurrentwrapper.objkey;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSortedSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/** @see SynchronizedObj2ObjLinkedHashMap */
class SynchronizedObj2ObjLinkedHashMapTest {
	private SynchronizedObj2ObjLinkedHashMap<String, Integer> map;

	@BeforeEach
	void setUp() {
		map = new SynchronizedObj2ObjLinkedHashMap<>();
	}

	@Test
	void testConstructorWithParameters() {
		SynchronizedObj2ObjLinkedHashMap<String, Integer> customMap =
			new SynchronizedObj2ObjLinkedHashMap<>(100, 0.75f);
		assertNotNull(customMap);
		assertEquals(0, customMap.size());
	}

	@Test
	void testSize() {
		assertEquals(0, map.size());
		map.put("one", 1);
		assertEquals(1, map.size());
	}

	@Test
	void testIsEmpty() {
		assertTrue(map.isEmpty());
		map.put("one", 1);
		assertFalse(map.isEmpty());
	}

	@Test
	void testKeyArray() {
		map.put("one", 1);
		map.put("two", 2);
		String[] keys = map.keyArray(new String[0]);
		assertEquals(2, keys.length);
		assertTrue(keys[0].equals("one") || keys[1].equals("one"));
		assertTrue(keys[0].equals("two") || keys[1].equals("two"));
	}

	@Test
	void testValueArray() {
		map.put("one", 1);
		map.put("two", 2);
		Integer[] values = map.valueArray(new Integer[0]);
		assertEquals(2, values.length);
		assertTrue(values[0] == 1 || values[1] == 1);
		assertTrue(values[0] == 2 || values[1] == 2);
	}

	@Test
	void testForEachKeyRead() {
		map.put("one", 1);
		map.put("two", 2);
		AtomicInteger count = new AtomicInteger(0);
		map.forEachKey(key -> count.incrementAndGet());
		assertEquals(2, count.get());
	}

	@Test
	void testForEachValueRead() {
		map.put("one", 1);
		map.put("two", 2);
		AtomicInteger sum = new AtomicInteger(0);
		map.forEachValue(value -> sum.addAndGet(value));
		assertEquals(3, sum.get());
	}

	@Test
	void testForEachEntryRead() {
		map.put("one", 1);
		map.put("two", 2);
		AtomicInteger sum = new AtomicInteger(0);
		map.forEach((key, value) -> sum.addAndGet(value));
		assertEquals(3, sum.get());
	}

	@Test
	void testFirstKey() {
		map.put("one", 1);
		map.put("two", 2);
		assertEquals("one", map.firstKey());
	}

	@Test
	void testLastKey() {
		map.put("one", 1);
		map.put("two", 2);
		assertEquals("two", map.lastKey());
	}

	@Test
	void testContainsKey() {
		assertFalse(map.containsKey("one"));
		map.put("one", 1);
		assertTrue(map.containsKey("one"));
	}

	@Test
	void testGet() {
		assertNull(map.get("one"));
		map.put("one", 1);
		assertEquals(1, map.get("one"));
	}

	@Test
	void testContainsValue() {
		assertFalse(map.containsValue(1));
		map.put("one", 1);
		assertTrue(map.containsValue(1));
	}

	@Test
	void testKeySet() {
		map.put("one", 1);
		map.put("two", 2);
		ObjectSortedSet<String> keySet = map.keySet();
		assertInstanceOf(ObjectLinkedOpenHashSet.class, keySet);
		assertEquals(2, keySet.size());
		assertTrue(keySet.contains("one"));
		assertTrue(keySet.contains("two"));
	}

	@Test
	void testValues() {
		map.put("one", 1);
		map.put("two", 2);
		ObjectArrayList<Integer> values = map.values();
		assertEquals(2, values.size());
		assertTrue(values.contains(1));
		assertTrue(values.contains(2));
	}

	@Test
	void testDefaultReturnValue() {
		assertNull(map.defaultReturnValue());
		assertThrows(UnsupportedOperationException.class, () -> map.defaultReturnValue(0));
	}

	@Test
	void testForEachKeyWrite() {
		map.put("one", 1);
		map.put("two", 2);
		AtomicInteger count = new AtomicInteger(0);
		map.forEachKeyWrite(key -> count.incrementAndGet());
		assertEquals(2, count.get());
	}

	@Test
	void testForEachValueWrite() {
		map.put("one", 1);
		map.put("two", 2);
		AtomicInteger sum = new AtomicInteger(0);
		map.forEachValueWrite(value -> sum.addAndGet(value));
		assertEquals(3, sum.get());
	}

	@Test
	void testForEachEntryWrite() {
		map.put("one", 1);
		map.put("two", 2);
		AtomicInteger sum = new AtomicInteger(0);
		map.forEachEntryWrite((key, value) -> sum.addAndGet(value));
		assertEquals(3, sum.get());
	}

	@Test
	void testWithWriteLock() {
		Integer result = map.withWriteLock(m -> {
			m.put("one", 1);
			return m.get("one");
		});
		assertEquals(1, result);
		assertEquals(1, map.get("one"));
	}

	@Test
	void testPutAll() {
		Map<String, Integer> input = new HashMap<>();
		input.put("one", 1);
		input.put("two", 2);
		map.putAll(input);
		assertEquals(2, map.size());
		assertEquals(1, map.get("one"));
		assertEquals(2, map.get("two"));
	}

	@Test
	void testClear() {
		map.put("one", 1);
		map.clear();
		assertEquals(0, map.size());
		assertTrue(map.isEmpty());
	}

	@Test
	void testPutIfAbsent() {
		assertNull(map.putIfAbsent("one", 1));
		assertEquals(1, map.putIfAbsent("one", 2));
		assertEquals(1, map.get("one"));
	}

	@Test
	void testRemove() {
		map.put("one", 1);
		assertFalse(map.remove("one", 2));
		assertTrue(map.remove("one", 1));
		assertFalse(map.containsKey("one"));
	}

	@Test
	void testReplace() {
		map.put("one", 1);
		assertFalse(map.replace("one", 2, 3));
		assertTrue(map.replace("one", 1, 3));
		assertEquals(3, map.get("one"));
	}

	@Test
	void testReplaceSingleArg() {
		map.put("one", 1);
		assertEquals(1, map.replace("one", 2));
		assertEquals(2, map.get("one"));
	}

	@Test
	void testComputeIfAbsent() {
		assertEquals(1, map.computeIfAbsent("one", k -> 1));
		assertEquals(1, map.computeIfAbsent("one", k -> 2));
	}

	@Test
	void testComputeIfPresent() {
		assertNull(map.computeIfPresent("one", (k, v) -> v + 1));
		map.put("one", 1);
		assertEquals(2, map.computeIfPresent("one", (k, v) -> v + 1));
	}

	@Test
	void testCompute() {
		assertEquals(1, map.compute("one", (k, v) -> 1));
		assertEquals(2, map.compute("one", (k, v) -> v + 1));
	}

	@Test
	void testMerge() {
		assertEquals(1, map.merge("one", 1, (oldVal, newVal) -> oldVal + newVal));
		assertEquals(3, map.merge("one", 2, (oldVal, newVal) -> oldVal + newVal));
	}

	@Test
	void testPut() {
		assertNull(map.put("one", 1));
		assertEquals(1, map.put("one", 2));
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
							String key = "key-" + threadId + "-" + j;
							map.put(key, j);
							assertEquals(j, map.get(key));
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
		assertThrows(UnsupportedOperationException.class, () -> map.subMap("a", "b"));
		assertThrows(UnsupportedOperationException.class, () -> map.headMap("a"));
		assertThrows(UnsupportedOperationException.class, () -> map.tailMap("a"));
		assertThrows(UnsupportedOperationException.class, () -> map.object2ObjectEntrySet());
	}
}