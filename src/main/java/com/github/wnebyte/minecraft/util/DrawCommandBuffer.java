package com.github.wnebyte.minecraft.util;

import java.util.Arrays;
import java.util.Iterator;

import com.github.wnebyte.minecraft.renderer.DrawCommand;
import org.joml.Vector2i;

public class DrawCommandBuffer implements Iterable<DrawCommand> {

    private DrawCommand[] data;

    private int maxNumCommands;

    private int numCommands;

    private Vector2i[] chunkCoordsData;

    private boolean dirty = true;

    public DrawCommandBuffer(int maxNumCommands) {
        this.maxNumCommands = maxNumCommands;
        this.data = new DrawCommand[maxNumCommands];
        this.chunkCoordsData = new Vector2i[maxNumCommands];
        this.numCommands = 0;
    }

    public void addCommand(DrawCommand drawCommand, Vector2i chunkCoords) {
        drawCommand.baseInstance = numCommands;
        data[numCommands] = drawCommand;
        chunkCoordsData[numCommands] = chunkCoords;
        numCommands++;
        dirty = true;
    }

    public void setDrawCommand(int index, DrawCommand drawCommand, Vector2i chunkCoords) {
        boolean mod = (data[index] != null);
        drawCommand.baseInstance = index;
        data[index] = drawCommand;
        chunkCoordsData[index] = chunkCoords;
        if (!mod)
            numCommands++;
        dirty = true;
    }

    public void removeDrawCommand(int index) {

    }

    public int[] data() {
        int index = 0;
        int[] drawCommands = new int[DrawCommand.SIZE * numCommands];
        for (int i = 0; i < numCommands; i++) {
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
        int[] data = new int[2 * numCommands];
        for (int i = 0; i < numCommands; i++) {
            Vector2i chunkCoords = chunkCoordsData[i];
            data[index + 0] = chunkCoords.x;
            data[index + 1] = chunkCoords.y;
            index += 2;
        }
        return data;
    }

    public int numCommands() {
        return numCommands;
    }

    public int maxNumCommands() {
        return maxNumCommands;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void clean() {
        dirty = false;
    }

    @Override
    public Iterator<DrawCommand> iterator() {
        return Arrays.stream(data, 0, numCommands).iterator();
    }
}
