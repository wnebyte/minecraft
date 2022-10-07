package com.github.wnebyte.minecraft.renderer;

public interface Batch<T> {

    void start();

    void render();

    void destroy();

    boolean add(T element);

    boolean remove(T element);

    boolean hasSpace();
}
