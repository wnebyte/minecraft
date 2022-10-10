package com.github.wnebyte.minecraft.util;

import java.util.Arrays;
import java.util.Iterator;
import org.joml.Vector2i;
import com.github.wnebyte.minecraft.renderer.DrawCommand;

public class DrawCommandBuffer implements Iterable<DrawCommand> {

    private final DrawCommand[] data;

    private final Vector2i[] chunkCoordsData;

    private final int capacity;

    private int size;

    private boolean dirty;

    public DrawCommandBuffer(int capacity) {
        this.capacity = capacity;
        this.data = new DrawCommand[capacity];
        this.chunkCoordsData = new Vector2i[capacity];
        this.size = 0;
        this.dirty = true;
    }

    public void putCommand(DrawCommand drawCommand, Vector2i chunkCoords) {
        int index = Arrays.binarySearch(data, 0, size, drawCommand, DrawCommand.COMPARATOR);
        if (index > 0) {
            setDrawCommand(index, drawCommand, chunkCoords);
        } else {
            addCommand(drawCommand, chunkCoords);
        }
    }

    public void addCommand(DrawCommand drawCommand, Vector2i chunkCoords) {
        if (size == capacity) return;
        drawCommand.baseInstance = size;
        data[size] = drawCommand;
        chunkCoordsData[size] = chunkCoords;
        size++;
        dirty = true;
    }

    public void setDrawCommand(int index, DrawCommand drawCommand, Vector2i chunkCoords) {
        if (index < 0 || index >= capacity)
            return;
        boolean contains = (data[index] != null);
        drawCommand.baseInstance = index;
        data[index] = drawCommand;
        chunkCoordsData[index] = chunkCoords;
        if (!contains) size++;
        dirty = true;
    }

    public DrawCommand get(int index) {
        return data[index];
    }

    public Vector2i getChunkCoord(int index) {
        return chunkCoordsData[index];
    }

    public int[] data() {
        int index = 0;
        int[] drawCommands = new int[DrawCommand.SIZE * size];
        for (int i = 0; i < size; i++) {
            DrawCommand drawCommand = data[i];
            drawCommands[index + 0] = drawCommand.vertexCount;
            drawCommands[index + 1] = drawCommand.instanceCount;
            drawCommands[index + 2] = drawCommand.first;
            drawCommands[index + 3] = drawCommand.baseInstance;
            index += DrawCommand.SIZE;
        }
        return drawCommands;
    }

    public int[] chunkCoords() {
        int index = 0;
        int[] data = new int[2 * size];
        for (int i = 0; i < size; i++) {
            Vector2i chunkCoords = chunkCoordsData[i];
            data[index + 0] = chunkCoords.x;
            data[index + 1] = chunkCoords.y;
            index += 2;
        }
        return data;
    }

    public int size() {
        return size;
    }

    public int capacity() {
        return capacity;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void clean() {
        dirty = false;
    }

    public void setDirty() {
        dirty = true;
    }

    @Override
    public Iterator<DrawCommand> iterator() {
        return Arrays.stream(data, 0, size).iterator();
    }
}
