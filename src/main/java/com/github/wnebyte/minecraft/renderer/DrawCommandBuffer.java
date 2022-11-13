package com.github.wnebyte.minecraft.renderer;

import java.util.Arrays;
import org.joml.Vector2i;

public class DrawCommandBuffer implements IDrawCommandBuffer {

    private final DrawCommand[] drawCommands;

    private final Vector2i[] chunkCoords;

    private final int capacity;

    private int size;

    public DrawCommandBuffer(int capacity) {
        this.capacity = capacity;
        this.drawCommands = new DrawCommand[capacity];
        this.chunkCoords = new Vector2i[capacity];
        this.size = 0;
    }

    @Override
    public boolean add(Subchunk subchunk) {
        return add(subchunk.getFirst(), subchunk.getNumVertices(), subchunk.getChunkCoords());
    }

    public boolean add(int first, int vertexCount, Vector2i ivec2) {
        if (remaining() > 0) {
            DrawCommand drawCommand = new DrawCommand(vertexCount, 1, first, size);
            drawCommands[size] = drawCommand;
            chunkCoords[size] = ivec2;
            size++;
            return true;
        }
        return false;
    }

    @Override
    public void reset() {
        Arrays.fill(drawCommands, 0, size, null);
        Arrays.fill(chunkCoords,  0, size, null);
        size = 0;
    }

    @Override
    public int[] getDrawCommands() {
        int[] data = new int[4 * size];
        int index = 0;
        for (int i = 0; i < size; i++) {
            DrawCommand drawCommand = drawCommands[i];
            data[index + 0] = drawCommand.getVertexCount();
            data[index + 1] = drawCommand.getInstanceCount();
            data[index + 2] = drawCommand.getFirst();
            data[index + 3] = drawCommand.getBaseInstance();
            index += 4;
        }
        return data;
    }

    @Override
    public int[] getChunkCoords() {
        int[] data = new int[2 * size];
        int index = 0;
        for (int i = 0; i < size; i++) {
            Vector2i ivec2 = chunkCoords[i];
            data[index + 0] = ivec2.x;
            data[index + 1] = ivec2.y;
            index += 2;
        }
        return data;
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
}
