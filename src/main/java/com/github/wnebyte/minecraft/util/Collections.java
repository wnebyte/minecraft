package com.github.wnebyte.minecraft.util;

import java.util.Collection;

public class Collections {

    public static float[] toArray(Collection<Float> c) {
        if (c == null) return null;
        float[] array = new float[c.size()];

        int i = 0;
        for (float f : c) {
            array[i++] = f;
        }

        return array;
    }
}
