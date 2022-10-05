package com.github.wnebyte.minecraft.renderer;

import java.util.Objects;
import java.util.Comparator;
import com.github.wnebyte.minecraft.util.Settings;

public class DrawCommand implements Comparable<DrawCommand> {

    public static final int SIZE = 4;

    public static final int SIZE_BYTES = SIZE * Integer.BYTES;

    public static final Comparator<DrawCommand> COMPARATOR = DrawCommand::compareTo;

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
    public int compareTo(DrawCommand o) {
        return Integer.compare(this.first, o.first);
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

    public String toJson() {
        return Settings.GSON.toJson(this);
    }

}
