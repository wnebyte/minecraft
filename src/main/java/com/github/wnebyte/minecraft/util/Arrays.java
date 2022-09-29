package com.github.wnebyte.minecraft.util;

public class Arrays {

    public static <T> T get(T[] array, int index) {
        if (withinBounds(array, index)) {
            return array[index];
        }
        return null;
    }

    public static void set(Object[] array, int index, Object o) {
        if (withinBounds(array, index)) {
            array[index] = o;
        }
    }

    public static boolean withinBounds(Object[] array, int index) {
        return (index >= 0 && index < array.length);
    }

    public static boolean contains(Object[] array, Object o) {
        return (indexOf(array, o) != -1);
    }

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
