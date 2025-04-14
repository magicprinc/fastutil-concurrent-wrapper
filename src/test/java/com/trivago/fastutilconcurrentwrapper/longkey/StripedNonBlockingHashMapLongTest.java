package com.trivago.fastutilconcurrentwrapper.longkey;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 @see StripedNonBlockingHashMapLong
*/
class StripedNonBlockingHashMapLongTest {
	static final int threadCount = 10;
	ExecutorService executor;

	@BeforeEach
	void setUp () {
		executor = Executors.newFixedThreadPool(threadCount);
	}

	@AfterEach
	void tearDown () {
		executor.shutdownNow();
	}

	protected <E> StripedNonBlockingHashMapLong<E> spawn () {
		return new StripedNonBlockingHashMapLong<>(1000, true, 16);
	}

	@Test
	public void testPutAndGet() {
		ConcurrentMap<Long,String> map = spawn();
		map.put(1L, "value1");
		assertEquals("value1", map.get(1L));
		assertNull(map.get(2L));
	}

	@Test
	public void testContainsKey() {
		ConcurrentMap<Long, String> map = spawn();
		map.put(1L, "value1");
		assertTrue(map.containsKey(1L));
		assertFalse(map.containsKey(2L));
	}

	@Test
	public void testRemove() {
		ConcurrentMap<Long, String> map = spawn();
		map.put(1L, "value1");
		assertEquals("value1", map.remove(1L));
		assertNull(map.get(1L));
	}

	@Test
	public void testSize() {
		ConcurrentMap<Long, String> map = spawn();
		assertEquals(0, map.size());
		map.put(1L, "value1");
		assertEquals(1, map.size());
		map.put(2L, "value2");
		assertEquals(2, map.size());
		map.remove(1L);
		assertEquals(1, map.size());
	}

	@Test
	public void testIsEmpty() {
		ConcurrentMap<Long, String> map = spawn();
		assertTrue(map.isEmpty());
		map.put(1L, "value1");
		assertFalse(map.isEmpty());
		map.remove(1L);
		assertTrue(map.isEmpty());
	}

	// Concurrent operation tests
	@Test
	public void testConcurrentPut() throws InterruptedException {
		final ConcurrentMap<Long, String> map = spawn();
		final int iterations = 1000;

		for (int i = 0; i < threadCount; i++) {
			final int threadId = i;
			executor.execute(() -> {
				for (int j = 0; j < iterations; j++) {
					long key = threadId * iterations + j;
					map.put(key, "value" + key);
				}
			});
		}

		executor.shutdown();
		assertTrue(executor.awaitTermination(1, TimeUnit.MINUTES));
		assertEquals(threadCount * iterations, map.size());
	}

	@Test
	public void testConcurrentPutIfAbsent() throws InterruptedException {
		final ConcurrentMap<Long,AtomicInteger> map = spawn();
		final int iterations = 1000;

		// Initialize with one key
		map.put(1L, new AtomicInteger(0));

		for (int i = 0; i < threadCount; i++) {
			executor.execute(() -> {
				for (int j = 0; j < iterations; j++) {
					AtomicInteger newValue = new AtomicInteger(1);
					AtomicInteger existing = map.putIfAbsent(1L, newValue);
					if (existing != null) {
						existing.incrementAndGet();
					}
				}
			});
		}

		executor.shutdown();
		assertTrue(executor.awaitTermination(1, TimeUnit.MINUTES));

		assertEquals(1, map.size());
		assertEquals(threadCount * iterations, map.get(1L).get());
	}

	@Test
	public void testConcurrentRemove() throws InterruptedException {
		final ConcurrentMap<Long, String> map = spawn();
		final int entries = 1000;

		// Populate the map
		for (long i = 0; i < entries; i++) {
			map.put(i, "value" + i);
		}

		for (int i = 0; i < threadCount; i++) {
			executor.execute(() -> {
				for (long j = 0; j < entries; j++) {
					map.remove(j);
				}
			});
		}

		executor.shutdown();
		assertTrue(executor.awaitTermination(1, TimeUnit.MINUTES));
		assertTrue(map.isEmpty());
	}

	// Edge cases
	@Test
	public void testNullKey() {
		ConcurrentMap<Long, String> map = spawn();
		try {
			map.put(null, "value");
			fail("Expected NullPointerException");
		} catch (NullPointerException expected) {
			// Expected
		}
	}

	@Test
	public void testNullValue() {
		ConcurrentMap<Long, String> map = spawn();
		try {
			map.put(1L, null);
			fail("Expected NullPointerException");
		} catch (NullPointerException expected) {
			// Expected
		}
	}

	@Test
	public void testReplace() {
		ConcurrentMap<Long, String> map = spawn();
		map.put(1L, "oldValue");
		assertEquals("oldValue", map.replace(1L, "newValue"));
		assertEquals("newValue", map.get(1L));
		assertNull(map.replace(2L, "value"));
	}

	@Test
	public void testReplaceWithOldValue() {
		ConcurrentMap<Long, String> map = spawn();
		map.put(1L, "oldValue");
		assertTrue(map.replace(1L, "oldValue", "newValue"));
		assertEquals("newValue", map.get(1L));
		assertFalse(map.replace(1L, "wrongOldValue", "newValue2"));
	}

	@Test
	public void testClear() {
		ConcurrentMap<Long, String> map = spawn();
		map.put(1L, "value1");
		map.put(2L, "value2");
		map.clear();
		assertTrue(map.isEmpty());
		assertEquals(0, map.size());
	}

	// Performance test (not strictly a unit test, but useful)
	@Test
	public void testPerformanceUnderContention() throws InterruptedException {
		final ConcurrentMap<Long, String> map = spawn();
		final int iterations = 100000;

		long startTime = System.currentTimeMillis();

		for (int i = 0; i < threadCount; i++) {
			executor.execute(() -> {
				for (int j = 0; j < iterations; j++) {
					long key = ThreadLocalRandom.current().nextLong(100);
					map.put(key, "value" + key);
					map.get(key);
					if (j % 10 == 0) {
						map.remove(key);
					}
				}
			});
		}

		executor.shutdown();
		assertTrue(executor.awaitTermination(2, TimeUnit.MINUTES));

		long duration = System.currentTimeMillis() - startTime;
		System.out.println("Performance test completed in " + duration + "ms");
	}

	@Test
	void _withLock () {
		StripedNonBlockingHashMapLong<Integer> map = spawn();

		map.put(1L, (Integer) 999);
		var r = map.withLock(1, x->{
			assertEquals(1, x.getLongKey());
			assertEquals(1, x.getKey());
			assertEquals(999, x.getValue());
			var prev = x.setValue(-1);
			assertEquals(999, prev);
			assertEquals(-1, x.getValue());
			return 31;
		});
		assertEquals(31, r);
		assertEquals(-1, map.get(1));

		r = map.withLock(2, x->{
			assertEquals(2, x.getLongKey());
			assertEquals(2, x.getKey());
			assertNull(x.getValue());
			var prev = x.setValue(42);
			assertNull(prev);
			assertEquals(42, x.getValue());
			return 17;
		});
		assertEquals(17, r);
		assertEquals(42, map.get(2));

		r = map.withLock(2, x->{
			assertEquals(2, x.getLongKey());
			assertEquals(2, x.getKey());
			assertEquals(42, x.getValue());
			var prev = x.setValue(null);
			assertEquals(42, prev);
			assertNull(x.getValue());
			assertFalse(map.containsKey(2));
			return 3;
		});
		assertEquals(3, r);
		assertNull(map.get(2));
		assertFalse(map.containsKey(2));
	}
}