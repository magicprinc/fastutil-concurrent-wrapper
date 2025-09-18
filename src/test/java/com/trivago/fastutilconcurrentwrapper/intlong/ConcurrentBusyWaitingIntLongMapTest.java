package com.trivago.fastutilconcurrentwrapper.intlong;


import com.trivago.fastutilconcurrentwrapper.intkey.ConcurrentBusyWaitingIntLongMap;

public class ConcurrentBusyWaitingIntLongMapTest extends AbstractIntLongMapTest {
  @Override
	ConcurrentBusyWaitingIntLongMap createMap () {
    return new ConcurrentBusyWaitingIntLongMap(16, 16, 0.9F, defaultValue);
  }
}