package com.github.wnebyte.minecraft.renderer;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Comparator;
import org.joml.Vector2i;
import org.joml.Vector3f;
import com.github.wnebyte.minecraft.world.Chunk;
import com.github.wnebyte.minecraft.util.JMath;
import com.github.wnebyte.minecraft.util.BufferSort;

public class VertexBuffer {

    static int compress(int position, int uv, byte face, byte vertex) {
        return (position << 16) | (uv << 6) | (face << 3) | vertex;
    }

    public static final int DATA_SIZE = 1;

    public static final int DATA_OFFSET = 0;

    public static final int STRIDE = DATA_SIZE;

    public static final int STRIDE_BYTES = STRIDE * Integer.BYTES;

    public static final int CAPACITY = 10_000;

    // ((16 * 256 * 16) - 1) = 65,535
    // (2^16 - 1) =  65,535
    // 1111 1111 1111 1111 0000 0000 0000 0000
    public static final int POSITION_BITMASK = 0xFFFF0000;

    // (2^10 - 1) = 1,023
    // 0000 0000 0000 0000 1111 1111 1100 0000
    public static final int UV_BITMASK = 0xFFC0;

    // 0000 0000 0000 0000 0000 0000 0011 1000
    public static final int FACE_BITMASK = 0x38;

    // 0000 0000 0000 0000 0000 0000 0000 0111
    public static final int VERTEX_BITMASK = 0x7;

    private IntBuffer data;

    private int size;

    private boolean dirty;

    public int first;

    public int drawCommandIndex;

    public boolean isBlendable;

    public Vector2i chunkCoords;

    public short subchunkLevel;

    public VertexBuffer(ByteBuffer buffer) {
        data = buffer.asIntBuffer();
        size = 0;
        dirty = true;
    }

    // position index - 16 bits
    // uv index       - 10 bits
    // face index     - 3 bits
    // vertex index   - 3 bits
    public void append(int position, int uv, byte face) {
        // TR
        data.put(compress(position, uv, face, (byte)0));
        // TL
        data.put(compress(position, uv, face, (byte)3));
        // BL
        data.put(compress(position, uv, face, (byte)2));
        // BR
        data.put(compress(position, uv, face, (byte)1));
        // TR
        data.put(compress(position, uv, face, (byte)0));
        // BL
        data.put(compress(position, uv, face, (byte)2));
        size += STRIDE * 6;
    }

    public void sort(Vector3f pos) {
        if (isEmpty()) return;

        Comparator<Integer> c = (a, b) -> {
            int aIndex = (a & POSITION_BITMASK) >> 16;
            int bIndex = (b & POSITION_BITMASK) >> 16;
            Vector3f aDelta = JMath.sub(pos, JMath.toVector3f(Chunk.toIndex3D(aIndex)));
            Vector3f bDelta = JMath.sub(pos, JMath.toVector3f(Chunk.toIndex3D(bIndex)));
            return JMath.compare(aDelta, bDelta);
        };

        BufferSort.heapSort(data, 0, size, c);
    }


    /*
    TL     TR
    # ----- #

    # ----- #
    BL      BR
     */
    // TR - TL - BL (Counter-closewise, i.e. Front-facing)
    // BR - TR - BL (Counter-clockwise, i.e. Front-facing)
    /*
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
     */

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
}
