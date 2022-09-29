package com.github.wnebyte.minecraft.util;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

public class Sets {

    @SafeVarargs
    public static <T> Set<T> of(T... elements) {
        if (elements == null) return null;
        Set<T> newSet = new HashSet<T>(elements.length);
        newSet.addAll(Arrays.asList(elements));
        return newSet;
    }

    public static Set<Integer> of(int[] elements, int startIndex, int endIndex) {
        if (elements == null) return null;
        Set<Integer> newSet = new HashSet<>();
        for (int i = startIndex; i < endIndex; i++) {
            int e = elements[i];
            newSet.add(e);
        }
        return newSet;
    }

    public static <T> Set<T> emptySet() {
        return new HashSet<T>(10);
    }
}
