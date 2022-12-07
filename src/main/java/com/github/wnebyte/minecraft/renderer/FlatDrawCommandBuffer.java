package com.github.wnebyte.minecraft.renderer;

import java.util.Arrays;
import org.joml.Vector2i;

public class FlatDrawCommandBuffer implements IDrawCommandBuffer {

    private final int[] drawCommands;

    private final int[] chunkCoords;

    private final int capacity;

    private int size;

    public FlatDrawCommandBuffer(int capacity) {
        this.capacity = capacity;
        this.drawCommands = new int[4 * capacity];
        this.chunkCoords = new int[2 * capacity];
        this.size = 0;
    }

    @Override
    public boolean add(DrawCommand drawCommand, Vector2i ivec2) {
        if (remaining() > 0) {
            int index = 4 * size;
            drawCommands[index + 0] = drawCommand.getVertexCount();
            drawCommands[index + 1] = drawCommand.getInstanceCount();
            drawCommands[index + 2] = drawCommand.getFirst();
            drawCommands[index + 3] = drawCommand.getBaseInstance();
            index = 2 * size;
            chunkCoords[index + 0] = ivec2.x;
            chunkCoords[index + 1] = ivec2.y;
            size++;
            return true;
        }
        return false;
    }

    public boolean add(int first, int vertexCount, Vector2i ivec2) {
        if (remaining() > 0) {
            int index = 4 * size;
            drawCommands[index + 0] = vertexCount;
            drawCommands[index + 1] = 1;
            drawCommands[index + 2] = first;
            drawCommands[index + 3] = size;
            index = 2 * size;
            chunkCoords[index + 0] = ivec2.x;
            chunkCoords[index + 1] = ivec2.y;
            size++;
            return true;
        }
        return false;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public int capacity() {
        return capacity;
    }

    @Override
    public int remaining() {
        return capacity - size;
    }

    @Override
    public void reset() {
        Arrays.fill(drawCommands, 0, 4 * size, 0);
        Arrays.fill(chunkCoords,  0, 2 * size, 0);
        size = 0;
    }

    @Override
    public int[] getDrawCommands() {
        return drawCommands;
    }

    @Override
    public int[] getChunkCoords() {
        return chunkCoords;
    }
}
