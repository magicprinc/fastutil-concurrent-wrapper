package com.trivago.fastutilconcurrentwrapper.io;

import it.unimi.dsi.fastutil.io.MeasurableStream;
import it.unimi.dsi.fastutil.io.RepositionableStream;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.val;
import org.jspecify.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.UTFDataFormatException;
import java.util.UUID;

/**
 @see java.io.ByteArrayInputStream

 @see it.unimi.dsi.fastutil.io.FastByteArrayInputStream

 @see it.unimi.dsi.fastutil.io.FastMultiByteArrayInputStream
 @see it.unimi.dsi.fastutil.io.FastBufferedInputStream

 @author Andrej Fink [https://magicprinc.github.io]
*/
@SuppressWarnings({"NonSynchronizedMethodOverridesSynchronizedMethod", "ResultOfMethodCallIgnored"})
public class BAIS extends ByteArrayInputStream implements ObjectInput, MeasurableStream, RepositionableStream, SafeCloseable {
	protected int offset;

	public BAIS (byte[] array, @PositiveOrZero int offset, @PositiveOrZero int maxLength) {
		super(array, offset, maxLength);
		this.offset = offset;
		pos = mark = 0;
		count = Math.min(maxLength, array.length - offset);// offset|0..pos..length|...array.length
	}//new

	public BAIS (byte[] array){ super(array); }//new

	public BAIS (BAOS baos) {
		super(baos.array(), 0, baos.size());
	}//new

	/// The array backing the input stream. capacity = array().length
	public byte[] array (){ return buf; }
	public void array (byte[] array, @PositiveOrZero int offset, @PositiveOrZero int maxLength){
		buf = array;
		this.offset = offset;
		pos = mark = 0;
		count = Math.min(maxLength, array.length - offset);
	}

	/** The first valid entry. aka {@link #position()} */
	public @PositiveOrZero int offset (){ return offset; }
	public void offset (@PositiveOrZero int newOffset){ offset = newOffset; }

	/** The current position as a distance from {@link #offset}. */
	@Override public long position (){ return pos; }
	public int readerIndex (){ return pos; }

	public int markIndex (){ return mark; }

	@Override
	public void position (long newPosition) {
		pos = (int)Math.min(newPosition, limit());
	}
	public void readerIndex (int newReaderIndex) {
		pos = Math.min(newReaderIndex, limit());
	}

	/** The number of valid bytes in {@link #array} starting from {@link #offset}. */
	@Override public @PositiveOrZero long length (){ return count; }
	public @PositiveOrZero int limit (){ return count; }

	public void limit (@PositiveOrZero int maxLength){ count = Math.min(maxLength, array().length - offset); }

	/** Closing a fast byte array input stream has no effect. */
	@Override public void close (){}

	@Override public int available (){ return count - pos; }// length - position

	@Override
	public long skip (@PositiveOrZero long n) {
		int avail = count - pos;
		if (n <= avail){
			pos += (int)n;
			return n;
		}
		n = avail;
		pos = count;// position = length
		return n;
	}

	@Override
	public int read () {
		if (count <= pos) return -1;// EOF
		return buf[offset + pos++] & 0xFF;
	}

	/**
	 Reads bytes from this byte-array input stream as specified in {@link java.io.InputStream#read(byte[], int, int)}.

	 Note! The implementation given in {@link java.io.ByteArrayInputStream#read(byte[], int, int)} will return -1
	 on a 0-length read at EOF, contrarily to the specification. We won't.
	*/
	@Override
	public int read (byte[] b, @PositiveOrZero int fromOffset, @PositiveOrZero int length) {
		if (this.count <= this.pos) return length == 0 ? 0 : -1;
		int n = Math.min(length, this.count - this.pos);
		System.arraycopy(buf, this.offset + this.pos,  b, fromOffset,  n);
		pos += n;
		return n;
	}

	@Override
	public byte[] readAllBytes () {
		return readNBytes(available());
	}

	@Override
	public int read (byte[] b) {
		return read(b, 0, b.length);
	}

	@Override
	public byte[] readNBytes (int len) {
		int n = Math.min(len, available());
		val result = new byte[n];
		read(result);
		return result;
	}

	@Override public void skipNBytes (long n){ skip(n); }

	// read next byte without shifting readerIndex
	public int peek () {
		if (count <= pos) return -1;
		return buf[offset + pos] & 0xFF;
	}

	@Override public void readFully (byte[] b){ read(b); }

	@Override
	public void readFully (byte[] b, int off, int len) {
		read(b, off, len);
	}

	@Override public int skipBytes (@PositiveOrZero int n){ return (int) skip(n); }//DataInput#skipBytes

	@Override
	public boolean readBoolean () {
		return read() != 0;
	}

	@Override public byte readByte (){ return (byte) read(); }

	@Override public int readUnsignedByte (){ return read() & 0xFF; }

	/// @see DataInputStream#readShort
	/// (short)((read() << 8)|(read() & 0xFF))
	@Override
	public short readShort() {
		return (short)((read() << 8)|(read() & 0xFF));
	}

	@Override
	public int readUnsignedShort() {
		return ((read() & 0xFF) << 8)|(read() & 0xFF);
	}

	@Override
	public char readChar() {
		return (char)(((read() & 0xFF) << 8)|(read() & 0xFF));
	}

	@Override
	public int readInt() {
		return read() << 24 | ((read() & 0xFF) << 16) | ((read() & 0xFF) << 8) | (read() & 0xFF);
	}

	public int readMedium () {
		return ((read() & 0xFF) << 16) | ((read() & 0xFF) << 8) | (read() & 0xFF);
	}

	/// @see UUID#UUID(long, long)
	/// @see UUID#fromString(String)
	public UUID readUUID () {
		//val bb = ByteBuffer.wrap(bytes); long mostSigBits = bb.getLong(); long leastSigBits = bb.getLong();  быстрее за счёт VarHandle
		long mostSigBits = readLong();// 0..7
		long leastSigBits = readLong();// 8..15
		return new UUID(mostSigBits, leastSigBits);
	}

	@Override
	public long readLong () {
		return (long) readInt() << 32 | (readInt() & 0xFFFF_FFFFL);
	}

	@Override
	public float readFloat () {
		return Float.intBitsToFloat(readInt());
	}

	@Override
	public double readDouble () {
		return Double.longBitsToDouble(readLong());
	}

	@Override  @Deprecated
	public String readLine () {
		val sb = new StringBuilder(160);
loop:
		for (int c;;){
			switch (c = read()){
			case -1:
				break loop;// eof

			case '\n':
				return sb.toString();
			case '\r':
				if (peek() == '\n') // CR LF
						read();
				return sb.toString();

			default:
				sb.append((char) c);
			}
		}
		return sb.isEmpty() ? null : sb.toString();
	}

	@Override
	public @Nullable String readUTF () throws UTFDataFormatException {
		try {
			return available() > 2 ? DataInputStream.readUTF(this) : null;
		} catch (UTFDataFormatException badBinaryFormatting){
			throw badBinaryFormatting;
		} catch (IOException e){
			val t = new UTFDataFormatException("IOException: readUTF @ "+ this);
			t.initCause(e);
			throw t;
		}
	}

	/// not efficient! Only added to support custom {@link java.io.Externalizable}
	/// todo size instead of magic prefix; see io.netty.handler.codec.serialization.CompactObjectInputStream
	@Override
	public Object readObject () throws ClassNotFoundException, IOException {
		try (val ois = new ObjectInputStream(this)){
			return ois.readObject();
		}
	}

	/// @see BAOS#writeBytes(String)
	/// @see java.nio.charset.StandardCharsets#ISO_8859_1
	public String readLatin1String (@PositiveOrZero int strLen) {
		String s = new String(buf, 0,  pos, strLen);
		pos += strLen;
		return s;
	}
}