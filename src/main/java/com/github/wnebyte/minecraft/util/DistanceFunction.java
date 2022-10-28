package com.github.wnebyte.minecraft.util;

@FunctionalInterface
public interface DistanceFunction<T extends Number & Comparable<? super T>> {

    T get(T a, T b);
}
