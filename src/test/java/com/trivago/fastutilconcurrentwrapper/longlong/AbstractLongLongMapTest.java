package com.trivago.fastutilconcurrentwrapper.longlong;

import com.trivago.fastutilconcurrentwrapper.AbstractMapTest;
import com.trivago.fastutilconcurrentwrapper.longkey.ConcurrentLongLongMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

abstract class AbstractLongLongMapTest extends AbstractMapTest {
  private ConcurrentLongLongMap map;
  // Keep the default value to easily verify that this value is returned.
  protected long defaultValue;
  // Some methods return the default value of the underlying Fastutil implementation.
  private static final long FASTUTIL_DEFAULT_VALUE = 0L;

  abstract ConcurrentLongLongMap createMap ();

  @BeforeEach
  void initializeMap() {
    defaultValue = nextLong();
    map = createMap();
  }

	@Test  @Override
  protected void containsKeyReturnsFalseIfMapIsEmpty() {
    final long key = nextLong();

    final boolean contains = map.containsKey(key);

    assertFalse(contains);
  }

  @Test  @Override
  protected void containsKeyReturnsTrueIfKeyExists() {
    long key = nextLong();
    long value = nextLong();
    map.put(key, value);

    final boolean contains = map.containsKey(key);

    assertTrue(contains);
  }

  @Test  @Override
  protected void containsKeyReturnsFalseIfKeyWasRemoved() {
    long key = nextLong();
    long value = nextLong();
    map.put(key, value);
    map.remove(key);

    final boolean contains = map.containsKey(key);

    assertFalse(contains);
  }

  @Test  @Override
  protected void mapIsEmptyWhenNothingWasInserted() {
    final boolean empty = map.isEmpty();

    assertTrue(empty);
  }

  @Test  @Override
  protected void mapIsEmptyWhenAllKeysAreDeleted() {
    int entryCount = (Math.abs(nextInt()) % 100) + 1;
    long value = nextLong();

    for (long key = 1; key <= entryCount; key++) {
      map.put(key, value);
    }
    for (long key = 1; key <= entryCount; key++) {
      map.remove(key);
    }

    final boolean empty = map.isEmpty();

    assertTrue(empty);
  }

  @Test  @Override
  protected void sizeIsCorrect() {
    int entries = (Math.abs(nextInt()) % 50) + 1;
    long value = nextLong();

    for (long key = 1; key <= entries; key++) {
      map.put(key, value);
    }

    final int size = map.size();

    assertEquals(entries, size);
  }

  @Test  @Override
  protected void gettingExistingValueReturnsCorrectValue() {
    long key = nextLong();
    long value = nextLong();
    map.put(key, value);
    final long returnedValue = map.get(key);

    assertEquals(value, returnedValue);
  }

  @Test  @Override
  protected void gettingNonExistingValueReturnsCorrectValue() {
    long key = nextLong();
    final long returnedValue = map.get(key);

    assertEquals(defaultValue, returnedValue);
  }

  @Test  @Override
  protected void removingNonExistingKeyReturnsDefaultValue() {
    long key = nextLong();
    final long removedValue = map.remove(key);

    assertEquals(FASTUTIL_DEFAULT_VALUE, removedValue);
  }

  @Test  @Override
  protected void removingExistingKeyReturnsPreviousValue() {
    long key = nextLong();
    long value = nextLong();
    map.put(key, value);
    final long removedValue = map.remove(key);

    assertEquals(value, removedValue);
  }

  @Test  @Override
  protected void removingWithValueWhenKeyDoesNotExistReturnsFalse() {
    long key = nextLong();
    long value = nextLong();
    final boolean result = map.remove(key, value);

    assertFalse(result);
  }

  @Test  @Override
  protected void removingWithValueWhenValueIsDifferentReturnsFalse() {
    long key = nextLong();
    long value = nextLong();
    map.put(key, value);
    final boolean result = map.remove(key, value - 1);

    assertFalse(result);
  }

  @Test  @Override
  protected void removingWithValueWhenValueIsSameReturnsTrue() {
    long key = nextLong();
    long value = nextLong();
    map.put(key, value);
    final boolean result = map.remove(key, value);

    assertTrue(result);
  }

  @Test  @Override
  protected void puttingValueIfAbsentReturnsSameValue() {
    long key = nextLong();
    long value = nextLong();
    map.computeIfAbsent(key, l -> value);

    long result = map.get(key);

    assertEquals(result, value);
  }

  @Test  @Override
  protected void checkingValueIfNotAbsentReturnsSameValue() {
    long key = nextLong();
    long value = nextLong();
    map.put(key, value);
    long returned = map.computeIfAbsent(key, l -> value);

    long result = map.get(key);

    assertEquals(result, value);
    assertEquals(value, returned);
  }

  @Test  @Override
  protected void replacingValueIfPresentReturnsNewValue() {
    long key = nextLong();
    long value = nextLong();
    map.put(key, value);

    map.computeIfPresent(key, Long::sum); // key + old value

    long result = map.get(key);

    assertEquals(result, key + value);
  }

  @Test  @Override
  protected void checkingValueIfNotPresentReturnsDefaultValue() {
    long key = nextLong();
    map.computeIfPresent(key, Long::sum);

    long result = map.get(key);

    assertEquals(result, map.getDefaultValue());
  }
}