package com.trivago.fastutilconcurrentwrapper.util;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 @see com.google.common.util.concurrent.Striped.PaddedLock
 @see java.util.concurrent.locks.ReadWriteLock
*/
public final class PaddedReadWriteLock extends ReentrantReadWriteLock {
	/*
	 * Padding from 40 into 64 bytes, same size as cache line. Might be beneficial to add a fourth
	 * long here, to minimize chance of interference between consecutive locks, but I couldn't
	 * observe any benefit from that.
	 */
	long unused1;
	long unused2;
	private final CloseableLock read  = ()->
			readLock().unlock();
	private final CloseableLock write = ()->
			writeLock().unlock();

	public CloseableLock read () {
		readLock().lock();
		return read;
	}

	public CloseableLock write () {
		writeLock().lock();
		return write;
	}

	public PaddedReadWriteLock (){ super(false); }//new unfair
}