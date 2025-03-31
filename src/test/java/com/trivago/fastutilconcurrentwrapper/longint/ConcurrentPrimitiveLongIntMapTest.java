package com.trivago.fastutilconcurrentwrapper.longint;

import com.trivago.fastutilconcurrentwrapper.longkey.ConcurrentLongIntMap;


public class ConcurrentPrimitiveLongIntMapTest extends AbstractLongIntMapTest {
    @Override
    ConcurrentLongIntMap createMap() {
        return new ConcurrentLongIntMap(16, 16, 0.9F, defaultValue);
    }
}