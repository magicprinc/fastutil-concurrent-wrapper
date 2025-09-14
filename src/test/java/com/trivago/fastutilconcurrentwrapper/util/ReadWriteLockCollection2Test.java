package com.trivago.fastutilconcurrentwrapper.util;

import com.trivago.fastutilconcurrentwrapper.support.ReadWriteLockCollection;
import com.trivago.fastutilconcurrentwrapper.support.SmartIterator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 @see ReadWriteLockCollection */
class ReadWriteLockCollection2Test {
	private ReadWriteLockCollection<Integer> collection;
	private Collection<Integer> backingCollection;

	@BeforeEach
	void setUp() {
		backingCollection = new ArrayList<>();
		collection = new ReadWriteLockCollection<>(backingCollection);
	}

	@Test
	void constructorWithNullCollectionThrows() {
		assertThrows(NullPointerException.class, () -> new ReadWriteLockCollection<>(null));
	}

	@Test
	void constructorWithNullLockThrows() {
		assertThrows(NullPointerException.class,
			() -> new ReadWriteLockCollection<>(new ArrayList<>(), null));
	}

	@Test
	void size() {
		assertEquals(0, collection.size());
		collection.add(1);
		assertEquals(1, collection.size());
	}

	@Test
	void isEmpty() {
		assertTrue(collection.isEmpty());
		collection.add(1);
		assertFalse(collection.isEmpty());
	}

	@Test
	void contains() {
		assertFalse(collection.contains(1));
		collection.add(1);
		assertTrue(collection.contains(1));
		assertFalse(collection.contains(2));
	}

	@Test
	void iterator() {
		collection.add(1);
		collection.add(2);

		Iterator<Integer> it = collection.iterator();
		assertTrue(it.hasNext());
		assertEquals(1, it.next());
		assertTrue(it.hasNext());
		assertEquals(2, it.next());
		assertFalse(it.hasNext());
	}

	@Test
	void iteratorRemove() {
		collection.add(1);
		collection.add(2);

		Iterator<Integer> it = collection.iterator();
		it.next();
		it.remove();
		assertEquals(1, collection.size());
		assertFalse(collection.contains(1));
	}

	@Test
	void iteratorRemoveWithoutNextThrows() {
		collection.add(1);
		Iterator<Integer> it = collection.iterator();
		assertThrows(IllegalStateException.class, it::remove);
	}

	@Test
	void iteratorNextWhenEmptyThrows() {
		Iterator<Integer> it = collection.iterator();
		assertThrows(NoSuchElementException.class, it::next);
	}

	@Test
	void toArray() {
		collection.add(1);
		collection.add(2);
		Object[] array = collection.toArray();
		assertArrayEquals(new Object[]{1, 2}, array);
	}

	@Test
	void toArrayWithType() {
		collection.add(1);
		collection.add(2);
		Integer[] array = collection.toArray(new Integer[0]);
		assertArrayEquals(new Integer[]{1, 2}, array);
	}

	@Test
	void toArrayWithGenerator() {
		collection.add(1);
		collection.add(2);
		Integer[] array = collection.toArray(Integer[]::new);
		assertArrayEquals(new Integer[]{1, 2}, array);
	}

	@Test
	void containsAll() {
		collection.add(1);
		collection.add(2);
		collection.add(3);

		assertTrue(collection.containsAll(Arrays.asList(1, 2)));
		assertFalse(collection.containsAll(Arrays.asList(1, 4)));
	}

	@Test
	void add() {
		assertTrue(collection.add(1));
		assertTrue(collection.contains(1));
		assertEquals(1, collection.size());
	}

	@Test
	void remove() {
		collection.add(1);
		assertTrue(collection.remove(1));
		assertFalse(collection.contains(1));
		assertFalse(collection.remove(1));
	}

	@Test
	void addAll() {
		assertTrue(collection.addAll(Arrays.asList(1, 2, 3)));
		assertEquals(3, collection.size());
		assertTrue(collection.containsAll(Arrays.asList(1, 2, 3)));
	}

	@Test
	void addAllVarargs() {
		collection.addAll(1, 2, 3);
		assertEquals(3, collection.size());
		assertTrue(collection.containsAll(Arrays.asList(1, 2, 3)));
	}

	@Test
	void removeAll() {
		collection.addAll(1, 2, 3, 4);
		assertTrue(collection.removeAll(Arrays.asList(1, 3)));
		assertEquals(2, collection.size());
		assertTrue(collection.containsAll(Arrays.asList(2, 4)));
	}

	@Test
	void removeIf() {
		collection.addAll(1, 2, 3, 4);
		assertTrue(collection.removeIf(x -> x % 2 == 0));
		assertEquals(2, collection.size());
		assertTrue(collection.containsAll(Arrays.asList(1, 3)));
	}

	@Test
	void retainAll() {
		collection.addAll(1, 2, 3, 4);
		assertTrue(collection.retainAll(Arrays.asList(2, 3)));
		assertEquals(2, collection.size());
		assertTrue(collection.containsAll(Arrays.asList(2, 3)));
	}

	@Test
	void clear() {
		collection.addAll(1, 2, 3);
		collection.clear();
		assertTrue(collection.isEmpty());
	}

	@Test
	void spliterator() {
		collection.addAll(1, 2, 3);
		List<Integer> result = new ArrayList<>();
		collection.spliterator().forEachRemaining(result::add);
		assertEquals(Arrays.asList(1, 2, 3), result);
	}

	@Test
	void stream() {
		collection.addAll(1, 2, 3);
		List<Integer> result = collection.stream().collect(Collectors.toList());
		assertEquals(Arrays.asList(1, 2, 3), result);
	}

	@Test
	void parallelStream() {
		collection.addAll(1, 2, 3);
		List<Integer> result = collection.parallelStream().collect(Collectors.toList());
		assertEquals(Arrays.asList(1, 2, 3), result);
	}

	@Test
	void forEach() {
		collection.addAll(1, 2, 3);
		List<Integer> result = new ArrayList<>();
		collection.forEach(result::add);
		assertEquals(Arrays.asList(1, 2, 3), result);
	}

	@Test
	void toStringTest() {
		collection.addAll(1, 2, 3);
		assertEquals(backingCollection.toString(), collection.toString());
	}

	@Test
	void equalsTest() {
		collection.addAll(1, 2, 3);
		Collection<Integer> other = new ArrayList<>(Arrays.asList(1, 2, 3));
		assertEquals(collection, other);

		Collection<Integer> different = new ArrayList<>(Arrays.asList(1, 2));
		assertNotEquals(collection, different);
		assertNotEquals(different, collection);
	}

	@Test
	void hashCodeTest() {
		collection.addAll(1, 2, 3);
		assertEquals(backingCollection.hashCode(), collection.hashCode());
	}

	@Test
	void concurrentAccess() throws InterruptedException {
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
							collection.add(threadId * iterations + j);
							collection.contains(threadId * iterations + j);
							collection.size();
						}
					} finally {
						latch.countDown();
					}
				});
			}

			latch.await(5, TimeUnit.SECONDS);
			executor.shutdown();

			assertEquals(threadCount * iterations, collection.size());
		} finally {
			executor.shutdownNow();
		}
	}

	@Test
	void customLockImplementation() {
		ReadWriteLock customLock = new ReentrantReadWriteLock();
		Collection<Integer> backing = new ArrayList<>();
		ReadWriteLockCollection<Integer> customCollection =
			new ReadWriteLockCollection<>(backing, customLock);

		customCollection.add(1);
		assertEquals(1, customCollection.size());
	}

	@Test
	void tryAdvance() {
		collection.addAll(1, 2, 3);
		SmartIterator<Integer> it = collection.iterator();
		List<Integer> result = new ArrayList<>();

		while (it.tryAdvance(result::add)) {
			// continue
		}

		assertEquals(Arrays.asList(1, 2, 3), result);
	}

	@Test
	void estimateSize() {
		collection.addAll(1, 2, 3);
		assertEquals(3, collection.iterator().estimateSize());
	}

	@Test
	void getExactSizeIfKnown() {
		collection.addAll(1, 2, 3);
		assertEquals(3, collection.iterator().getExactSizeIfKnown());
	}
}