package com.trivago.fastutilconcurrentwrapper.longint;

import com.trivago.fastutilconcurrentwrapper.longkey.ConcurrentLongIntMap;

public class PrimitiveFastutilLongIntWrapperTest extends AbstractLongIntMapTest {
  @Override
  ConcurrentLongIntMap createMap() {
    return new ConcurrentLongIntMap(1, 5, 0.9F, defaultValue);
  }
}