package com.trivago.fastutilconcurrentwrapper.util;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;

import static it.unimi.dsi.fastutil.bytes.ByteArrays.EMPTY_ARRAY;

/**
 @see java.util.Arrays
 @see it.unimi.dsi.fastutil.Arrays
 @see java.lang.reflect.Array
 @see it.unimi.dsi.fastutil.bytes.ByteArrays
 @see java.io.DataOutputStream
*/
public class JBytes {
	public static final byte B0 = 0;
	public static final byte B1 = 1;
	public static final byte B2 = 2;
	public static final byte N1 = -1;// 255

	public static byte[] safe (byte @Nullable [] b) {
		return b != null ? b
				: EMPTY_ARRAY;
	}

	public static boolean isEmpty (byte @Nullable [] b) {
		return b == null || b.length <= 0;
	}

	public static boolean nonEmpty (byte @Nullable [] b) {
		return b != null && b.length > 0;
	}

	public static int len (byte @Nullable [] b){
		return b != null ? b.length : 0;
	}

	/// Simple bytes (from ints)
	public static byte[] bytes (int... elements) {
		int len = elements != null ? elements.length : 0;
		val b = new byte[len];
		for (int i = 0; i < len; i++)
				b[i] = (byte) elements[i];
		return b;
	}

	/**
	 A common case of copying arrays from start to [start .. min(len1,len2))
	 Used when increasing or decreasing the array length.

	 @see System#arraycopy(Object, int, Object, int, int)
	 @see java.util.Arrays#copyOf
	 @see java.util.Arrays#copyOfRange
	 @see java.util.Arrays#copyOfRangeInt
	 @see org.apache.commons.lang3.ArrayUtils#addAll
	 @see com.google.common.collect.ObjectArrays#concat
	 @see it.unimi.dsi.fastutil.objects.ObjectArrays#grow
	 @see it.unimi.dsi.fastutil.objects.ObjectArrays#trim
	 */
	public static <A> A arraycopy (@Nullable A src, A dst, @PositiveOrZero int len) {
		if (src != null)
				System.arraycopy(src,0,  dst,0, len);
		return dst;
	}

	/**
	 Similar to JDK and FastUtil, but fixes mistakes and doesn't clone.
	 @see Arrays#copyOfRange(byte[], int, int)
	 @see it.unimi.dsi.fastutil.bytes.ByteArrays#copy(byte[], int, int)
	 */
	public static byte[] copyOrSame (byte @Nullable[] src, @PositiveOrZero int from, @PositiveOrZero int toNewLen) {
		if (src == null || (from <= 0 && toNewLen >= src.length))
				return src;

		int tail = src.length - from;
		if (tail > toNewLen){ tail = toNewLen; }
		byte[] b = new byte[tail];
		System.arraycopy(src,from,  b,0, tail);
		return b;
	}


	/**
	 Similar to {@link java.util.Arrays#setAll(int[], IntUnaryOperator)} but for byte and returns argument-array (e.g. newly created, no extra line with assignment)
	 One argument version with index.
	 @see #setAll(byte[], IntBinaryOperator)
	 */
	public static byte[] setAll (byte @Nullable [] array, IntUnaryOperator generator) {
		if (array == null)
				return EMPTY_ARRAY;

		for (int i = 0, len = array.length; i < len; i++){
			int b = generator.applyAsInt(i);
			if (b < Short.MIN_VALUE){ break; }// pseudo command `break`
			if (b > Short.MAX_VALUE){ continue; }// skip
			array[i] = (byte) b;
		}
		return array;
	}

	/**
	 Similar to {@link java.util.Arrays#setAll(int[], IntUnaryOperator)} but for byte and returns argument-array (e.g. newly created, no extra line with assignment)
	 Two argument version with index and current byte.
	 @see #setAll(byte[], IntUnaryOperator)
	 */
	public static byte[] setAll (byte @Nullable [] array, IntBinaryOperator modifier) {
		if (array == null)
				return EMPTY_ARRAY;

		for (int i = 0, len = array.length; i < len; i++){
			int b = modifier.applyAsInt(i, array[i]);
			if (b < Short.MIN_VALUE){ break; }// pseudo command `break`
			if (b > Short.MAX_VALUE){ continue; }// skip
			array[i] = (byte) b;
		}
		return array;
	}

	/**
	 Для работы с bytes лучше ByteBuf (netty, vert.x, oIo) или Input/OutputStream
	 @see Arrays#copyOf
	 @see Arrays#copyOfRange
	 @see Arrays#copyOfRangeByte
	 @see JSystem#arraycopy(Object, Object, int)
	 @see #concatClone(byte[], byte[])
	 @see JBuffer
	 @see it.unimi.dsi.fastutil.bytes.ByteArrays#swap
	 @see it.unimi.dsi.fastutil.bytes.ByteArrays#trim
	 @see it.unimi.dsi.fastutil.bytes.ByteArrays#grow
	 @see it.unimi.dsi.fastutil.bytes.ByteArrays#setLength

	 @see org.apache.commons.lang3.ArrayUtils#addAll

	 @see com.google.common.collect.ObjectArrays#concat
	 @see it.unimi.dsi.fastutil.objects.ObjectArrays#grow
	 */
	public static byte[] concatOrSame (byte @Nullable [] a, byte @Nullable [] b) {
		int len1;
		if (a == null || (len1 = a.length) <= 0){
			return b == null || b.length <= 0 ? EMPTY_ARRAY
					: b;
		}//×a

		int len2;
		if (b == null || (len2 = b.length) <= 0){
			return a;
		}//×b

		byte[] c = new byte[len1 + len2];
		System.arraycopy(a,0, c,0,  len1);
		System.arraycopy(b,0, c,len1,  len2);
		return c;
	}

	/** Similar to {@link #concatOrSame}, but if only one array is not empty → returns cloned array */
	public static byte[] concatClone (byte @Nullable [] a, byte @Nullable [] b) {
		int len1;
		if (a == null || (len1 = a.length) <= 0){
			return b == null || b.length <= 0 ? EMPTY_ARRAY
					: b.clone();
		}//×a

		int len2;
		if (b == null || (len2 = b.length) <= 0){
			return a.clone();
		}//×b

		byte[] c = new byte[len1 + len2];
		System.arraycopy(a,0, c,0,  len1);
		System.arraycopy(b,0, c,len1,  len2);
		return c;
	}

	/** Concat multiple byte arrays */
	public static byte[] concatMulti (byte @Nullable [] @Nullable ... byteArrays) {
		if (CFUtil.blankVarargs(byteArrays))
				return EMPTY_ARRAY;

		int total = 0;// total length = all bytes in all arrays
		for (byte[] src : byteArrays){
			if (src != null)
					total += src.length;
		}

		val result = new byte[total];
		int j = 0;

		for (byte[] src : byteArrays){
			if (src != null && src.length > 0){
				System.arraycopy(src,0, result,j,  src.length);
				j += src.length;
			}
		}
		return result;
	}

	public static byte[] longToBytes (long longValue) {
		val b = new byte[8];
		DirectByteArrayAccess.setLong(b, 0, longValue);
		return b;
	}

	public static long bytesToLong (byte[] bytes) {
		return DirectByteArrayAccess.getLong(bytes, 0);
	}


	/**
	 * Utility methods for packing/unpacking primitive values in/out of byte arrays
	 * using {@linkplain ByteOrder#BIG_ENDIAN big endian order} (aka. "network order").
	 * <p>
	 * All methods in this class will throw an {@linkplain NullPointerException} if {@code null} is
	 * passed in as a method parameter for a byte array.
	 * @see java.io.DataOutputStream
	 * @see java.io.DataInputStream
	 * @see jdk.internal.util.ByteArray
	 * @see jdk.internal.util.ByteArrayLittleEndian
	 * @see com.dynatrace.hash4j.internal.ByteArrayUtil
	 */
	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	public static final class DirectByteArrayAccess {
		private static final VarHandle SHORT = create(short[].class);
		private static final VarHandle CHAR = create(char[].class);
		private static final VarHandle INT = create(int[].class);
		private static final VarHandle FLOAT = create(float[].class);
		private static final VarHandle LONG = create(long[].class);
		private static final VarHandle DOUBLE = create(double[].class);

		// Methods for unpacking primitive values from byte arrays starting at  a given offset.

		/**
		 * {@return a {@code boolean} from the provided {@code array} at the given {@code offset}}.
		 *
		 * @param array  to read a value from.
		 * @param offset where extraction in the array should begin
		 * @throws IndexOutOfBoundsException if the provided {@code offset} is outside
		 *                                   the range [0, array.length - 1]
		 * @see #setBoolean(byte[], int, boolean)
		 */
		public static boolean getBoolean(byte[] array, int offset) {
			return array[offset] != 0;
		}

		/**
		 * {@return a {@code char} from the provided {@code array} at the given {@code offset}
		 * using big endian order}.
		 * <p>
		 * There are no access alignment requirements.
		 *
		 * @param array  to get a value from.
		 * @param offset where extraction in the array should begin
		 * @throws IndexOutOfBoundsException if the provided {@code offset} is outside
		 *                                   the range [0, array.length - 2]
		 * @see #setChar(byte[], int, char)
		 */
		public static char getChar(byte[] array, int offset) {
			return (char) CHAR.get(array, offset);
		}

		/**
		 * {@return a {@code short} from the provided {@code array} at the given {@code offset}
		 * using big endian order}.
		 * <p>
		 * There are no access alignment requirements.
		 *
		 * @param array  to get a value from.
		 * @param offset where extraction in the array should begin
		 * @return a {@code short} from the array
		 * @throws IndexOutOfBoundsException if the provided {@code offset} is outside
		 *                                   the range [0, array.length - 2]
		 * @see #setShort(byte[], int, short)
		 */
		public static short getShort(byte[] array, int offset) {
			return (short) SHORT.get(array, offset);
		}

		/**
		 * {@return an {@code unsigned short} from the provided {@code array} at the given {@code offset}
		 * using big endian order}.
		 * <p>
		 * There are no access alignment requirements.
		 *
		 * @param array  to get a value from.
		 * @param offset where extraction in the array should begin
		 * @return an {@code int} representing an unsigned short from the array
		 * @throws IndexOutOfBoundsException if the provided {@code offset} is outside
		 *                                   the range [0, array.length - 2]
		 * @see #setUnsignedShort(byte[], int, int)
		 */
		public static int getUnsignedShort(byte[] array, int offset) {
			return Short.toUnsignedInt((short) SHORT.get(array, offset));
		}

		/**
		 * {@return an {@code int} from the provided {@code array} at the given {@code offset}
		 * using big endian order}.
		 * <p>
		 * There are no access alignment requirements.
		 *
		 * @param array  to get a value from.
		 * @param offset where extraction in the array should begin
		 * @throws IndexOutOfBoundsException if the provided {@code offset} is outside
		 *                                   the range [0, array.length - 4]
		 * @see #setInt(byte[], int, int)
		 */
		public static int getInt(byte[] array, int offset) {
			return (int) INT.get(array, offset);
		}

		/**
		 * {@return a {@code float} from the provided {@code array} at the given {@code offset}
		 * using big endian order}.
		 * <p>
		 * Variants of {@linkplain Float#NaN } values are canonized to a single NaN value.
		 * <p>
		 * There are no access alignment requirements.
		 *
		 * @param array  to get a value from.
		 * @param offset where extraction in the array should begin
		 * @throws IndexOutOfBoundsException if the provided {@code offset} is outside
		 *                                   the range [0, array.length - 4]
		 * @see #setFloat(byte[], int, float)
		 */
		public static float getFloat(byte[] array, int offset) {
			// Using Float.intBitsToFloat collapses NaN values to a single
			// "canonical" NaN value
			return Float.intBitsToFloat((int) INT.get(array, offset));
		}

		/**
		 * {@return a {@code float} from the provided {@code array} at the given {@code offset}
		 * using big endian order}.
		 * <p>
		 * Variants of {@linkplain Float#NaN } values are silently read according
		 * to their bit patterns.
		 * <p>
		 * There are no access alignment requirements.
		 *
		 * @param array  to get a value from.
		 * @param offset where extraction in the array should begin
		 * @throws IndexOutOfBoundsException if the provided {@code offset} is outside
		 *                                   the range [0, array.length - 4]
		 * @see #setFloatRaw(byte[], int, float)
		 */
		public static float getFloatRaw(byte[] array, int offset) {
			// Just gets the bits as they are
			return (float) FLOAT.get(array, offset);
		}

		/**
		 * {@return a {@code long} from the provided {@code array} at the given {@code offset}
		 * using big endian order}.
		 * <p>
		 * There are no access alignment requirements.
		 *
		 * @param array  to get a value from.
		 * @param offset where extraction in the array should begin
		 * @throws IndexOutOfBoundsException if the provided {@code offset} is outside
		 *                                   the range [0, array.length - 8]
		 * @see #setLong(byte[], int, long)
		 */
		public static long getLong(byte[] array, int offset) {
			return (long) LONG.get(array, offset);
		}

		/**
		 * {@return a {@code double} from the provided {@code array} at the given {@code offset}
		 * using big endian order}.
		 * <p>
		 * Variants of {@linkplain Double#NaN } values are canonized to a single NaN value.
		 * <p>
		 * There are no access alignment requirements.
		 *
		 * @param array  to get a value from.
		 * @param offset where extraction in the array should begin
		 * @throws IndexOutOfBoundsException if the provided {@code offset} is outside
		 *                                   the range [0, array.length - 8]
		 * @see #setDouble(byte[], int, double)
		 */
		public static double getDouble(byte[] array, int offset) {
			// Using Double.longBitsToDouble collapses NaN values to a single
			// "canonical" NaN value
			return Double.longBitsToDouble((long) LONG.get(array, offset));
		}

		/**
		 * {@return a {@code double} from the provided {@code array} at the given {@code offset}
		 * using big endian order}.
		 * <p>
		 * Variants of {@linkplain Double#NaN } values are silently read according to
		 * their bit patterns.
		 * <p>
		 * There are no access alignment requirements.
		 *
		 * @param array  to get a value from.
		 * @param offset where extraction in the array should begin
		 * @throws IndexOutOfBoundsException if the provided {@code offset} is outside
		 *                                   the range [0, array.length - 8]
		 * @see #setDoubleRaw(byte[], int, double)
		 */
		public static double getDoubleRaw(byte[] array, int offset) {
			// Just gets the bits as they are
			return (double) DOUBLE.get(array, offset);
		}

		// Methods for packing primitive values into byte arrays starting at a given * offset.

		/**
		 * Sets (writes) the provided {@code value} into
		 * the provided {@code array} beginning at the given {@code offset}.
		 *
		 * @param array  to set (write) a value into
		 * @param offset where setting (writing) in the array should begin
		 * @param value  value to set in the array
		 * @throws IndexOutOfBoundsException if the provided {@code offset} is outside
		 *                                   the range [0, array.length]
		 * @see #getBoolean(byte[], int)
		 */
		public static void setBoolean(byte[] array, int offset, boolean value) {
			array[offset] = (byte) (value ? 1 : 0);
		}

		/**
		 * Sets (writes) the provided {@code value} using big endian order into
		 * the provided {@code array} beginning at the given {@code offset}.
		 * <p>
		 * There are no access alignment requirements.
		 *
		 * @param array  to set (write) a value into
		 * @param offset where setting (writing) in the array should begin
		 * @param value  value to set in the array
		 * @throws IndexOutOfBoundsException if the provided {@code offset} is outside
		 *                                   the range [0, array.length - 2]
		 * @see #getChar(byte[], int)
		 */
		public static void setChar(byte[] array, int offset, char value) {
			CHAR.set(array, offset, value);
		}

		/**
		 * Sets (writes) the provided {@code value} using big endian order into
		 * the provided {@code array} beginning at the given {@code offset}.
		 * <p>
		 * There are no access alignment requirements.
		 *
		 * @param array  to set (write) a value into
		 * @param offset where setting (writing) in the array should begin
		 * @param value  value to set in the array
		 * @throws IndexOutOfBoundsException if the provided {@code offset} is outside
		 *                                   the range [0, array.length - 2]
		 * @see #getShort(byte[], int)
		 */
		public static void setShort(byte[] array, int offset, short value) {
			SHORT.set(array, offset, value);
		}

		/**
		 * Sets (writes) the provided {@code value} using big endian order into
		 * the provided {@code array} beginning at the given {@code offset}.
		 * <p>
		 * There are no access alignment requirements.
		 *
		 * @param array  to set (write) a value into
		 * @param offset where setting (writing) in the array should begin
		 * @param value  value to set in the array
		 * @throws IndexOutOfBoundsException if the provided {@code offset} is outside
		 *                                   the range [0, array.length - 2]
		 * @see #getUnsignedShort(byte[], int)
		 */
		public static void setUnsignedShort(byte[] array, int offset, int value) {
			SHORT.set(array, offset, (short) (char) value);
		}

		/**
		 * Sets (writes) the provided {@code value} using big endian order into
		 * the provided {@code array} beginning at the given {@code offset}.
		 * <p>
		 * There are no access alignment requirements.
		 *
		 * @param array  to set (write) a value into
		 * @param offset where setting (writing) in the array should begin
		 * @param value  value to set in the array
		 * @throws IndexOutOfBoundsException if the provided {@code offset} is outside
		 *                                   the range [0, array.length - 4]
		 * @see #getInt(byte[], int)
		 */
		public static void setInt(byte[] array, int offset, int value) {
			INT.set(array, offset, value);
		}

		/**
		 * Sets (writes) the provided {@code value} using big endian order into
		 * the provided {@code array} beginning at the given {@code offset}.
		 * <p>
		 * Variants of {@linkplain Float#NaN } values are canonized to a single NaN value.
		 * <p>
		 * There are no access alignment requirements.
		 *
		 * @param array  to set (write) a value into
		 * @param offset where setting (writing) in the array should begin
		 * @param value  value to set in the array
		 * @throws IndexOutOfBoundsException if the provided {@code offset} is outside
		 *                                   the range [0, array.length - 2]
		 * @see #getFloat(byte[], int)
		 */
		public static void setFloat(byte[] array, int offset, float value) {
			// Using Float.floatToIntBits collapses NaN values to a single
			// "canonical" NaN value
			INT.set(array, offset, Float.floatToIntBits(value));
		}

		/**
		 * Sets (writes) the provided {@code value} using big endian order into
		 * the provided {@code array} beginning at the given {@code offset}.
		 * <p>
		 * Variants of {@linkplain Float#NaN } values are silently written according to
		 * their bit patterns.
		 * <p>
		 * There are no access alignment requirements.
		 *
		 * @param array  to set (write) a value into
		 * @param offset where setting (writing) in the array should begin
		 * @param value  value to set in the array
		 * @throws IndexOutOfBoundsException if the provided {@code offset} is outside
		 *                                   the range [0, array.length - 2]
		 * @see #getFloatRaw(byte[], int)
		 */
		public static void setFloatRaw(byte[] array, int offset, float value) {
			// Just sets the bits as they are
			FLOAT.set(array, offset, value);
		}

		/**
		 * Sets (writes) the provided {@code value} using big endian order into
		 * the provided {@code array} beginning at the given {@code offset}.
		 * <p>
		 * There are no access alignment requirements.
		 *
		 * @param array  to set (write) a value into
		 * @param offset where setting (writing) in the array should begin
		 * @param value  value to set in the array
		 * @throws IndexOutOfBoundsException if the provided {@code offset} is outside
		 *                                   the range [0, array.length - 4]
		 * @see #getLong(byte[], int)
		 */
		public static void setLong(byte[] array, int offset, long value) {
			LONG.set(array, offset, value);
		}

		/**
		 * Sets (writes) the provided {@code value} using big endian order into
		 * the provided {@code array} beginning at the given {@code offset}.
		 * <p>
		 * Variants of {@linkplain Double#NaN } values are canonized to a single NaN value.
		 * <p>
		 * There are no access alignment requirements.
		 *
		 * @param array  to set (write) a value into
		 * @param offset where setting (writing) in the array should begin
		 * @param value  value to set in the array
		 * @throws IndexOutOfBoundsException if the provided {@code offset} is outside
		 *                                   the range [0, array.length - 2]
		 * @see #getDouble(byte[], int)
		 */
		public static void setDouble(byte[] array, int offset, double value) {
			// Using Double.doubleToLongBits collapses NaN values to a single
			// "canonical" NaN value
			LONG.set(array, offset, Double.doubleToLongBits(value));
		}

		/**
		 * Sets (writes) the provided {@code value} using big endian order into
		 * the provided {@code array} beginning at the given {@code offset}.
		 * <p>
		 * Variants of {@linkplain Double#NaN } values are silently written according to
		 * their bit patterns.
		 * <p>
		 * There are no access alignment requirements.
		 *
		 * @param array  to set (write) a value into
		 * @param offset where setting (writing) in the array should begin
		 * @param value  value to set in the array
		 * @throws IndexOutOfBoundsException if the provided {@code offset} is outside
		 *                                   the range [0, array.length - 2]
		 * @see #getDoubleRaw(byte[], int)
		 */
		public static void setDoubleRaw(byte[] array, int offset, double value) {
			// Just sets the bits as they are
			DOUBLE.set(array, offset, value);
		}

		private static VarHandle create(Class<?> viewArrayClass) {
			return MethodHandles.byteArrayViewVarHandle(viewArrayClass, ByteOrder.BIG_ENDIAN);
		}

		/**
		 * Reads a {@code long} value from a {@link CharSequence} with given offset.
		 *
		 * @param cs a char sequence
		 * @param off an offset
		 * @return the value
		 */
		public static long getLong (CharSequence cs, int off) {
			return ((long) cs.charAt(off) << 48)
					| ((long) cs.charAt(off + 1) << 32)
					| ((long) cs.charAt(off + 2) << 16)
					| cs.charAt(off + 3);
		}

		/**
		 * Reads an {@code int} value from a {@link CharSequence} with given offset.
		 *
		 * @param cs a char sequence
		 * @param off an offset
		 * @return the value
		 */
		public static int getInt (CharSequence cs, int off) {
			return ((int) cs.charAt(off) << 16)
					| (int) cs.charAt(off + 1);
		}

		/**
		 * Copies a given number of characters from a {@link CharSequence} into a byte array.
		 *
		 * @param cs a char sequence
		 * @param offsetCharSequence an offset for the char sequence
		 * @param toByteArray a byte array
		 * @param offsetByteArray an offset for the byte array
		 * @param numChars the number of characters to copy
		 */
		public static void copyCharsToByteArray (
			CharSequence cs,
			int offsetCharSequence,
			byte[] toByteArray,
			int offsetByteArray,
			int numChars
		){
			for (int charIdx = 0; charIdx <= numChars - 4; charIdx += 4){
				setLong(toByteArray,
					offsetByteArray + (charIdx << 1),
					getLong(cs, offsetCharSequence + charIdx));
			}

			if ((numChars & 2) != 0){
				int charIdx = numChars & 0xFFFF_FFFC;
				setInt(toByteArray,
					offsetByteArray + (charIdx << 1),
					getInt(cs, offsetCharSequence + charIdx));
			}

			if ((numChars & 1) != 0){
				int charIdx = numChars & 0xFFFF_FFFE;
				setChar(toByteArray,
					offsetByteArray + (charIdx << 1),
					cs.charAt(offsetCharSequence + charIdx));
			}
		}
	}
}