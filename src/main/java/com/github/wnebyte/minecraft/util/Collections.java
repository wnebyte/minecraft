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

    public static <E> int intersection(Collection<E> c1, Collection<E> c2) {
        int count = 0;
        for (E element : c1) {
            if (c2.contains(element)) {
                count++;
            }
        }
        return count;
    }
}
