package com.trivago.fastutilconcurrentwrapper.longlong;

import com.trivago.fastutilconcurrentwrapper.longkey.ConcurrentLongLongMap;

public class PrimitiveFastutilLongLongWrapperTest extends AbstractLongLongMapTest {
  @Override
  ConcurrentLongLongMap createMap() {
    return new ConcurrentLongLongMap(1, 5, 0.9F, defaultValue);
  }
}