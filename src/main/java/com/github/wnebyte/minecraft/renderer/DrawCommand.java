package com.github.wnebyte.minecraft.renderer;

import java.util.Objects;

public class DrawCommand {

    public static final int SIZE = 4;

    public static final int SIZE_BYTES = SIZE * Integer.BYTES;

    public int vertexCount;

    public int instanceCount;

    public int first;

    public int baseInstance;

    public DrawCommand() {
        this(0, 0, 0, 0);
    }

    public DrawCommand(int vertexCount, int instanceCount, int first, int baseInstance) {
        this.vertexCount = vertexCount;
        this.instanceCount = instanceCount;
        this.first = first;
        this.baseInstance = baseInstance;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public void setVertexCount(int vertexCount) {
        this.vertexCount = vertexCount;
    }

    public int getInstanceCount() {
        return instanceCount;
    }

    public void setInstanceCount(int instanceCount) {
        this.instanceCount = instanceCount;
    }

    public int getFirst() {
        return first;
    }

    public void setFirst(int first) {
        this.first = first;
    }

    public int getBaseInstance() {
        return baseInstance;
    }

    public void setBaseInstance(int baseInstance) {
        this.baseInstance = baseInstance;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof DrawCommand)) return false;
        DrawCommand drawCommand = (DrawCommand) o;
        return Objects.equals(drawCommand.vertexCount, this.vertexCount) &&
                Objects.equals(drawCommand.instanceCount, this.instanceCount) &&
                Objects.equals(drawCommand.first, this.first) &&
                Objects.equals(drawCommand.baseInstance, this.baseInstance);
    }

    @Override
    public int hashCode() {
        int result = 43;
        return result +
                Objects.hashCode(this.vertexCount) +
                Objects.hashCode(this.instanceCount) +
                Objects.hashCode(this.first) +
                Objects.hashCode(this.baseInstance);
    }

    @Override
    public String toString() {
        return String.format("DrawCommand[vertexCount: %d, instanceCount: %d, first: %d, baseInstance: %d]",
                this.vertexCount, this.instanceCount, this.first, this.baseInstance);
    }
}
