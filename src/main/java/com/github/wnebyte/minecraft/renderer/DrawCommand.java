package com.github.wnebyte.minecraft.renderer;

import com.github.wnebyte.minecraft.util.Settings;

public class DrawCommand {

    public static final int SIZE = 4;

    public static final int SIZE_BYTES = SIZE * Integer.BYTES;

    public DrawCommand() {
        this(0, 0, 0, 0);
    }

    public DrawCommand(int vertexCount, int instanceCount, int first, int baseInstance) {
        this.vertexCount = vertexCount;
        this.instanceCount = instanceCount;
        this.first = first;
        this.baseInstance = baseInstance;
    }

    public int vertexCount;

    public int instanceCount;

    public int first;

    public int baseInstance;

    @Override
    public String toString() {
        return String.format("DrawCommand[vertexCount: %d, instanceCount: %d, first: %d, baseInstance: %d]",
                vertexCount, instanceCount, first, baseInstance);
    }

    public String toJson() {
        return Settings.GSON.toJson(this);
    }
}
