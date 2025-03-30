package com.trivago.fastutilconcurrentwrapper.longint;

import com.trivago.fastutilconcurrentwrapper.LongIntMap;
import com.trivago.fastutilconcurrentwrapper.map.ConcurrentLongIntMap;

public class PrimitiveFastutilLongIntWrapperTest extends AbstractLongIntMapTest {

  @Override
  LongIntMap createMap() {
    return new ConcurrentLongIntMap(1, 5, 0.9F, defaultValue);
  }
}