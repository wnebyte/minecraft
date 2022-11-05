package com.github.wnebyte.minecraft.renderer;

import org.joml.Matrix4f;
import com.github.wnebyte.minecraft.core.Camera;

public interface Batch<T> {

    void start();

    void render(Matrix4f viewMatrix, Matrix4f projectionMatrix);

    void render(Camera camera);

    void destroy();

    boolean add(T element);

    int zIndex();
}
