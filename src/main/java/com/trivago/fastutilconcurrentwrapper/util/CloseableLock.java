package com.trivago.fastutilconcurrentwrapper.util;

import com.trivago.fastutilconcurrentwrapper.io.SafeCloseable;

/**
 @see AutoCloseable
 @see java.util.concurrent.locks.Lock
 @see java.util.concurrent.locks.ReadWriteLock
*/
@FunctionalInterface
public interface CloseableLock extends SafeCloseable {
}