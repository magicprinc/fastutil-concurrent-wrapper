package com.trivago.fastutilconcurrentwrapper.io;

import com.trivago.fastutilconcurrentwrapper.util.JBytes;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.UTFDataFormatException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

///@see BAOS
class BAOSTest {
	private BAOS stream;

	static String toHex (byte[] b){
		return HexFormat.of().formatHex(b);
	}

	@BeforeEach
	void setUp () {
		stream = new BAOS();
	}

	@Nested
	@DisplayName("Constructor Tests")
	class ConstructorTests {

		@Test
		@DisplayName("Default constructor creates stream with default capacity")
		void testDefaultConstructor() {
			BAOS defaultStream = new BAOS();

			assertNotNull(defaultStream.array());
			assertEquals(160, defaultStream.array().length); // DEFAULT_INITIAL_CAPACITY
			assertEquals(0, defaultStream.length());
			assertEquals(0, defaultStream.position());
		}

		@Test
		@DisplayName("Constructor with initial capacity")
		void testConstructorWithInitialCapacity() {
			BAOS customStream = new BAOS(100);

			assertNotNull(customStream.array());
			assertEquals(100, customStream.capacity());
			assertEquals(0, customStream.length());
			assertEquals(0, customStream.position());
		}

		@Test
		@DisplayName("Constructor with zero capacity")
		void testConstructorWithZeroCapacity() {
			BAOS zeroStream = new BAOS(0);

			assertNotNull(zeroStream.array());
			assertEquals(0, zeroStream.capacity());
			assertEquals(0, zeroStream.length());
			assertEquals(0, zeroStream.position());
		}

		@Test  @DisplayName("Constructor wrapping existing array")
		void testConstructorWithArray() {
			byte[] existingArray = {1, 2, 3, 4, 5};
			BAOS wrappedStream = new BAOS(existingArray);

			assertSame(existingArray, wrappedStream.array());
			assertEquals(0, wrappedStream.length()); // length starts at 0
			assertEquals(0, wrappedStream.position());
		}
	}

	@Nested  @DisplayName("Basic Write Operations")
	class BasicWriteTests {

		@Test  @DisplayName("Write single byte")
		void testWriteSingleByte() {
			stream.write(65); // 'A'

			assertEquals(1, stream.length());
			assertEquals(1, stream.position());
			assertEquals(65, stream.array()[0]);
		}

		@Test  @DisplayName("Write multiple single bytes")
		void testWriteMultipleSingleBytes() {
			stream.write(65);
			stream.write(66);
			stream.write(67);

			assertEquals(3, stream.length());
			assertEquals(3, stream.position());
			assertArrayEquals(new byte[]{65, 66, 67}, Arrays.copyOf(stream.array(), 3));
		}

		@Test  @DisplayName("Write byte array")
		void testWriteByteArray() {
			byte[] data = {1, 2, 3, 4, 5};
			stream.write(data);

			assertEquals(5, stream.length());
			assertEquals(5, stream.position());
			assertArrayEquals(data, Arrays.copyOf(stream.array(), 5));
		}

		@Test
		@DisplayName("Write byte array with offset and length")
		void testWriteByteArrayWithOffsetAndLength() {
			byte[] data = {0, 1, 2, 3, 4, 5, 6};
			stream.write(data, 2, 3); // write bytes 2, 3, 4

			assertEquals(3, stream.length());
			assertEquals(3, stream.position());
			assertArrayEquals(new byte[]{2, 3, 4}, Arrays.copyOf(stream.array(), 3));
		}

		@Test
		@DisplayName("Write empty byte array")
		void testWriteEmptyByteArray() {
			stream.write(new byte[0]);

			assertEquals(0, stream.length());
			assertEquals(0, stream.position());
		}
	}

	@Nested  @DisplayName("Array Growth Tests")
	class ArrayGrowthTests {

		@Test  @DisplayName("Array grows when capacity exceeded")
		void testArrayGrowth() {
			BAOS smallStream = new BAOS(2);

			smallStream.write(1);
			smallStream.write(2);
			assertEquals(2, smallStream.array().length);

			smallStream.write(3); // Should trigger growth
			assertTrue(smallStream.array().length > 2);
			assertEquals(3, smallStream.length());
			assertArrayEquals(new byte[]{1, 2, 3}, Arrays.copyOf(smallStream.array(), 3));
		}

		@Test
		@DisplayName("Array grows when writing large byte array")
		void testArrayGrowthWithLargeWrite() {
			BAOS smallStream = new BAOS(5);
			byte[] largeData = new byte[10];
			Arrays.fill(largeData, (byte) 42);

			smallStream.write(largeData);

			assertTrue(smallStream.array().length >= 10);
			assertEquals(10, smallStream.length());
			for (int i = 0; i < 10; i++)
					assertEquals(42, smallStream.array()[i]);
		}
	}

	@Nested  @DisplayName("Position and Length Management")
	class PositionLengthTests {

		@Test
		@DisplayName("Reset clears length and position")
		void testReset() {
			stream.write(new byte[]{1, 2, 3, 4, 5});
			assertEquals(5, stream.length());
			assertEquals(5, stream.position());

			stream.reset();
			assertEquals(0, stream.length());
			assertEquals(0, stream.position());
			// Array content should still be there, just not counted
		}

		@Test
		@DisplayName("Position can be set to different values")
		void testSetPosition() {
			stream.write(new byte[]{1, 2, 3, 4, 5});

			stream.position(2);
			assertEquals(2, stream.position());
			assertEquals(5, stream.length()); // length unchanged

			// Writing at position 2 should overwrite
			stream.write(99);
			assertEquals(3, stream.position());
			assertEquals(5, stream.length()); // length unchanged since we didn't extend
			assertEquals(99, stream.array()[2]);
		}

		@Test
		@DisplayName("Position beyond length extends length when writing")
		void testPositionBeyondLength() {
			stream.write(new byte[]{1, 2, 3});
			stream.position(5);
			stream.write(99);

			assertEquals(6, stream.position());
			assertEquals(6, stream.length()); // length extended
			assertEquals(99, stream.array()[5]);
		}

		@Test  @DisplayName("Position throws exception for values too large")
		void testPositionTooLarge() {
			assertThrows(IllegalArgumentException.class, () ->
				stream.position(((long) Integer.MAX_VALUE) + 1)
			);
		}

		@Test
		@DisplayName("Trim reduces array size to match length")
		void testTrim() {
			val largeStream = new BAOS(100);
			largeStream.write(new byte[]{1, 2, 3});

			assertEquals(100, largeStream.array().length);
			assertEquals(3, largeStream.length());

			largeStream.trim();
			assertEquals(3, largeStream.array().length);
			assertEquals(3, largeStream.length());
			assertArrayEquals(new byte[]{1, 2, 3}, largeStream.array());
		}
	}

	@Nested
	@DisplayName("Data Type Write Methods")
	class DataTypeWriteTests {

		@Test
		@DisplayName("Write boolean values")
		void testWriteBoolean() {
			stream.writeBoolean(true);
			stream.writeBoolean(false);

			assertEquals(2, stream.length());
			assertEquals(1, stream.array()[0]);
			assertEquals(0, stream.array()[1]);
		}

		@Test
		@DisplayName("Write byte values")
		void testWriteByte() {
			stream.writeByte(127);
			stream.writeByte(-128);

			assertEquals(2, stream.length());
			assertEquals(127, stream.array()[0]);
			assertEquals(-128, stream.array()[1]);
		}

		@Test
		@DisplayName("Write short values")
		void testWriteShort() {
			stream.writeShort(0x1234);

			assertEquals(2, stream.length());
			assertEquals(0x12, stream.array()[0] & 0xFF);
			assertEquals(0x34, stream.array()[1] & 0xFF);
		}

		@Test
		@DisplayName("Write char values")
		void testWriteChar() {
			stream.writeChar('A'); // 0x0041

			assertEquals(2, stream.length());
			assertEquals(0x00, stream.array()[0] & 0xFF);
			assertEquals(0x41, stream.array()[1] & 0xFF);
		}

		@Test
		@DisplayName("Write int values")
		void testWriteInt() {
			stream.writeInt(0x12345678);

			assertEquals(4, stream.length());
			assertEquals(0x12, stream.array()[0] & 0xFF);
			assertEquals(0x34, stream.array()[1] & 0xFF);
			assertEquals(0x56, stream.array()[2] & 0xFF);
			assertEquals(0x78, stream.array()[3] & 0xFF);
		}

		@Test
		@DisplayName("Write long values")
		void testWriteLong() {
			stream.writeLong(0x123456789ABCDEF0L);

			assertEquals(8, stream.length());
			assertEquals(0x12, stream.array()[0] & 0xFF);
			assertEquals(0x34, stream.array()[1] & 0xFF);
			assertEquals(0x56, stream.array()[2] & 0xFF);
			assertEquals(0x78, stream.array()[3] & 0xFF);
			assertEquals(0x9A, stream.array()[4] & 0xFF);
			assertEquals(0xBC, stream.array()[5] & 0xFF);
			assertEquals(0xDE, stream.array()[6] & 0xFF);
			assertEquals(0xF0, stream.array()[7] & 0xFF);
		}

		@Test
		@DisplayName("Write float values")
		void testWriteFloat() {
			float value = 3.14159f;
			stream.writeFloat(value);

			assertEquals(4, stream.length());

			// Verify by reconstructing the float
			int intBits = ((stream.array()[0] & 0xFF) << 24) |
				((stream.array()[1] & 0xFF) << 16) |
				((stream.array()[2] & 0xFF) << 8) |
				(stream.array()[3] & 0xFF);
			assertEquals(value, Float.intBitsToFloat(intBits), 0.0001f);
		}

		@Test
		@DisplayName("Write double values")
		void testWriteDouble() {
			double value = 3.141592653589793;
			stream.writeDouble(value);

			assertEquals(8, stream.length());

			// Verify by reconstructing the double
			long longBits = 0;
			for (int i = 0; i < 8; i++) {
				longBits = (longBits << 8) | (stream.array()[i] & 0xFF);
			}
			assertEquals(value, Double.longBitsToDouble(longBits), 0.0000000001);
		}
	}

	@Nested
	@DisplayName("String Write Methods")
	class StringWriteTests {

		@Test
		@DisplayName("Write bytes from string (deprecated method)")
		void testWriteBytes() {
			stream.writeBytes("ABC");

			assertEquals(3, stream.length());
			assertEquals(65, stream.array()[0]); // 'A'
			assertEquals(66, stream.array()[1]); // 'B'
			assertEquals(67, stream.array()[2]); // 'C'
		}

		@Test
		@DisplayName("Write chars from string")
		void testWriteChars() {
			stream.writeChars("AB");

			assertEquals(4, stream.length()); // 2 chars * 2 bytes each
			assertEquals(0, stream.array()[0]); // 'A' high byte
			assertEquals(65, stream.array()[1]); // 'A' low byte
			assertEquals(0, stream.array()[2]); // 'B' high byte
			assertEquals(66, stream.array()[3]); // 'B' low byte
		}

		@Test
		@DisplayName("Write UTF-8 string")
		void testWriteUTF() {
			String testString = "Hello 世界";
			stream.writeUTF(testString);

			byte[] expected = testString.getBytes(StandardCharsets.UTF_8);
			assertEquals(expected.length+2, stream.length());
			assertArrayEquals(expected, Arrays.copyOfRange(stream.array(), 2, stream.size()));
		}

		@Test
		@DisplayName("Write empty string")
		void testWriteEmptyString() {
			stream.writeUTF("");

			assertEquals(2, stream.length());
		}
	}

	@Nested  @DisplayName("Output and Conversion Tests")
	class OutputConversionTests {
		@Test  @DisplayName("toByteArray returns copy when array not full")
		void testToByteArrayCopy() {
			stream.write(new byte[]{1, 2, 3});

			byte[] result = stream.toByteArray();

			assertNotSame(stream.array(), result); // Should be a copy
			assertEquals(3, result.length);
			assertArrayEquals(new byte[]{1, 2, 3}, result);
		}

		@Test
		@DisplayName("toByteArray returns empty array when empty")
		void testToByteArrayEmpty() {
			byte[] result = stream.toByteArray();

			assertEquals(0, result.length);
		}
	}

	@Nested
	@DisplayName("Edge Cases and Error Conditions")
	class EdgeCaseTests {

		@Test
		@DisplayName("Close method does nothing")
		void testClose() {
			stream.write(new byte[]{1, 2, 3});

			assertDoesNotThrow(() -> stream.close());

			// Stream should still be usable
			assertEquals(3, stream.length());
			stream.write(4);
			assertEquals(4, stream.length());
		}

		@Test
		@DisplayName("Write with invalid offset and length throws exception")
		void testWriteInvalidOffsetLength() {
			byte[] data = new byte[5];

			// Invalid offset
			assertThrows(Exception.class, () -> stream.write(data, -1, 3));

			// Invalid length
			assertThrows(Exception.class, () -> stream.write(data, 0, 10));

			// Invalid offset + length
			assertThrows(Exception.class, () -> stream.write(data, 3, 4));
		}

		@Test
		@DisplayName("Large data writes work correctly")
		void testLargeDataWrite() {
			byte[] largeData = new byte[10000];
			for (int i = 0; i < largeData.length; i++) {
				largeData[i] = (byte) (i % 256);
			}

			stream.write(largeData);

			assertEquals(10000, stream.length());
			assertTrue(stream.array().length >= 10000);

			byte[] result = stream.toByteArray();
			assertArrayEquals(largeData, result);
		}

		@Test
		@DisplayName("Multiple operations maintain consistency")
		void testMultipleOperationsConsistency() {
			// Mix of different write operations
			stream.writeInt(42);
			stream.writeUTF("test");
			stream.writeBoolean(true);
			stream.write(new byte[]{1, 2, 3});

			int expectedLength = 4 + 4 + 1 + 3+2; // int + "test" bytes + boolean + 3 bytes
			assertEquals(expectedLength, stream.length());
			assertEquals(expectedLength, stream.position());

			// Reset and verify
			stream.reset();
			assertEquals(0, stream.length());
			assertEquals(0, stream.position());

			// Should be able to write again
			stream.write(99);
			assertEquals(1, stream.length());
			assertEquals(99, stream.array()[0]);
		}
	}

	@Test
	void basic () {
		var os = new BAOS();
		os.close();
		os.write(0);
		os.writeBoolean(true);
		os.writeBoolean(false);
		os.writeByte(3);
		os.writeShort(0x4321);
		os.writeChar('Я');
		os.writeInt(0x8765_4321);
		os.writeLong(0x1020_3040_5060_7080L);
		os.write(new byte[]{0x71,0x72,0x73});

		assertEquals("000100034321042f876543211020304050607080717273", HexFormat.of().formatHex(os.toByteArray()));
	}

	@Test
	void sameDO () throws IOException {
		var a = new BAOS();
		var x = new ByteArrayOutputStream();
		var b = (DataOutput) new DataOutputStream(x);

		a.write(255);
		b.write(255);
		a.write(Integer.MAX_VALUE);
		b.write(Integer.MAX_VALUE);
		var tmp = "Привет!".getBytes();
		a.write(tmp);
		b.write(tmp);

		a.writeByte(255);
		b.writeByte(255);

		a.writeBoolean(true);
		b.writeBoolean(true);
		a.writeBoolean(false);
		b.writeBoolean(false);

		a.writeShort(Short.MIN_VALUE);
		b.writeShort(Short.MIN_VALUE);
		a.writeShort(0x1234);
		b.writeShort(0x1234);
		assertArrayEquals(a.toByteArray(), x.toByteArray());

		a.writeChar('Я');
		b.writeChar('Я');
		a.writeChar('1');
		b.writeChar('1');

		a.writeInt(Integer.MIN_VALUE);
		b.writeInt(Integer.MIN_VALUE);
		a.writeInt(0x12345678);
		b.writeInt(0x12345678);

		a.writeLong(Integer.MIN_VALUE);
		b.writeLong(Integer.MIN_VALUE);
		a.writeLong(0x12345678);
		b.writeLong(0x12345678);
		a.writeLong(Long.MIN_VALUE);
		b.writeLong(Long.MIN_VALUE);
		a.writeLong(0x12345678_87654321L);
		b.writeLong(0x12345678_87654321L);

		a.writeFloat(27247.24794f);
		b.writeFloat(27247.24794f);
		a.writeFloat(Float.NaN);
		b.writeFloat(Float.NaN);

		a.writeDouble(27247.24794f);
		b.writeDouble(27247.24794f);
		a.writeDouble(Float.NaN);
		b.writeDouble(Float.NaN);

		a.writeBytes("Ура👍");
		b.writeBytes("Ура👍");

		a.writeChars("Ура👍");
		b.writeChars("Ура👍");

		assertArrayEquals(a.toByteArray(), x.toByteArray());
		assertEquals(105, a.length());
		assertEquals(105, x.size());

		a.reset();
		x.reset();

		a.writeUTF("\0\0\0 \t\rHello Проверка 💯");
		b.writeUTF("\0\0\0 \t\rHello Проверка 💯");

		assertEquals(toHex(a.toByteArray()), toHex(x.toByteArray()));
		assertArrayEquals(a.toByteArray(), x.toByteArray());
		assertEquals(40, a.length());

		a.writeUTF("Я".repeat(31_000));
		b.writeUTF("Я".repeat(31_000));
		assertArrayEquals(a.toByteArray(), x.toByteArray());
		assertEquals(62042, a.length());
		assertEquals(62042, a.size());

		assertThrows(IllegalArgumentException.class, ()->a.writeUTF("Я".repeat(33_000)));
		assertThrows(UTFDataFormatException.class, ()->b.writeUTF("Я".repeat(33_000)));
		assertArrayEquals(a.toByteArray(), x.toByteArray());
		assertEquals(62042, a.length());
		assertEquals(62042, a.size());
		assertEquals(a.array().length, a.capacity());
	}

	@Test
	void asStr () {
		var x = new BAOS();
		x.writeUtf8Char('Я');
		x.writeUtf8Char('$');
		x.writeUtf8Char('€');
		assertEquals("Я$€", x.toString(StandardCharsets.UTF_8));

		x.reset();
		x.writeChars("Я$€");
		assertEquals("Я$€", x.toString(StandardCharsets.UTF_16BE));
	}

	@Test
	void testBasicReader () throws IOException {
		var w = new BAOS(16);
		var r = new BAIS(w.array());
		r.mark(0);
		assertEquals(0, r.peek());
		assertEquals(0, r.peek());

		for (int i = 0; i < w.array().length; i++){
			w.array()[i] = (byte) i;
		}
		assertEquals("000102030405060708090a0b0c0d0e0f", toHex(r.readAllBytes()));
		assertEquals("", toHex(r.readAllBytes()));
		assertEquals(-1, r.peek());

		r.reset();
		assertFalse(r.readBoolean());
		assertTrue(r.readBoolean());
		assertTrue(r.readBoolean());
		assertEquals(3, r.peek());

		assertEquals(3, r.readByte());

		assertEquals(4, r.readUnsignedByte());

		assertEquals(0x0506, r.readShort());
		assertEquals(0x0708, r.readUnsignedShort());

		assertEquals(0x090A, r.readChar());

		assertEquals(0x0B0C_0D0E, r.readInt());

		r.reset();
		assertEquals(0x00010203_04050607L, r.readLong());

		r.reset();
		w.writeFloat(27247.24794f);  w.writeFloat(Float.NaN);
		assertEquals(27247.24794f, r.readFloat());
		assertTrue(Float.isNaN(r.readFloat()));

		r.reset();  w.reset();
		w.writeDouble(27247.24794f);  w.writeDouble(Double.NaN);
		assertEquals(27247.24794f, r.readDouble());
		assertTrue(Double.isNaN(r.readDouble()));

		r.reset();  w.reset();
		w.writeBytes("\0Hello\r\n Hi! \n\r");
		w.trim();
		r.array(w.array(), 0, w.array().length);
		assertEquals("\0Hello", r.readLine());
		assertEquals(" Hi! ", r.readLine());
		assertEquals("", r.readLine());
		assertNull(r.readLine());
		assertNull(r.readLine());

		w = new BAOS(4);
		w.writeBytes("Test");
		r = new BAIS(w.array());
		assertEquals("Test", r.readLine());
		assertNull(r.readLine());
		//

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		String s = "\0\0\0Hello Привет 💯🤝\r\n\t";
		dos.writeUTF(s);
		r = new BAIS(baos.toByteArray());
		assertEquals(s, r.readUTF());
	}

	public static final byte IE_CONCATENATED_SHORT_MESSAGES_16BIT_REF = 8;
	public static final byte IE_APPLICATION_PORT_ADDRESSING_SCHEME_16BIT_ADDR = 0x05;

	@Test
	void demo () {
		String latin1text = "   latin1 text is ok";

		var sms = new BAOS(160);
		sms.writeByte(0);// udh len

		sms.writeByte(IE_CONCATENATED_SHORT_MESSAGES_16BIT_REF);
		sms.writeByte(4);
		sms.writeShort(12345);
		sms.writeByte(2);
		sms.writeByte(1);

		sms.writeByte(IE_APPLICATION_PORT_ADDRESSING_SCHEME_16BIT_ADDR);
		sms.write(4);
		sms.writeShort(8080);
		sms.writeShort(12728);

		sms.array()[0] = (byte)(sms.position() -1);

		sms.writeBytes(latin1text);

		assertEquals(
			"0c08043039020105041f9031b82020206c6174696e312074657874206973206f6b",
			toHex(sms.toByteArray())
		);
		//
		var parse = new BAIS(sms.toByteArray());
		int len = parse.readByte();
		assertEquals(12, len);
		assertEquals(IE_CONCATENATED_SHORT_MESSAGES_16BIT_REF, parse.readByte());
		assertEquals(4, parse.readByte());
		assertEquals(12345, parse.readShort());
		assertEquals(2, parse.readByte());
		assertEquals(1, parse.readByte());

		assertEquals(IE_APPLICATION_PORT_ADDRESSING_SCHEME_16BIT_ADDR, parse.readByte());
		assertEquals(4, parse.readByte());
		assertEquals(8080, parse.readShort());
		assertEquals(12728, parse.readShort());

		assertEquals(latin1text, parse.readLine());// just as a bad example!
	}

	@Test
	void testFBAISCtor () {
		var a = new byte[10];
		JBytes.setAll(a, i->i);
		var is = new BAIS(a, 1, 100);
		assertEquals(0, is.markIndex());// это position [..offset.. (0..length) ..array.length]
		assertEquals(1, is.offset);
		assertEquals(0, is.position());
		assertEquals(1, is.peek());
		assertEquals(1, is.read());
		assertEquals("0203040506070809", toHex(is.readAllBytes()));
		assertEquals(9, is.length());
		assertEquals(9, is.limit());
	}

	// fastUtil original tests

	@Test
	public void testWrite() {
		BAOS fbaos = new BAOS();
		fbaos.write(1);
		fbaos.write(2);
		assertEquals(1, fbaos.array()[0]);
		assertEquals(2, fbaos.array()[1]);
		assertEquals(2, fbaos.length());
		assertEquals(2, fbaos.size());
		assertEquals(2, fbaos.position());
		assertEquals(2, fbaos.writerIndex());
		fbaos.position(1);
		fbaos.write(3);
		assertEquals(2, fbaos.position());
		assertEquals(2, fbaos.length());
		assertEquals(2, fbaos.size());
		assertEquals(3, fbaos.array()[1]);
		fbaos.write(4);
		assertEquals(3, fbaos.length());
		assertEquals(3, fbaos.size());
		assertEquals(4, fbaos.array()[2]);

		for (int i = 0; i < 14; i++) fbaos.write(i + 10);
		assertEquals(17, fbaos.length());
		for (int i = 0; i < 14; i++)
				assertEquals(i + 10, fbaos.array()[3 + i]);
	}

	@Test
	public void testWriteArray() throws IOException {
		BAOS fbaos = new BAOS();
		fbaos.write(1);
		fbaos.write(2);
		fbaos.write(3);

		byte[] a = new byte[14];
		for(int i = 0; i < 14; i++) a[i] = (byte)(i + 10);
		fbaos.write(a);
		assertEquals(17, fbaos.length());
		assertEquals(17, fbaos.size());
		assertEquals(1, fbaos.array()[0]);
		assertEquals(2, fbaos.array()[1]);
		assertEquals(3, fbaos.array()[2]);
		for(int i = 0; i < 14; i++) assertEquals(i + 10, fbaos.array()[3 + i]);

		fbaos.write(a);
		assertEquals(31, fbaos.length());
		for(int i = 0; i < 14; i++) assertEquals(i + 10, fbaos.array()[17 + i]);

		fbaos = new BAOS();
		fbaos.write(1);
		fbaos.write(2);
		fbaos.write(3);
		fbaos.position(2);

		fbaos.write(a);
		assertEquals(16, fbaos.length());
		assertEquals(1, fbaos.array()[0]);
		assertEquals(2, fbaos.array()[1]);
		for(int i = 0; i < 14; i++) assertEquals(i + 10, fbaos.array()[2 + i]);

		fbaos = new BAOS();
		fbaos.write(1);
		fbaos.write(2);
		fbaos.write(3);
		fbaos.write(4);
		fbaos.position(3);

		fbaos.write(a);
		assertEquals(17, fbaos.length());
		assertEquals(1, fbaos.array()[0]);
		assertEquals(2, fbaos.array()[1]);
		assertEquals(3, fbaos.array()[2]);
		for(int i = 0; i < 14; i++) assertEquals(i + 10, fbaos.array()[3 + i]);
	}

	@Test
	public void testPositionWrite () {
		val fbaos = new BAOS();
		fbaos.position(1);
		fbaos.write(1);
		assertEquals(2, fbaos.length());
		assertEquals(2, fbaos.size());
		assertEquals(2, fbaos.position());
		assertEquals(2, fbaos.writerIndex());
	}

	@Test
	public void testPositionWrite2 () {
		val fbaos = new BAOS();
		fbaos.position(fbaos.array().length + 2);
		fbaos.write(1);
		assertEquals(163, fbaos.length());
		assertEquals(163, fbaos.size());
		assertEquals(163, fbaos.position());
		assertEquals(163, fbaos.writerIndex());
	}

	@Test
	void _bug_write_array () {
		val o = new BAOS();
		o.write(JBytes.bytes(1,2,3,4,5));
		assertEquals(5, o.size());
		assertEquals(5, o.length());
		assertEquals(5, o.position());
		assertEquals(5, o.writerIndex());

		o.position(0);
		o.write(JBytes.bytes(1,2,3));
		assertEquals(5, o.size());
		assertEquals(5, o.length());
		assertEquals(3, o.position());
		assertEquals(3, o.writerIndex());
	}

	@Test
	void _bug_write_array0 () {
		val o = new it.unimi.dsi.fastutil.io.FastByteArrayOutputStream ();
		o.write(new byte[]{1,2,3,4,5});
		assertEquals(5, o.length());
		assertEquals(5, o.position());

		o.position(0);
		o.write(new byte[]{1,2,3});
		assertEquals(5, o.length());
		assertEquals(0, o.position());//!!! MUST be 3: 0+3
	}

	@Test
	void _setArray () {
		val o = new BAOS();
		o.write(JBytes.bytes(1,2,3,4,5));

		o.array(new byte[]{11, 12});
		assertEquals(2, o.capacity());
		assertEquals(0, o.size());
		assertEquals(0, o.length());
		assertEquals(0, o.position());
		assertEquals(0, o.writerIndex());

		assertThrows(IndexOutOfBoundsException.class, ()->o.write(JBytes.bytes(1,2,3), 1, 3));

		o.writerIndex(o.capacity());
		assertEquals(2, o.position());
		assertEquals(2, o.writerIndex());
		o.writeLong(0x1234567812345678L);
		assertEquals(13, o.capacity());
		assertEquals(10, o.size());
		assertEquals(10, o.length());
		assertEquals(10, o.position());
		assertEquals(10, o.writerIndex());
	}

	@Test
	void _spring_ResizableByteArrayOutputStream () {
		val o = new BAOS();
		o.writeBytes("0123456789 Test 0123456789 ");
		assertEquals(160, o.capacity());
		assertEquals(27, o.size());
		assertEquals(27, o.length());
		assertEquals(27, o.position());
		assertEquals(27, o.writerIndex());

		o.resize(10);
		assertEquals("0123456789", o.toString());
		assertEquals(10, o.capacity());
		assertEquals(10, o.size());
		assertEquals(10, o.length());
		assertEquals(27, o.position());
		assertEquals(27, o.writerIndex());

		o.resize(20);
		assertEquals("0123456789", o.toString());
		assertEquals(20, o.capacity());
		assertEquals(10, o.size());
		assertEquals(10, o.length());
		assertEquals(27, o.position());
		assertEquals(27, o.writerIndex());
		//

		o.grow(20);
		assertEquals("0123456789", o.toString());
		assertEquals(30, o.capacity());
		assertEquals(10, o.size());
		assertEquals(10, o.length());
		assertEquals(27, o.position());
		assertEquals(27, o.writerIndex());

		o.grow(100);
		assertEquals("0123456789", o.toString());
		assertEquals(110, o.capacity());// 10 было занято и 100 сверху
		assertEquals(10, o.size());

		o.grow(10);
		assertEquals("0123456789", o.toString());
		assertEquals(110, o.capacity());
		assertEquals(10, o.size());

		o.writerIndex(109);
		o.write('x');
		assertEquals(110, o.capacity());
		assertEquals(110, o.size());
		assertEquals(110, o.position());

		o.grow(10);
		assertEquals(165, o.capacity());// 110 * 1.5 = 165
		assertEquals(110, o.size());
	}

	static final UUID uuid = UUID.fromString("5a0372e3-3af7-4dbe-b68c-31e3aa5b75d0");

	@Test
	void _medium_guid () throws IOException {
		val o = new BAOS();
		o.writeMedium(0x313233);// '123'
		o.writeUUID(uuid);

		val w = new BAOS();
		o.writeTo(w);

		assertEquals(160, o.capacity());
		assertEquals(19, o.size());// 16+3
		assertEquals(19, o.writerIndex());

		assertEquals(160, w.capacity());
		assertEquals(19, w.size());// 16+3
		assertEquals(19, w.writerIndex());

		val is = new BAIS(o.toByteArray());
		assertEquals(0x313233, is.readMedium());
		assertEquals(uuid, is.readUUID());
	}

	@Test
	void _oos () throws IOException, ClassNotFoundException {
		ArrayList<Long> list = new ArrayList<>();
		list.add(1L);  list.add(2L);  list.add(3L);
		HashMap<Serializable,Serializable> map = new HashMap<>();
		map.put("Key1", uuid);
		map.put(true, 42.0);
		map.put('Z', list);

		val o = new BAOS();
		o.writeObject(map);
		assertEquals(540, o.capacity());
		assertEquals(467, o.size());// 16+3
		assertEquals(o.size(), o.writerIndex());

		val is = new BAIS(o.toByteArray());
		val m2 = is.readObject();
		assertEquals(map, m2);
	}

	/// 500k: 5_760 ms, op/s=86_802
	@Test
	void _benchmark () {
		IntStream.range(0, 2).forEach(__->{
			long t = System.nanoTime();
			for (int loop = 1; loop < 500_000; loop++){
				val os = new BAOS();
				val is = new BAIS(os.array());

				for (int i = 0; i < 133; i++){
					os.writeMedium(0xAB_CD_EF);
					os.writeInt(0x1234_5678);
					os.writeLong(0x91929394_95969798L);
					os.writeDouble(Math.PI);
					os.writeUUID(uuid);
					os.writeBytes("Answer42!");
				}
				assertEquals(9222, os.capacity());
				assertEquals(6384, os.length());
				assertEquals(6384, os.size());
				assertEquals(6384, os.position());
				assertEquals(6384, os.writerIndex());

				is.array(os.array(), 0, os.size());

				for (int i = 0; i < 133; i++){
					assertEquals(0xAB_CD_EF, is.readMedium());
					assertEquals(0x1234_5678, is.readInt());
					assertEquals(0x91929394_95969798L, is.readLong());
					assertEquals(Math.PI, is.readDouble());
					assertEquals(uuid, is.readUUID());
					assertEquals("Answer42!", is.readLatin1String(9));
				}
				assertEquals(9222, is.array().length);
				assertEquals(6384, is.length());
				assertEquals(6384, is.limit());
				assertEquals(6384, is.position());
				assertEquals(6384, is.readerIndex());
			}
			System.out.println(benchToStr(t, System.nanoTime(), 500_000));
		});
	}

	public static String benchToStr (long start, long end, long totalOperations) {
		assert start <= end : "start ≤ end, but " + start + " > " + end;

		long elapsed = end - start;

		return String.format(Locale.ENGLISH, "%.3f, op/s=%.2f", elapsed/1000/1000.0, totalOperations * 1_000_000_000.0 / elapsed);
	}

	@Test
	void _ascii () {
		val baos = new BAOS();
		for (int i = 0; i<=255; i++)
			baos.write(i);

		assertEquals(256, baos.size());
		assertEquals(256, baos.position());

		val bais = new BAIS(baos);
		assertEquals(256, bais.available());
		assertEquals(256, bais.limit());

		String sss = baos.toString();
		assertEquals(sss, bais.readLatin1String(256));
		assertEquals(0, bais.available());
		assertEquals(256, bais.limit());
		//
		bais.readerIndex(0);
		for (int i = 0; i<=255; i++)
				assertEquals(i, bais.read());
		//
		baos.reset();
		baos.writeBytes(sss);
		assertEquals(sss, baos.toString());
		assertEquals(sss, baos.toString(StandardCharsets.ISO_8859_1));
	}
}