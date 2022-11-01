package com.github.wnebyte.minecraft.renderer;

import com.github.wnebyte.minecraft.core.Camera;

public interface Batch<T> {

    void start();

    void render(Camera camera);

    void destroy();

    boolean add(T element);

    int zIndex();
}
