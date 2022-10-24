package com.github.wnebyte.minecraft.util;

@FunctionalInterface
public interface Formatter<T> {

    String format(T t);
}
