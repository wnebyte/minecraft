package com.github.wnebyte.minecraft.renderer;

public interface EntityBatch<T> extends Batch<T> {

    boolean remove(T element);

    void clear();
}
