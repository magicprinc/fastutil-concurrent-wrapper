package com.trivago.kangaroo.object2long;

import java.util.concurrent.ThreadLocalRandom;

public class TestObjectKey {

    private final int id = ThreadLocalRandom.current().nextInt();

    /**
     * Returns the hash code for this object based on its unique identifier.
     * <p>
     * The hash code is computed by applying {@link Integer#hashCode(int)} to the {@code id} field.
     * </p>
     *
     * @return the hash code derived from the object's {@code id}
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
