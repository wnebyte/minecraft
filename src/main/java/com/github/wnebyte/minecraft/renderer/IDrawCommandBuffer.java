package com.github.wnebyte.minecraft.renderer;

public interface IDrawCommandBuffer {

    boolean add(Subchunk subchunk);

    int size();

    int capacity();

    int remaining();

    void reset();

    int[] getDrawCommands();

    int[] getChunkCoords();
}
