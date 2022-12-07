package com.github.wnebyte.minecraft.renderer;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import org.joml.Vector2i;

public class Subchunk {

    public enum State {
        MESHED,
        UNMESHED;
    }

    private final VertexBuffer vertexBuffer;

    private final int first;

    private Vector2i chunkCoords;

    private int subchunkLevel;

    private AtomicReference<State> state;

    public Subchunk(VertexBuffer vertexBuffer, int first) {
        this.vertexBuffer = vertexBuffer;
        this.first = first;
        this.state = new AtomicReference<>(State.UNMESHED);
    }

    public void resetVertexBuffer() {
        vertexBuffer.reset();
    }

    public VertexBuffer getVertexBuffer() {
        return vertexBuffer;
    }

    public int getFirst() {
        return first;
    }

    public Vector2i getChunkCoords() {
        return chunkCoords;
    }

    public void setChunkCoords(Vector2i chunkCoords) {
        this.chunkCoords = chunkCoords;
    }

    public int getSubchunkLevel() {
        return subchunkLevel;
    }

    public void setSubchunkLevel(int subchunkLevel) {
        this.subchunkLevel = subchunkLevel;
    }

    public State getState() {
        return state.get();
    }

    public void setState(State newValue) {
        state.set(newValue);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof Subchunk)) return false;
        Subchunk subchunk = (Subchunk) o;
        return Objects.equals(subchunk.chunkCoords, this.chunkCoords) &&
                Objects.equals(subchunk.subchunkLevel, this.subchunkLevel);
    }

    @Override
    public int hashCode() {
        int result = 2;
        return 2 *
                result +
                Objects.hashCode(this.chunkCoords) +
                Objects.hashCode(this.subchunkLevel);
    }

    @Override
    public String toString() {
        return String.format("Subchunk[x: %d, y: %d, z: %d]", chunkCoords.x, subchunkLevel, chunkCoords.y);
    }
}