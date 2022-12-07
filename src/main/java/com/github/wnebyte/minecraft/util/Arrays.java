package com.github.wnebyte.minecraft.util;

public class Arrays {

    public static int indexOf(Object[] array, Object o) {
        if (array == null) return -1;
        for (int i = 0; i < array.length; i++) {
            Object e = array[i];
            if (e.equals(o)) {
                return i;
            }
        }
        return -1;
    }
}
