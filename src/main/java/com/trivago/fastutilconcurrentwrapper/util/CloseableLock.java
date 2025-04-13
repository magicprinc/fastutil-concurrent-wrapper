package com.trivago.fastutilconcurrentwrapper.util;

/**
 @see AutoCloseable
 @see java.util.concurrent.locks.Lock
 @see java.util.concurrent.locks.ReadWriteLock
*/
@FunctionalInterface
public interface CloseableLock extends AutoCloseable {
	@Override void close ();
}