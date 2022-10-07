package com.github.wnebyte.minecraft.renderer;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import com.github.wnebyte.minecraft.util.DebugStats;
import com.github.wnebyte.minecraft.world.Chunk;

public class VertexBuffer {

    static int compress(int position, int uv, byte face, byte vertex) {
        return (position << 16) | (uv << 6) | (face << 3) | vertex;
    }

    static int compress(int position, int uv, byte face) {
        return (position << 16) | (uv << 6) | (face << 3);
    }

    public static final int DATA_SIZE = 1;

    public static final int DATA_OFFSET = 0;

    public static final int STRIDE = DATA_SIZE;

    public static final int STRIDE_BYTES = STRIDE * Integer.BYTES;

    public static final int VERTEX_CAPACITY = 10_000;

    public static final int POSITION_BITMASK = 0xFFFF0000;

    public static final int UV_BITMASK = 0xFFC0;

    public static final int FACE_BITMASK = 0x38;

    public static final int VERTEX_BITMASK = 0x7;

    private IntBuffer data;

    private int size;

    public VertexBuffer(ByteBuffer buffer) {
        data = buffer.asIntBuffer();
        size = 0;
    }

    public void reset() {
        data.clear();
        DebugStats.vertexMemUsed -= (long)STRIDE_BYTES * size;
        size = 0;
    }

    // position index - 16 bits
    // uv index       - 10 bits
    // face index     - 3 bits
    // vertex index   - 3 bits
    // TR, TL, BL, BR, TR, BL
    public void append(int position, int uv, byte face) {
        // Todo: FRONT, LEFT, and RIGHT facing textures are upside-down
        assert (size <= capacity() - 6) : String.format("Error: (VertexBuffer) Overflow: %d", size);
        if (face == Chunk.FaceType.FRONT.ordinal()) {
            uv = 289;
        }
        int shared = compress(position, uv, face);
        data.put(shared | 0);
        data.put(shared | 3);
        data.put(shared | 2);
        data.put(shared | 1);
        data.put(shared | 0);
        data.put(shared | 2);
        size += STRIDE * 6;
        DebugStats.vertexMemUsed += (long)STRIDE_BYTES * 6;
    }

    public int size() {
        return size;
    }

    public int capacity() {
        return data.capacity();
    }

    public int remaining() {
        return data.remaining();
    }

    public boolean isEmpty() {
        return (size == 0);
    }

    public int getNumVertices() {
        return size / STRIDE;
    }

    public int getNumQuads() {
        return (size / STRIDE) / 6;
    }
}
