package com.trivago.fastutilconcurrentwrapper.longint;

import com.trivago.fastutilconcurrentwrapper.longkey.ConcurrentBusyWaitingLongIntMap;

public class ConcurrentBusyWaitingLongIntMapTest extends AbstractLongIntMapTest {
  @Override
  ConcurrentBusyWaitingLongIntMap createMap() {
    return new ConcurrentBusyWaitingLongIntMap(16, 16, 0.9F, defaultValue);
  }
}