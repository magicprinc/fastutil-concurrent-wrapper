package com.trivago.fastutilconcurrentwrapper.util;

import com.trivago.fastutilconcurrentwrapper.io.BAIS;
import com.trivago.fastutilconcurrentwrapper.io.BAOS;
import com.trivago.fastutilconcurrentwrapper.io.BAOSTest;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.HexFormat;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;

import static it.unimi.dsi.fastutil.bytes.ByteArrays.EMPTY_ARRAY;
import static org.junit.jupiter.api.Assertions.*;

/// @see JBytes
class JBytesTest {
	@Test
	void testSafe() {
		assertSame(EMPTY_ARRAY, JBytes.safe(null));

		byte[] b = {1, 2};
		assertSame(b, JBytes.safe(b));
		b = new byte[0];
		assertSame(b, JBytes.safe(b));
	}

	@Test
	void testIsEmptyAndNonEmpty() {
		assertTrue(JBytes.isEmpty(null));
		assertTrue(JBytes.isEmpty(new byte[0]));
		assertFalse(JBytes.isEmpty(new byte[]{0}));

		assertFalse(JBytes.nonEmpty(null));
		assertFalse(JBytes.nonEmpty(new byte[0]));
		assertTrue(JBytes.nonEmpty(new byte[]{1}));

		assertEquals(0, JBytes.len(null));
		assertEquals(0, JBytes.len(new byte[0]));
		assertEquals(1, JBytes.len(new byte[]{0}));
	}

	@Test
	void testArrayCopy() {
		Integer[] src = {1, 2, 3};
		Number[] dst = new Number[3];
		JBytes.arraycopy(src, dst, 2);
		assertArrayEquals(new Integer[]{1, 2, null}, dst);

		dst = new Integer[3];
		JBytes.arraycopy(null, dst, 2);
		assertArrayEquals(new Integer[]{null, null, null}, dst);

		dst = new Number[2];
		JBytes.arraycopy(src, dst, 2);
		assertArrayEquals(new Integer[]{1, 2}, dst);
	}

	@Test
	void testCopyOrSame() {
		assertNull(JBytes.copyOrSame(null, 0, 100));
		byte[] src = {10, 20, 30, 40};
		assertSame(src, JBytes.copyOrSame(src, 0, src.length));
		assertSame(src, JBytes.copyOrSame(src, 0, 1000));
		byte[] result = JBytes.copyOrSame(src, 1, 2);
		assertArrayEquals(new byte[]{20, 30}, result);
		result = JBytes.copyOrSame(src, 1, 200);
		assertArrayEquals(new byte[]{20, 30, 40}, result);
	}

	@Test
	void testSetAllUnary () {
		assertSame(EMPTY_ARRAY, JBytes.setAll(null, i->i));

		byte[] arr = new byte[5];
		IntUnaryOperator op = i -> i * 10;
		byte[] result = JBytes.setAll(arr, op);
		assertSame(arr, result);
		assertArrayEquals(new byte[]{0, 10, 20, 30, 40}, result);

		// Test break condition (value < Short.MIN_VALUE triggers break)
		byte[] arrBreak = new byte[5];
		IntUnaryOperator opBreak = i -> i == 3 ? Short.MIN_VALUE - 1 : i;
		byte[] resBreak = JBytes.setAll(arrBreak, opBreak);
		// Should stop setting at index 3
		assertEquals(0, resBreak[3]);
		assertSame(arrBreak, resBreak);
		assertArrayEquals(new byte[]{0, 1, 2, 0, 0}, resBreak);
	}

	@Test
	void testSetAllBinary() {
		assertSame(EMPTY_ARRAY, JBytes.setAll(null, (i,j)->i));

		byte[] arr = {1, 2, 3};
		IntBinaryOperator op = (i, b) -> b + i;
		byte[] result = JBytes.setAll(arr, op);
		assertSame(arr, result);
		assertArrayEquals(new byte[]{1, 3, 5}, result);

		// Break condition test
		byte[] arrBreak = {1, 2, 3};
		IntBinaryOperator opBreak = (i, b) -> i == 2 ? Short.MIN_VALUE - 1 : b;
		byte[] resBreak = JBytes.setAll(arrBreak, opBreak);
		assertEquals(2, arrBreak[1]); // unchanged
		assertEquals(3, arrBreak[2]); // unchanged because break before assign
	}

	@Test
	void testConcatOrSame() {
		assertSame(EMPTY_ARRAY, JBytes.concatOrSame(null, null));
		assertSame(EMPTY_ARRAY, JBytes.concatOrSame(null, EMPTY_ARRAY));
		assertSame(EMPTY_ARRAY, JBytes.concatOrSame(EMPTY_ARRAY, null));

		byte[] a = {1, 2};
		byte[] b = {3, 4};
		assertSame(b, JBytes.concatOrSame(null, b));
		assertSame(a, JBytes.concatOrSame(a, null));
		byte[] c = JBytes.concatOrSame(a, b);
		assertArrayEquals(new byte[]{1, 2, 3, 4}, c);
		assertArrayEquals(c, JBytes.concatClone(a, b));
		assertArrayEquals(c, JBytes.concatMulti(a, b));
	}

	@Test
	void testConcatClone () {
		assertSame(EMPTY_ARRAY, JBytes.concatClone(null, null));
		assertSame(EMPTY_ARRAY, JBytes.concatClone(null, EMPTY_ARRAY));
		assertSame(EMPTY_ARRAY, JBytes.concatClone(EMPTY_ARRAY, null));

		byte[] a = {1, 2};
		byte[] b = {3, 4};
		byte[] bClone = JBytes.concatClone(null, b);
		assertArrayEquals(b, bClone);
		assertNotSame(b, bClone);
		byte[] aClone = JBytes.concatClone(a, null);
		assertArrayEquals(a, aClone);
		assertNotSame(a, aClone);
		byte[] c = JBytes.concatClone(a, b);
		assertArrayEquals(new byte[]{1, 2, 3, 4}, c);
		assertArrayEquals(c, JBytes.concatOrSame(a, b));
		assertArrayEquals(c, JBytes.concatMulti(a, b));
	}

	@Test
	void testConcatMulti () {
		assertSame(EMPTY_ARRAY, JBytes.concatMulti());
		assertArrayEquals(EMPTY_ARRAY, JBytes.concatMulti(null, null));
		assertArrayEquals(EMPTY_ARRAY, JBytes.concatMulti(null, EMPTY_ARRAY));

		byte[] a = {1};
		byte[] b = {2, 3};
		byte[] c = {4};
		byte[] result = JBytes.concatMulti(a, null, b, c);
		assertArrayEquals(new byte[]{1, 2, 3, 4}, result);
		assertArrayEquals(new byte[]{1, 4}, JBytes.concatMulti(a, null, EMPTY_ARRAY, c));
	}

	@Test
	void testLongConversion() {
		long value = 0x1122334455667788L;
		byte[] bytes = JBytes.longToBytes(value);
		assertNotNull(bytes);
		assertEquals(8, bytes.length);
		long result = JBytes.bytesToLong(bytes);
		assertEquals(value, result);

		assertEquals(Long.MAX_VALUE, JBytes.bytesToLong(JBytes.longToBytes(Long.MAX_VALUE)));

		val r = ThreadLocalRandom.current();
		for (int i = 0; i < 1_000_000; i++){
			long x = r.nextLong();
			assertEquals(x, JBytes.bytesToLong(JBytes.longToBytes(x)));
		}
	}

	@Test
	void testDirectByteArrayAccessGetSet() {
		byte[] arr = new byte[8];

		JBytes.DirectByteArrayAccess.setBoolean(arr, 0, true);
		assertTrue(JBytes.DirectByteArrayAccess.getBoolean(arr, 0));

		JBytes.DirectByteArrayAccess.setChar(arr, 0, 'A');
		assertEquals('A', JBytes.DirectByteArrayAccess.getChar(arr, 0));

		JBytes.DirectByteArrayAccess.setShort(arr, 0, (short) 12345);
		assertEquals(12345, JBytes.DirectByteArrayAccess.getShort(arr, 0));

		JBytes.DirectByteArrayAccess.setUnsignedShort(arr, 0, 60000);
		assertEquals(60000, JBytes.DirectByteArrayAccess.getUnsignedShort(arr, 0));

		JBytes.DirectByteArrayAccess.setInt(arr, 0, 123456789);
		assertEquals(123456789, JBytes.DirectByteArrayAccess.getInt(arr, 0));

		JBytes.DirectByteArrayAccess.setFloat(arr, 0, 1.5f);
		assertEquals(1.5f, JBytes.DirectByteArrayAccess.getFloat(arr, 0));

		JBytes.DirectByteArrayAccess.setFloatRaw(arr, 0, 2.5f);
		assertEquals(2.5f, JBytes.DirectByteArrayAccess.getFloatRaw(arr, 0));

		JBytes.DirectByteArrayAccess.setLong(arr, 0, 987654321L);
		assertEquals(987654321L, JBytes.DirectByteArrayAccess.getLong(arr, 0));

		JBytes.DirectByteArrayAccess.setDouble(arr, 0, 123456.789);
		assertEquals(123456.789, JBytes.DirectByteArrayAccess.getDouble(arr, 0));

		JBytes.DirectByteArrayAccess.setDoubleRaw(arr, 0, 98765.4321);
		assertEquals(98765.4321, JBytes.DirectByteArrayAccess.getDoubleRaw(arr, 0));
	}

	@Test
	void _randomAccess () {
		val bytes = new byte[11];// 0..10
		JBytes.DirectByteArrayAccess.setLong(bytes, 1, Long.MAX_VALUE);
		assertEquals("007fffffffffffffff0000", HexFormat.of().formatHex(bytes));
		JBytes.DirectByteArrayAccess.setInt(bytes, 7, 0xA1B2C3D4);//11-4==7
		assertEquals("007fffffffffffa1b2c3d4", HexFormat.of().formatHex(bytes));
	}


	@Test
	void doubleAndFloat () {
		var w = new BAOS();
		var x = new BAIS(w.array(), 0, 160);
		var y = new DataInputStream(new ByteArrayInputStream(w.array(), 0, 160));
		var r = ThreadLocalRandom.current();
		BAOSTest.loop(10_000, ()->{
			w.reset();
			double d = r.nextDouble();
			w.writeDouble(d);
			float f = r.nextFloat();
			w.writeFloat(f);

			x.reset();
			y.reset();

			assertEquals(d, x.readDouble());
			assertEquals(d, y.readDouble());
			assertEquals(f, x.readFloat());
			assertEquals(f, y.readFloat());
			return null;
		});
	}
}