package com.github.wnebyte.minecraft.renderer;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;

public class VertexBuffer {

    static int combinePositions(Vector3i pos, int shared) {
        return (shared |
                (pos.x & 63) |
                (pos.y & 63) << 6 |
                (pos.z & 63) << 12);
    }

    public static final int POS_SIZE = 3;

    public static final int UV_SIZE = 2;

    public static final int TEX_ID_SIZE = 1;

    public static final int POS_OFFSET = 0;

    public static final int UV_OFFSET = POS_OFFSET + (POS_SIZE * Float.BYTES);

    public static final int TEX_ID_OFFSET = UV_OFFSET + (UV_SIZE * Float.BYTES);

    public static final int STRIDE = POS_SIZE + UV_SIZE;

    public static final int STRIDE_BYTES = STRIDE * Float.BYTES;

    public static final int DEFAULT_CAPACITY = 4096;

    public static final int BUFFER_CAPACITY = 10_000;

    private FloatBuffer data;

    private int size;

    private boolean dirty = true;

    public int first;

    public int drawCommandIndex;

    public Vector2i chunkCoords;

    public short subchunkLevel;


    public VertexBuffer(ByteBuffer buffer) {
        data = buffer.asFloatBuffer();
        size = 0;
    }

    public void reset() {
        data.clear();
        size = 0;
        dirty = true;
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

    public Vector2i getChunkCoords() {
        return chunkCoords;
    }

    public int getSubchunkLevel() {
        return subchunkLevel;
    }

    public void appendQuad(Vector3f tl, Vector3f tr, Vector3f bl, Vector3f br,
                       Vector2f uv0, Vector2f uv1, Vector2f uv2, Vector2f uv3) {
        // TR
        data.put(tr.x);
        data.put(tr.y);
        data.put(tr.z);
        data.put(uv1.x);
        data.put(uv1.y);

        // TL
        data.put(tl.x);
        data.put(tl.y);
        data.put(tl.z);
        data.put(uv0.x);
        data.put(uv0.y);

        // BL
        data.put(bl.x);
        data.put(bl.y);
        data.put(bl.z);
        data.put(uv2.x);
        data.put(uv2.y);

        // BR
        data.put(br.x);
        data.put(br.y);
        data.put(br.z);
        data.put(uv3.x);
        data.put(uv3.y);

        // TR
        data.put(tr.x);
        data.put(tr.y);
        data.put(tr.z);
        data.put(uv1.x);
        data.put(uv1.y);

        // BL
        data.put(bl.x);
        data.put(bl.y);
        data.put(bl.z);
        data.put(uv2.x);
        data.put(uv2.y);

        size += STRIDE * 6;
    }
}
