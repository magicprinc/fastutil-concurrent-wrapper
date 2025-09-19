package com.trivago.fastutilconcurrentwrapper.io;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.trivago.fastutilconcurrentwrapper.util.JBytes;
import it.unimi.dsi.fastutil.Arrays;
import it.unimi.dsi.fastutil.bytes.ByteArrays;
import it.unimi.dsi.fastutil.io.MeasurableStream;
import it.unimi.dsi.fastutil.io.RepositionableStream;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.val;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.*;

/**
 Simple, fast byte-array output stream that exposes the backing array.

 * <p>{@link java.io.ByteArrayOutputStream} is nice, but to get its content you
 * must generate each time a new object. This doesn't happen here.

 * <p>This class will automatically enlarge the backing array, doubling its
 * size whenever new space is needed. The {@link #reset()} method will
 * mark the content as empty, but will not decrease the capacity: use
 * {@link #trim()} for that purpose.

 @see BAIS

 @see org.springframework.util.ResizableByteArrayOutputStream
 @see org.springframework.util.FastByteArrayOutputStream
 @see it.unimi.dsi.fastutil.io.FastByteArrayOutputStream

 @see it.unimi.dsi.fastutil.io.FastByteArrayInputStream
 @see it.unimi.dsi.fastutil.io.FastMultiByteArrayInputStream

 @see it.unimi.dsi.fastutil.io.FastBufferedOutputStream
 @see it.unimi.dsi.fastutil.io.FastBufferedInputStream

 @see java.io.DataOutputStream
 @see com.google.common.io.ByteArrayDataOutput

 @see java.nio.ByteBuffer
 @see io.netty.buffer.ByteBuf
 @see io.vertx.core.buffer.Buffer
 @see okio.Buffer

 @author Andrej Fink [https://magicprinc.github.io]
*/
@SuppressWarnings("NonSynchronizedMethodOverridesSynchronizedMethod")
public class BAOS extends ByteArrayOutputStream implements RepositionableStream, ObjectOutput, MeasurableStream, SafeCloseable, Appendable {
	/// The array backing the output stream.
	/// @see #buf
	public byte[] array (){ return buf; }

	public void array (byte[] array) {
		buf = array;
		count = 0;  position = 0;
	}

	/** The current writing position. */
	protected int position;

	/// Creates a new array output stream with an initial capacity of 160 bytes.
	public BAOS (){ super(160); }//new

	/// Creates a new array output stream with a given initial capacity.
	/// @param initialCapacity the initial length of the backing array.
	public BAOS (@PositiveOrZero int initialCapacity){ super(initialCapacity); }//new

	/// Creates a new array output stream wrapping a given byte array.
	/// @param a the byte array to wrap.
	public BAOS (byte[] a) {
		super(0);
		buf = a;
	}//new

	public BAOS (byte[] a, @PositiveOrZero int length) {
		super(0);
		buf = a;
		count = Math.min(length, a.length);
	}//new

	public BAOS (ByteArrayOutputStream baos) {
		super(0);
		count = baos.size();
		if (baos instanceof BAOS us){
			buf = us.array();
			position = us.writerIndex();
		} else {
			buf = baos.toByteArray();
			position = count;
		}
	}//new ~ clone

	/// Marks this array output stream as empty.
	@Override public void reset (){ count = 0;  position = 0; }

	/// Ensures that the length of the backing array is equal to [#length].
	/// @see #resize(int)
	@CanIgnoreReturnValue
	public byte[] trim () {
		buf = ByteArrays.trim(buf, count);
		return buf;
	}

	@Override
	public void write (int b) {
		if (position >= buf.length)
				buf = ByteArrays.grow(buf, position + 1, count);
		buf[position++] = (byte)b;
		if (count < position) count = position;
	}

	@Override
	public void write (byte[] b, @PositiveOrZero int off, @PositiveOrZero int len) {
		//ByteArrays.ensureOffsetLength(b, off, len);
		Objects.checkFromIndexSize(off, len, b.length);
		if (position + len > buf.length)
				buf = ByteArrays.grow(buf, position + len, position);
		System.arraycopy(b, off, buf, position, len);
		position += len;
		if (count < position) count = position;
	}

	@Override
	public void position (@PositiveOrZero long newPosition) {
		if (newPosition > Integer.MAX_VALUE) throw new IllegalArgumentException("Position too large: "+ newPosition);
		position = (int)newPosition;
	}
	public void writerIndex (@PositiveOrZero int newPosition){ position = newPosition; }

	@Override public @PositiveOrZero long position (){ return position; }
	public @PositiveOrZero int writerIndex (){ return position; }

	@Override public @PositiveOrZero long length (){ return count; }
	@Override public @PositiveOrZero int size (){ return count; }

	/// Return the current size of this stream's internal buffer.
	public @PositiveOrZero int capacity (){ return buf.length; }

	/// Resize the internal buffer size to a specified capacity.
	/// @param targetCapacity the desired size of the buffer
	/// ×throws IllegalArgumentException if the given capacity is smaller than the actual size of the content stored in the buffer already
	/// @see #size()
	/// @see #trim()
	/// @see org.springframework.util.ResizableByteArrayOutputStream#resize(int)
	public void resize (@PositiveOrZero int targetCapacity) {
		//Assert.isTrue(targetCapacity >= count, "New capacity must not be smaller than current size");
		val resizedBuffer = new byte[targetCapacity];
		count = Math.min(this.count, targetCapacity);
		System.arraycopy(buf,0, resizedBuffer,0, count);
		buf = resizedBuffer;
	}

	/// Grow the internal buffer size.
	/// @param additionalCapacity the number of bytes to add to the current buffer size
	/// @see #size()
	/// @see org.springframework.util.ResizableByteArrayOutputStream#grow(int)
	public void grow (@PositiveOrZero int additionalCapacity) {
		//Assert.isTrue(additionalCapacity >= 0, "Additional capacity must be 0 or higher");
		if (count < position) additionalCapacity = additionalCapacity + position - count;
		int needed = count + additionalCapacity;
		if (needed > buf.length){// size + new > capacity
			int newCapacity = (int)Math.min(Math.max((long)buf.length + (buf.length >> 1), needed), Arrays.MAX_ARRAY_SIZE);
			resize(newCapacity);
		}
	}

	@Override
	public byte[] toByteArray () {
		return ByteArrays.copy(buf, 0, count);
	}

	@Override public void close (){}// NOP: only to force no exception

	@Override
	public void write (byte[] b) {
		write(b, 0, b.length);// Only to force no exception
	}

	/// Fast {@link java.nio.charset.StandardCharsets#ISO_8859_1} ×not {@link ByteArrayOutputStream#toString()}
	@Override
	public String toString () {
		return new String(buf, 0, 0,count);
	}

	@Override
	public String toString (Charset charset) {
		return new String(buf, 0, count, charset);
	}

	@Override
	public void writeTo (OutputStream out) throws IOException {
		out.write(buf, 0, count);
	}


	@Override
	public void writeBoolean(boolean v) {
		write(v?1:0);
	}

	@Override
	public void writeByte(int v) {
		write(v);
	}

	@Override
	public void writeShort (int v) {
		grow(2);
		JBytes.DirectByteArrayAccess.setShort(buf, position, (short) v);
		position += 2;
		if (count < position) count = position;
	}

	@Override
	public void writeChar (int v) {
		grow(2);
		JBytes.DirectByteArrayAccess.setChar(buf, position, (char) v);
		position += 2;
		if (count < position) count = position;
	}

	@Override
	public void writeInt (int v) {
		grow(4);
		JBytes.DirectByteArrayAccess.setInt(buf, position, v);
		position += 4;
		if (count < position) count = position;
	}

	public void writeMedium (int v) {
		write(v >> 16);
		write(v >> 8);
		write(v);
	}

	public void writeUUID (UUID uuid) {
		writeLong(uuid.getMostSignificantBits());
		writeLong(uuid.getLeastSignificantBits());
	}

	@Override
	public void writeLong (long v) {
		grow(8);
		JBytes.DirectByteArrayAccess.setLong(buf, position, v);
		position += 8;
		if (count < position) count = position;
	}

	@Override
	public void writeFloat(float v) {
		grow(4);
		JBytes.DirectByteArrayAccess.setFloat(buf, position, v);
		position += 4;
		if (count < position) count = position;
	}

	@Override
	public void writeDouble(double v) {
		grow(8);
		JBytes.DirectByteArrayAccess.setDouble(buf, position, v);
		position += 8;
		if (count < position) count = position;
	}

	/**
	 * @deprecated This method is dangerous as it discards the high byte of every character. For UTF-8, use {@link #writeUTF(String)} or {@link #write(byte[]) @code write(s.getBytes(UTF_8))}.
	 * @see java.io.DataOutputStream#writeBytes(String)
	 */
	@Override
	public void writeBytes (String s) {
		//for (int i = 0, len = s.length(); i < len; i++)write((byte)s.charAt(i));
		int len = s.length();
		grow(len);
		s.getBytes(0, len, buf, position);
		position +=len;
		if (count < position) count = position;
	}

	@Override
	public void writeChars (String s) {
		for (int i = 0, len = s.length(); i < len; i++){
			int v = s.charAt(i);
			writeChar(v);
		}
	}

	@Override
	public void writeUTF (String s) {
		int savePos = position;
		writeShort(0);// len placeholder
		for (int i = 0, len = s.length(); i < len; i++){
			writeUtf8Char(s.charAt(i));
			if (position - savePos > 0xFF_FF + 2){
				count = position = savePos;// rollback
				throw new IllegalArgumentException("UTF encoded string too long: %d: %s".formatted(s.length(), s.substring(0, 99)));
			}
		}
		int len = position - savePos - 2;
		buf[savePos] = (byte)(len >> 8);
		buf[savePos+1] = (byte)len;
	}
	/// @see java.io.DataOutputStream#writeUTF(String,DataOutput)
	/// @see jdk.internal.util.ModifiedUtf#putChar(byte[], int, char)
	public int writeUtf8Char (char c) {
		if (c != 0 && c < 0x80){
			write(c);
			return 1;
		} else if (c >= 0x800){
			write(0xE0 | c >> 12 & 0x0F);
			write(0x80 | c >> 6  & 0x3F);
			write(0x80 | c       & 0x3F);
			return 3;
		} else {
			write(0xC0 | c >> 6 & 0x1F);
			write(0x80 | c      & 0x3F);
			return 2;
		}
	}

	/// not efficient! Only added to support custom {@link java.io.Externalizable}
	@Override
	public void writeObject(Object obj) throws IOException {
		try (var oos = new ObjectOutputStream(this)){
			oos.writeObject(obj);
			oos.flush();
		}
	}

	@Override
	public BAOS append (CharSequence csq) {
		append(csq, 0, csq.length());
		return this;
	}

	/// @see #write(byte[], int, int)
	/// @see #writeBytes(byte[])
	@Override
	public BAOS append (CharSequence csq, int start, int end) {
		final int len = end - start;
		Objects.checkFromIndexSize(start, len, csq.length());
		final int len2 = len << 1;
		if (position + len2 > buf.length)
				buf = ByteArrays.grow(buf, position + len2, position);

		JBytes.DirectByteArrayAccess.copyCharsToByteArray(csq, start,  buf, position,  len);

		position += len2;
		if (count < position) count = position;
		return this;
	}

	/// @see #writeChar(int)
	@Override
	public BAOS append (char c) {
		grow(2);
		JBytes.DirectByteArrayAccess.setChar(buf, position, c);
		position += 2;
		if (count < position) count = position;
		return this;
	}

	@Override
	public boolean equals (Object obj) {
		if (this == obj){
			return true;
		} else if (obj instanceof BAOS baos){
			return java.util.Arrays.equals(buf,0,count, baos.array(),0,baos.size());
		} else if (obj instanceof ByteArrayOutputStream baos){
			return java.util.Arrays.equals(buf,0,count, baos.toByteArray(),0,baos.size());
		} else if (obj instanceof byte[] a){
			return java.util.Arrays.equals(buf,0,count, a,0,a.length);
		} else if (obj instanceof CharSequence cs){
			byte[] a = cs.toString().getBytes(UTF_16BE);
			return java.util.Arrays.equals(buf,0,count, a,0,a.length);
		} else {
			return false;
		}
	}

	/// @see java.util.Arrays#hashCode(byte[])
	/// @see jdk.internal.util.ArraysSupport#hashCode(int, byte[], int, int)
	/// @see java.lang.String#hashCode
	@Override
	public int hashCode () {
		int result = 1;
		for (int i = 0; i < count; i++){
			result = 31 * result + buf[i];
		}
		return result;
	}
}