package com.trivago.fastutilconcurrentwrapper.longlong;

import com.trivago.fastutilconcurrentwrapper.map.ConcurrentBusyWaitingLongLongMap;

public class ConcurrentBusyWaitingLongLongMapTest extends AbstractLongLongMapTest {

  @Override
  ConcurrentBusyWaitingLongLongMap createMap() {
    return new ConcurrentBusyWaitingLongLongMap(16, 16, 0.9F, defaultValue);
  }
}