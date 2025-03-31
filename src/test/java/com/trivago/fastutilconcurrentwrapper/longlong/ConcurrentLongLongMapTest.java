package com.trivago.fastutilconcurrentwrapper.longlong;

import com.trivago.fastutilconcurrentwrapper.longkey.ConcurrentLongLongMap;

public class ConcurrentLongLongMapTest extends AbstractLongLongMapTest {

  @Override
  ConcurrentLongLongMap createMap() {
    return new ConcurrentLongLongMap(16, 16, 0.9F, defaultValue);
  }
}