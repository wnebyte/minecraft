package com.github.wnebyte.minecraft.renderer;

import org.joml.Vector2i;

public class DrawCommandBuffer {

    private DrawCommand[] data;

    private int capacity;

    private int size;

    public DrawCommandBuffer(int capacity) {
        this.capacity = capacity;
        this.data = new DrawCommand[capacity];
        this.size = 0;
    }

    public void addCommand(DrawCommand drawCommand) {
        data[size++] = drawCommand;
    }

    public int[] data() {
        int index = 0;
        int[] vDrawCommands = new int[DrawCommand.SIZE * size];
        for (int i = 0; i < size; i++) {
            DrawCommand cmd = data[i];
            vDrawCommands[index + 0] = cmd.vertexCount;
            vDrawCommands[index + 1] = cmd.instanceCount;
            vDrawCommands[index + 2] = cmd.firstIndex;
            vDrawCommands[index + 3] = cmd.baseInstance;
            index += DrawCommand.SIZE;
        }
        return vDrawCommands;
    }

    public int size() {
        return size;
    }

    public int capacity() {
        return capacity;
    }

    public int remaining() {
        return capacity - size;
    }
}
