package com.trivago.fastutilconcurrentwrapper.intlong;

import com.trivago.fastutilconcurrentwrapper.intkey.ConcurrentIntLongMap;

public class ConcurrentIntLongMapTest extends AbstractIntLongMapTest {
  @Override
	ConcurrentIntLongMap createMap () {
    return new ConcurrentIntLongMap(16, 16, 0.9F, defaultValue);
  }
}