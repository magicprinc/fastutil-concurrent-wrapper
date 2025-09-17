package com.trivago.fastutilconcurrentwrapper.io;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.UTFDataFormatException;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/// @see BAIS
class BAISTest {
	private byte[] testData;
	private BAIS stream;

	@BeforeEach
	void setUp () {
		testData = new byte[]{
			0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
			0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10,
			'H', 'e', 'l', 'l', 'o', '\r', '\n', 'W', 'o', 'r', 'l', 'd', 0
		};
		stream = new BAIS(testData);
	}

	@Test  @DisplayName("Test constructor with offset and maxLength")
	void testConstructorWithOffsetAndMaxLength() {
		val customStream = new BAIS(testData, 4, 8);
		assertEquals(4, customStream.offset());// начало в массиве: уровень 0 для position, mark, length
		assertEquals(8, customStream.limit());
		assertEquals(8, customStream.length());
		assertEquals(8, customStream.available());
		assertEquals(0, customStream.position());
		assertEquals(0, customStream.readerIndex());
		assertEquals(0, customStream.markIndex());
		assertEquals(29, customStream.array().length);

		customStream.position(29);
		assertEquals(8, customStream.position());
		assertEquals(8, customStream.readerIndex());
		assertEquals(0, customStream.available());
	}

	@Test  @DisplayName("Test array accessors")
	void testArrayAccessors() {
		byte[] newArray = new byte[]{0x11, 0x12, 0x13, 0, 0};
		stream.array(newArray, 1, 3);
		assertArrayEquals(newArray, stream.array());
		assertEquals(3, stream.length());
		assertEquals(3, stream.available());
	}

	@Test  @DisplayName("Test offset accessors")
	void testOffsetAccessors() {
		stream.offset(5);
		assertEquals(5, stream.offset());
	}

	@Test  @DisplayName("Test position and readerIndex")
	void testPositionAndReaderIndex() {
		stream.position(5);
		assertEquals(5, stream.position());
		assertEquals(5, stream.readerIndex());

		stream.readerIndex(10);
		assertEquals(10, stream.position());
	}

	@Test  @DisplayName("Test position beyond limit")
	void testPositionBeyondLimit() {
		stream.position(1000);
		assertEquals(stream.limit(), stream.position());
		assertEquals(stream.length(), stream.position());
		assertEquals(stream.length(), stream.readerIndex());
	}

	@Test  @DisplayName("Test limit accessors")
	void testLimitAccessors() {
		stream.limit(10);
		assertEquals(10, stream.limit());
		assertEquals(10, stream.length());
	}

	@Test  @DisplayName("Test available")
	void testAvailable() {
		assertEquals(testData.length, stream.available());
		assertEquals(stream.length()-stream.position(), stream.available());
		stream.position(5);
		assertEquals(testData.length - 5, stream.available());
	}

	@Test  @DisplayName("Test close has no effect")
	void testClose() {
		stream.close();// assertDoesNotThrow
		stream.close();// assertDoesNotThrow
		assertEquals(0, stream.position()); // Should still be readable after close
	}

	@Test  @DisplayName("Test skip")
	void testSkip () {
		long skipped = stream.skip(5);
		assertEquals(5, skipped);
		assertEquals(5, stream.position());

		// Test skipping beyond available
		long skippedBeyond = stream.skip(1000);
		assertEquals(testData.length - 5, skippedBeyond);
		assertEquals(testData.length, stream.position());
		assertEquals(0, stream.available());
	}

	@Test  @DisplayName("Test read single byte")
	void testReadSingleByte() {
		assertEquals(0x01, stream.read());
		assertEquals(0x02, stream.read());
		assertEquals(2, stream.position());
		assertEquals(2, stream.readerIndex());

		// Test pre and EOF
		stream.position(testData.length-1);
		assertEquals(0, stream.read());
		assertEquals(-1, stream.read());

		stream.position(testData.length);
		assertEquals(-1, stream.read());
	}

	@Test  @DisplayName("Test read byte array")
	void testReadByteArray() {
		val buffer = new byte[5];
		int bytesRead = stream.read(buffer);
		assertEquals(5, bytesRead);
		assertArrayEquals(new byte[]{0x01, 0x02, 0x03, 0x04, 0x05}, buffer);

		// Test partial read at end
		Arrays.fill(buffer, (byte)1);
		stream.position(testData.length - 2);
		bytesRead = stream.read(buffer);
		assertEquals(2, bytesRead);
		assertArrayEquals(new byte[]{'d',0, 1,1,1}, buffer);
	}

	@Test  @DisplayName("Test read with offset and length")
	void testReadWithOffsetAndLength() {
		val buffer = new byte[10];
		int bytesRead = stream.read(buffer, 2, 5);
		assertEquals(5, bytesRead);
		assertEquals(5, stream.position());
		assertArrayEquals(new byte[]{0, 0, 0x01, 0x02, 0x03, 0x04, 0x05, 0, 0, 0}, buffer);
	}

	@Test  @DisplayName("Test readAllBytes")
	void testReadAllBytes() {
		byte[] allBytes = stream.readAllBytes();
		assertArrayEquals(testData, allBytes);
		assertEquals(testData.length, stream.position());
	}

	@Test  @DisplayName("Test readNBytes")
	void testReadNBytes() {
		byte[] bytes = stream.readNBytes(5);
		assertArrayEquals(new byte[]{0x01, 0x02, 0x03, 0x04, 0x05}, bytes);
		assertEquals(5, stream.position());

		// Test reading beyond available
		stream.position(testData.length - 2);
		bytes = stream.readNBytes(5);
		assertEquals(2, bytes.length);
	}

	@Test  @DisplayName("Test peek")
	void testPeek() {
		assertEquals(0x01, stream.peek());
		assertEquals(0, stream.position()); // Position shouldn't change

		assertEquals(0x01, stream.read());
		assertEquals(0x02, stream.peek());
		assertEquals(1, stream.position());
		assertEquals(0x02, stream.read());
	}

	@Test  @DisplayName("Test readBoolean")
	void testReadBoolean() {
		assertTrue(stream.readBoolean()); // 0x01 != 0
		stream.position(stream.array().length-1);
		assertFalse(stream.readBoolean()); // last byte is 0
	}

	@Test  @DisplayName("Test readByte and readUnsignedByte")
	void testReadByteAndUnsignedByte() {
		assertEquals(0x01, stream.readByte());
		assertEquals(0x02, stream.readUnsignedByte());
	}

	@Test  @DisplayName("Test readShort and readUnsignedShort")
	void testReadShortAndUnsignedShort () {
		// 0x01 << 8 | 0x02 = 0x0102 = 258
		assertEquals(0x0102, stream.readUnsignedShort());
		// 0x03 << 8 | 0x04 = 0x0304 = 772
		assertEquals((short) 772, stream.readShort());
	}

	@Test  @DisplayName("Test readChar")
	void testReadChar() {
		// 0x01 << 8 | 0x02 = 0x0102
		assertEquals('\u0102', stream.readChar());
	}

	@Test  @DisplayName("Test readInt")
	void testReadInt() {
		// 0x01 << 24 | 0x02 << 16 | 0x03 << 8 | 0x04 = 0x01020304
		assertEquals(0x01020304, stream.readInt());
		assertEquals(4, stream.readerIndex());
		stream.position(1);
		assertEquals(0x02030405, stream.readInt());
		assertEquals(5, stream.readerIndex());
	}

	@Test  @DisplayName("Test readMedium")
	void testReadMedium() {
		// 0x01 << 16 | 0x02 << 8 | 0x03 = 0x010203
		assertEquals(0x010203, stream.readMedium());
	}

	@Test  @DisplayName("Test readLong")
	void testReadLong () {
		// First int: 0x01020304, Second int: 0x05060708
		// 0x01020304L << 32 | 0x05060708L = 0x0102030405060708L
		assertEquals(0x0102030405060708L, stream.readLong());
		assertEquals(8, stream.readerIndex());
		stream.position(1);
		assertEquals(0x0203040506070809L, stream.readLong());
		assertEquals(9, stream.readerIndex());
	}

	@Test  @DisplayName("Test readFloat and readDouble")
	void testReadFloatAndDouble() {
		int intValue = stream.readInt();
		stream.position(0); // Reset to test float
		assertEquals(Float.intBitsToFloat(intValue), stream.readFloat());

		stream.position(0); // Reset to test double
		long longValue = stream.readLong();
		stream.position(0); // Reset to test double
		assertEquals(Double.longBitsToDouble(longValue), stream.readDouble());
	}

	@Test  @DisplayName("Test readLine with different line endings")
	void testReadLine() {
		stream.position(16); // Position at 'H' in "Hello\r\nWorld"
		assertEquals("Hello", stream.readLine());
		assertEquals("World\0", stream.readLine());
		assertNull(stream.readLine()); // EOF
	}

	@Test  @DisplayName("Test readUUID")
	void testReadUUID() {
		val uuidBytes = new byte[16];
		for (int i = 0; i < 16; i++)
				uuidBytes[i] = (byte)(i + 1);
		val uuidStream = new BAIS(uuidBytes);

		val expected = new UUID(0x0102030405060708L, 0x090A0B0C0D0E0F10L);
		assertEquals(expected, uuidStream.readUUID());
	}

	@Test  @DisplayName("Test readUTF with valid data")
	void testReadUTF() throws Exception {
		// Create a simple UTF string
		String testString = "Hello World";
		byte[] utfBytes = java.nio.charset.StandardCharsets.UTF_8.encode(testString).array();

		// Prepend length (short) as required by UTF format
		byte[] completeBytes = new byte[utfBytes.length + 2];
		completeBytes[0] = (byte) ((testString.length() >> 8) & 0xFF);
		completeBytes[1] = (byte) (testString.length() & 0xFF);
		System.arraycopy(utfBytes, 0, completeBytes, 2, utfBytes.length);

		BAIS utfStream = new BAIS(completeBytes);
		assertEquals(testString, utfStream.readUTF());
	}

	@Test
	@DisplayName("Test readUTF with insufficient data")
	void testReadUTFWithInsufficientData() throws UTFDataFormatException {
		byte[] shortData = new byte[]{0x00}; // Only length byte, no data
		BAIS shortStream = new BAIS(shortData);
		assertNull(shortStream.readUTF());
	}

	@Test
	@DisplayName("Test mark and reset functionality")
	void testMarkAndReset() {
		stream.mark(10);
		stream.readNBytes(5);
		stream.reset();
		assertEquals(0, stream.position());
	}

	@Test
	@DisplayName("Test skipNBytes")
	void testSkipNBytes() {
		stream.skipNBytes(5);
		assertEquals(5, stream.position());

		// Should not throw when skipping beyond available
		assertDoesNotThrow(() -> stream.skipNBytes(1000));
		assertEquals(testData.length, stream.position());
	}

	@Test
	@DisplayName("Test readFully")
	void testReadFully() {
		byte[] buffer = new byte[5];
		stream.readFully(buffer);
		assertArrayEquals(new byte[]{0x01, 0x02, 0x03, 0x04, 0x05}, buffer);
	}

	@Test
	@DisplayName("Test skipBytes")
	void testSkipBytes() {
		int skipped = stream.skipBytes(3);
		assertEquals(3, skipped);
		assertEquals(3, stream.position());
	}
}