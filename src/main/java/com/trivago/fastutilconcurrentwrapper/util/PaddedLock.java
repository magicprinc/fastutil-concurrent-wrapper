package com.trivago.fastutilconcurrentwrapper.util;

import java.util.concurrent.locks.ReentrantLock;

/**
 @see com.google.common.util.concurrent.Striped.PaddedLock
*/
public final class PaddedLock extends ReentrantLock implements CloseableLock {
	/*
	 * Padding from 40 into 64 bytes, same size as cache line. Might be beneficial to add a fourth
	 * long here, to minimize chance of interference between consecutive locks, but I couldn't
	 * observe any benefit from that.
	 */
	long unused1;
	long unused2;
	long unused3;
	public PaddedLock (){ super(false); }//new unfair
	/** @see #unlock() */
	@Override public void close (){ unlock(); }
	//@MustBeClosed
	public PaddedLock write () {
		lock();
		return this;
	}
}