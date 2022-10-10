package com.github.wnebyte.minecraft.renderer;

import java.util.List;

public class Vertex3DBatch implements Batch<Vertex3D> {

    private List<Vertex3D> vertices;

    @Override
    public void start() {

    }

    @Override
    public void render() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public boolean add(Vertex3D element) {
        return false;
    }

    @Override
    public boolean remove(Vertex3D element) {
        return false;
    }

    @Override
    public void clear() {

    }

    @Override
    public boolean hasSpace() {
        return false;
    }
}
