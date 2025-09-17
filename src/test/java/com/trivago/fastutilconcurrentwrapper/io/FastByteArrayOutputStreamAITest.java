package com.trivago.fastutilconcurrentwrapper.io;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/// @see BAOS
class FastByteArrayOutputStreamAITest {
	private BAOS stream;

	@BeforeEach
	void setUp () {
		stream = new BAOS();
	}

	@Test
	void testDefaultConstructor() {
		assertEquals(0, stream.length());
		assertEquals(0, stream.position);
		assertNotNull(stream.array());
		assertTrue(stream.array().length >= 160);
		assertTrue(stream.capacity() >= 160);
	}

	@Test
	void testConstructorWithInitialCapacity() {
		int initialCapacity = 100;
		var customStream = new BAOS(initialCapacity);
		assertEquals(initialCapacity, customStream.array().length);
		assertEquals(0, customStream.length());
		assertEquals(0, customStream.position);
	}

	@Test
	void testConstructorWithByteArray() {
		byte[] initialArray = new byte[]{1, 2, 3, 4, 5};
		var customStream = new BAOS(initialArray);
		assertSame(initialArray, customStream.array());
		assertEquals(0, customStream.length());
		assertEquals(0, customStream.position);
	}

	@Test
	void testWriteSingleByte() {
		stream.write(65); // 'A'
		assertEquals(1, stream.length());
		assertEquals(1, stream.position);
		assertEquals(65, stream.array()[0]);
	}

	@Test
	void testWriteMultipleBytes() {
		byte[] data = new byte[]{1, 2, 3, 4, 5};
		stream.write(data, 0, data.length);
		assertEquals(5, stream.length());
		assertEquals(5, stream.position);
		assertArrayEquals(data, Arrays.copyOf(stream.array(), stream.size()));
	}

	@Test
	void testWriteByteArray() {
		byte[] data = new byte[]{10, 20, 30};
		stream.write(data);
		assertEquals(3, stream.length());
		assertEquals(3, stream.size());
		assertEquals(3, stream.position);
		assertEquals(3, stream.writerIndex());
		assertArrayEquals(data, Arrays.copyOf(stream.array(), stream.size()));
	}

	@Test
	void testAutoGrow() {
		// Test that the array grows when needed
		int initialCapacity = 2;
		var smallStream = new BAOS(initialCapacity);

		smallStream.write(1);
		smallStream.write(2);
		smallStream.write(3); // This should trigger growth

		assertEquals(3, smallStream.length());
		assertEquals(3, smallStream.size());
		assertTrue(smallStream.array().length > initialCapacity);
	}

	@Test
	void testReset() {
		stream.write(new byte[]{1, 2, 3});
		stream.reset();

		assertEquals(0, stream.length());
		assertEquals(0, stream.position);
		// Array capacity should be preserved
		assertTrue(stream.array().length > 0);
	}

	@Test
	void testTrim() {
		stream.write(new byte[]{1, 2, 3});
		int originalCapacity = stream.array().length;
		stream.trim();

		assertEquals(3, stream.array().length);
		assertEquals(3, stream.length());
		assertEquals(3, stream.position);
	}

	@Test
	void testPosition() {
		stream.write(new byte[]{1, 2, 3, 4, 5});
		stream.position(2);
		assertEquals(2, stream.position);

		stream.write(99);
		assertEquals(3, stream.position);
		assertEquals(99, stream.array()[2]);
		assertEquals(5, stream.length()); // Length should not decrease
	}

	@Test
	void testToByteArray() {
		byte[] expected = new byte[]{10, 20, 30};
		stream.write(expected);
		byte[] result = stream.toByteArray();

		assertArrayEquals(expected, result);
		assertNotSame(stream.array(), result); // Should be a copy
	}

	@Test
	void testToStringWithCharset() {
		String text = "Hello World";
		byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
		stream.write(bytes);

		String result = stream.toString(StandardCharsets.UTF_8);
		assertEquals(text, result);
	}

	@Test
	void testWriteTo() throws IOException {
		byte[] data = new byte[]{1, 2, 3, 4, 5};
		stream.write(data);

		ByteArrayOutputStream target = new ByteArrayOutputStream();
		stream.writeTo(target);

		assertArrayEquals(data, target.toByteArray());
	}

	@Test
	void testWriteBoolean() {
		stream.writeBoolean(true);
		stream.writeBoolean(false);

		assertEquals(2, stream.length());
		assertEquals(1, stream.array()[0]);
		assertEquals(0, stream.array()[1]);
	}

	@Test
	void testWriteByte() {
		stream.writeByte(255);
		assertEquals(1, stream.length());
		assertEquals((byte)255, stream.array()[0]);
	}

	@Test
	void testWriteShort() {
		stream.writeShort(0x1234);
		assertEquals(2, stream.length());
		assertEquals(0x12, stream.array()[0] & 0xFF);
		assertEquals(0x34, stream.array()[1] & 0xFF);
	}

	@Test
	void testWriteChar() {
		stream.writeChar('A');
		assertEquals(2, stream.length());
		assertEquals(0, stream.array()[0]); // High byte of 'A' (65)
		assertEquals(65, stream.array()[1] & 0xFF); // Low byte of 'A'
	}

	@Test
	void testWriteInt() {
		stream.writeInt(0x12345678);
		assertEquals(4, stream.length());
		assertEquals(0x12, stream.array()[0] & 0xFF);
		assertEquals(0x34, stream.array()[1] & 0xFF);
		assertEquals(0x56, stream.array()[2] & 0xFF);
		assertEquals(0x78, stream.array()[3] & 0xFF);
	}

	@Test
	void testWriteLong() {
		stream.writeLong(0x123456789ABCDEF0L);
		assertEquals(8, stream.length());
		// Verify first 4 bytes (high part)
		assertEquals(0x12, stream.array()[0] & 0xFF);
		assertEquals(0x34, stream.array()[1] & 0xFF);
		assertEquals(0x56, stream.array()[2] & 0xFF);
		assertEquals(0x78, stream.array()[3] & 0xFF);
		// Verify last 4 bytes (low part)
		assertEquals(0x9A, stream.array()[4] & 0xFF);
		assertEquals(0xBC, stream.array()[5] & 0xFF);
		assertEquals(0xDE, stream.array()[6] & 0xFF);
		assertEquals(0xF0, stream.array()[7] & 0xFF);
	}

	@Test
	void testWriteFloat() {
		float value = 123.456f;
		stream.writeFloat(value);
		int intBits = Float.floatToIntBits(value);

		assertEquals(4, stream.length());
		assertEquals((intBits >> 24) & 0xFF, stream.array()[0] & 0xFF);
		assertEquals((intBits >> 16) & 0xFF, stream.array()[1] & 0xFF);
		assertEquals((intBits >> 8) & 0xFF, stream.array()[2] & 0xFF);
		assertEquals(intBits & 0xFF, stream.array()[3] & 0xFF);
	}

	@Test
	void testWriteDouble() {
		double value = 123.456;
		stream.writeDouble(value);
		long longBits = Double.doubleToLongBits(value);

		assertEquals(8, stream.length());
		// Verify the written bytes match the long representation
		for (int i = 0; i < 8; i++) {
			assertEquals((int)((longBits >> (56 - i * 8)) & 0xFF), stream.array()[i] & 0xFF);
		}
	}

	@Test
	void testWriteBytes() {
		String text = "ABC";
		stream.writeBytes(text);

		assertEquals(3, stream.length());
		assertEquals('A', stream.array()[0]);
		assertEquals('B', stream.array()[1]);
		assertEquals('C', stream.array()[2]);
	}

	@Test
	void testWriteChars() {
		String text = "AB";
		stream.writeChars(text);

		assertEquals(4, stream.length()); // 2 chars * 2 bytes each
		// Char 'A' (65)
		assertEquals(0, stream.array()[0]);
		assertEquals(65, stream.array()[1]);
		// Char 'B' (66)
		assertEquals(0, stream.array()[2]);
		assertEquals(66, stream.array()[3]);
	}

	@Test
	void testWriteUTF() {
		String text = "Hello";
		stream.writeUTF(text);

		// Should write 2-byte length + 5 bytes of content
		assertEquals(7, stream.length());
		assertEquals(0, stream.array()[0]); // Length high byte (5)
		assertEquals(5, stream.array()[1] & 0xFF); // Length low byte
		// Content should match the string
		byte[] expectedContent = text.getBytes(StandardCharsets.UTF_8);
		for (int i = 0; i < 5; i++) {
			assertEquals(expectedContent[i], stream.array()[i + 2]);
		}
	}

	@Test
	void testWriteUTFWithSpecialCharacters() {
		String text = " café"; // Contains non-ASCII character
		stream.writeUTF(text);

		// The café string in UTF-8: ' '(1) + 'c'(1) + 'a'(1) + 'f'(1) + 'é'(2) = 6 bytes
		assertEquals(8, stream.length()); // 2 bytes length + 6 bytes content
		assertEquals(0, stream.array()[0]); // Length high byte (6)
		assertEquals(6, stream.array()[1] & 0xFF); // Length low byte
	}

	@Test
	void testWriteUTFTooLong() {
		// Create a very long string that will exceed 65535+2 bytes when UTF-8 encoded
		val longString = "test".repeat(100000);

		assertThrows(IllegalArgumentException.class, () ->
				stream.writeUTF(longString)
		);

		// Should be rolled back to original position
		assertEquals(0, stream.length());
		assertEquals(0, stream.position);
	}

	@Test
	void testWriteUtf8Char() {
		// Test ASCII character
		int bytesWritten = stream.writeUtf8Char('A');
		assertEquals(1, bytesWritten);
		assertEquals(65, stream.array()[0] & 0xFF);
		stream.reset();

		// Test 2-byte UTF-8 character
		bytesWritten = stream.writeUtf8Char('é');
		assertEquals(2, bytesWritten);
		assertEquals(0xC3, stream.array()[0] & 0xFF);
		assertEquals(0xA9, stream.array()[1] & 0xFF);
		stream.reset();

		// Test 3-byte UTF-8 character
		bytesWritten = stream.writeUtf8Char('€');
		assertEquals(3, bytesWritten);
		assertEquals(0xE2, stream.array()[0] & 0xFF);
		assertEquals(0x82, stream.array()[1] & 0xFF);
		assertEquals(0xAC, stream.array()[2] & 0xFF);
	}

	@Test
	void testWriteObject() throws IOException {
		String testObject = "Test String Object";
		stream.writeObject(testObject);

		// Verify that something was written (object serialization creates significant output)
		assertTrue(stream.length() > 0);

		// Verify we can read it back (simplified test)
		assertTrue(stream.array().length >= stream.length());
	}

	@Test
	void testClose() {
		// Close should not throw an exception and should not affect the stream
		assertDoesNotThrow(() -> stream.close());
		stream.write(1);
		assertEquals(1, stream.length());
		assertEquals(1, stream.size());
	}

	@Test
	void testLengthAndPositionMethods() {
		assertEquals(0, stream.length());
		assertEquals(0, stream.position());

		stream.write(new byte[]{1, 2, 3});
		assertEquals(3, stream.length());
		assertEquals(3, stream.position());

		stream.position(1);
		assertEquals(3, stream.length());
		assertEquals(1, stream.position());
	}
}