package com.trivago.fastutilconcurrentwrapper;

public abstract class PrimitiveMapBuilder<T extends PrimitiveKeyMap,V> {
    protected MapMode mapMode = MapMode.BUSY_WAITING;
    protected int buckets = 8;
    protected int initialCapacity = 100_000;
    protected float loadFactor = 0.8f;
    protected V defaultValue;

    protected PrimitiveMapBuilder () {
    }

    public final PrimitiveMapBuilder<T,V> withBuckets (int buckets) {
        this.buckets = buckets;
        return this;
    }

    public final PrimitiveMapBuilder<T,V> withInitialCapacity (int initialCapacity) {
        this.initialCapacity = initialCapacity;
        return this;
    }

    public final PrimitiveMapBuilder<T,V> withLoadFactor (float loadFactor) {
        this.loadFactor = loadFactor;
        return this;
    }

    public final PrimitiveMapBuilder<T,V> withMode (MapMode mapMode) {
        this.mapMode = mapMode;
        return this;
    }

    public final PrimitiveMapBuilder<T,V> withDefaultValue (V defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public abstract T build ();

    public enum MapMode {
        BUSY_WAITING,
        BLOCKING
    }

    @Override
    public String toString () {
        return "PrimitiveMapBuilder{mapMode=%s, buckets=%d, initialCapacity=%d, loadFactor=%s, def=%s}".formatted(
            mapMode, buckets, initialCapacity, loadFactor, defaultValue
        );
    }
}