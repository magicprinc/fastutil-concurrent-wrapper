package com.trivago.fastutilconcurrentwrapper.util;

import com.trivago.fastutilconcurrentwrapper.support.ReadWriteLockCollection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 @see ReadWriteLockCollection */
class ReadWriteLockCollectionTest {
	private List<String> backingList;
	private ReadWriteLockCollection<String> collection;

	@BeforeEach
	void setUp() {
		backingList = new ArrayList<>();
		collection = new ReadWriteLockCollection<>(backingList);
	}

	@Test
	void testBasicOperations() {
		// Add elements
		assertTrue(collection.add("A"));
		assertTrue(collection.add("B"));

		// Verify size
		assertEquals(2, collection.size());

		// Check contains
		assertTrue(collection.contains("A"));
		assertFalse(collection.contains("C"));

		// Remove element
		assertTrue(collection.remove("A"));
		assertEquals(1, collection.size());
	}

	@Test
	void testConcurrentReadOperations() throws InterruptedException {
		var executor = Executors.newFixedThreadPool(10);
		try {
			var latch = new CountDownLatch(100);

			// Add initial elements
			collection.add("A");
			collection.add("B");

			// Submit 100 concurrent read operations
			for (int i = 0; i < 100; i++){
				int finalI = i;
				executor.submit(()->{
					try {
						boolean contains = collection.contains(finalI % 2 == 0 ? "A" : "B");
						assertTrue(contains);
					} finally {
						latch.countDown();
					}
				});
			}

			// Wait for all operations to complete
			latch.await(5, TimeUnit.SECONDS);

			assertEquals(2, collection.size());
		} finally {
			executor.shutdown();
		}
	}

	@Test
	void testConcurrentReadWriteOperations() throws InterruptedException {
		var executor = Executors.newFixedThreadPool(10);
		try {
			var readLatch = new CountDownLatch(50);
			var writeLatch = new CountDownLatch(50);

			// Initial state
			collection.add("A");

			// Submit concurrent read and write operations
			for (int i = 0; i < 50; i++){
				// Write operations
				int v = i;
				executor.submit(()->{
					try {
						collection.add(String.valueOf(v));
					} finally {
						writeLatch.countDown();
					}
				});

				// Read operations
				executor.submit(()->{
					try {
						collection.size(); // Force read lock acquisition
						assertTrue(collection.size() >= 1);
					} finally {
						readLatch.countDown();
					}
				});
			}

			// Wait for completion
			readLatch.await(5, TimeUnit.SECONDS);
			writeLatch.await(5, TimeUnit.SECONDS);

			// Verify final state
			assertTrue(collection.size() >= 51); // Must contain original "A" plus some added numbers
			executor.shutdown();
		} finally {
			executor.shutdown();
		}
	}

	@Test
	void testIteratorThreadSafety() {
		var executor = Executors.newFixedThreadPool(2);
		try {
			collection.addAll(List.of("A", "B", "C"));

			var latch = new CountDownLatch(2);

			// One thread iterates, another modifies
			executor.execute(()->{
				try {
					collection.forEach(element->{
						assertNotNull(element);
						assertInstanceOf(String.class, element);
					});
				} finally {
					latch.countDown();
				}
			});

			executor.execute(()->{
				try {
					collection.remove("B");
					collection.add("D");
				} finally {
					latch.countDown();
				}
			});

			try {
				latch.await(5, TimeUnit.SECONDS);
			} catch (InterruptedException e){
				fail("Interrupted during iterator test");
			}

			assertEquals(3, collection.size());
		} finally {
			executor.shutdown();
		}
	}

	@Test
	void testEdgeCases() {
		// Null element handling
		collection.add(null);

		// Empty collection behavior
		assertFalse(collection.isEmpty());
		assertEquals(1, collection.size());

		// Single element operations
		collection.add("A");
		assertTrue(collection.contains("A"));
		assertFalse(collection.isEmpty());
	}

	@Test
	void testSerialization() throws IOException, ClassNotFoundException {
		// Add elements
		collection.addAll(List.of("A", "B", "C"));

		// Serialize
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(collection);
		oos.close();

		// Deserialize
		ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
		ObjectInputStream ois = new ObjectInputStream(bis);
		ReadWriteLockCollection<String> deserialized = (ReadWriteLockCollection<String>) ois.readObject();
		ois.close();

		// Verify contents
		assertEquals(collection.size(), deserialized.size());
		assertTrue(deserialized.containsAll(collection));
	}

	@Test
	void testIteratorStabilityUnderModification() {
		var executor = Executors.newSingleThreadExecutor();
		try {
			collection.addAll(List.of("A", "B", "C"));
			Iterator<String> iterator = collection.iterator();

			// Modify collection while iterating
			executor.submit(()->collection.remove("B"));

			// Verify iterator remains stable
			int count = 0;
			while (iterator.hasNext()){
				String element = iterator.next();
				assertNotNull(element);
				count++;
			}

			assertEquals(3, count); // Should see all elements including removed one
			executor.shutdown();
		} finally {
			executor.shutdown();
		}
	}

	@Test
	void basic () {
		var c = new ReadWriteLockCollection<>(Arrays.asList(1, 2, 3, 4, 5));
		assertEquals(5, c.size());
		assertEquals(5, c.stream().count());
		assertEquals(List.of(2, 4), c.stream().filter(x->x%2==0).toList());

		int i = 1;
		for (var v : c){
			assertEquals(v, i++);
		}
	}

	@Test
	void iteratorRemove () {
		var c = new ReadWriteLockCollection<>(new ArrayList<Integer>());
		c.addAll(1, 2, 3, 4, 5);
		for (var it = c.iterator(); it.hasNext();){
			var x = it.next();
			if ((x&1)==1)
				it.remove();
		}
		var z = new ReadWriteLockCollection<>(new ArrayList<Integer>());
		z.addAll(1, 2, 3, 4, 5);
		z.removeIf(x->(x&1)==1);
		assertEquals(c,z);
		assertEquals("[2, 4]", z.toString());
	}
}