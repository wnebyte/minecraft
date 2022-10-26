package com.github.wnebyte.minecraft.world;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import org.joml.Vector2i;
import com.github.wnebyte.minecraft.renderer.VertexBuffer;

public class Subchunk {

    public enum State {
        MESHED,
        UNMESHED;
    }

    private final VertexBuffer vertexBuffer;

    private int first;

    private boolean blendable;

    private Vector2i chunkCoords;

    private int subchunkLevel;

    private AtomicReference<State> state;

    public Subchunk(VertexBuffer vertexBuffer) {
        this.vertexBuffer = vertexBuffer;
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

    public void setFirst(int first) {
        this.first = first;
    }

    public int getNumVertices() {
        return vertexBuffer.getNumVertices();
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

    public void setBlendable(boolean value) {
        this.blendable = value;
    }

    public boolean isBlendable() {
        return blendable;
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
        return Objects.equals(subchunk.first, this.first);
    }

    @Override
    public int hashCode() {
        int result = 2;
        return 2 *
                result +
                Objects.hashCode(this.first);
    }
}