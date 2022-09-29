package com.github.wnebyte.minecraft.renderer;

import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;

public class DrawElementsIndirectCommand {

    public int vertexCount;

    public int instanceCount;

    public int firstIndex;

    public int baseVertex;

    public int baseInstance;

    public int[] toIntArray() {
        return new int[]{
                vertexCount, instanceCount, firstIndex, baseVertex, baseInstance
        };
    }

    public IntBuffer toIntBuffer() {
        IntBuffer buffer = BufferUtils.createIntBuffer(5);
        buffer.put(vertexCount);
        buffer.put(instanceCount);
        buffer.put(firstIndex);
        buffer.put(baseVertex);
        buffer.put(baseInstance);
        return buffer;
    }
}
