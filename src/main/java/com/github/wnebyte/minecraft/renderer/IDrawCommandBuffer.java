package com.github.wnebyte.minecraft.renderer;

import org.joml.Vector2i;

public interface IDrawCommandBuffer {

    boolean add(DrawCommand drawCommand, Vector2i chunkCoords);

    int size();

    int capacity();

    int remaining();

    void reset();

    int[] getDrawCommands();

    int[] getChunkCoords();
}
