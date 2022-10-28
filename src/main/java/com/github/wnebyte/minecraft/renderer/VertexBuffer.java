package com.github.wnebyte.minecraft.renderer;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Comparator;
import com.github.wnebyte.minecraft.util.BufferUtils;
import com.github.wnebyte.minecraft.util.DebugStats;
import com.github.wnebyte.minecraft.world.FaceType;

public class VertexBuffer {

    /*
    ###########################
    #        UTILITIES        #
    ###########################
    */

    static int compress(int position, int uv, byte face, byte vertex) {
        return (position << 16) | (uv << 6) | (face << 3) | vertex;
    }

    static int compress(int position, int uv, byte face) {
        return (position << 16) | (uv << 6) | (face << 3);
    }

    /*
    ###########################
    #      STATIC FIELDS      #
    ###########################
    */

    public static final int DATA_SIZE = 1;

    public static final int DATA_OFFSET = 0;

    public static final int STRIDE = DATA_SIZE;

    public static final int STRIDE_BYTES = STRIDE * Integer.BYTES;

    public static final int VERTEX_CAPACITY = 8000;

    public static final int POSITION_BITMASK = 0xFFFF0000;

    public static final int UV_BITMASK = 0xFFC0;

    public static final int FACE_BITMASK = 0x38;

    public static final int VERTEX_BITMASK = 0x7;

    /*
    ###########################
    #          FIELDS         #
    ###########################
    */

    private final IntBuffer data;

    private int size;

    /*
    ###########################
    #       CONSTRUCTORS      #
    ###########################
    */

    public VertexBuffer(ByteBuffer buffer) {
        this.data = buffer.asIntBuffer();
        this.size = 0;
    }

    /*
    ###########################
    #          METHODS        #
    ###########################
    */

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
        int shared = compress(position, uv, face);
        data.put(shared | 0); // TR
        data.put(shared | 3); // TL
        data.put(shared | 2); // BL
        data.put(shared | 1); // BR
        data.put(shared | 0); // TR
        data.put(shared | 2); // BL
        size += STRIDE * 6;
        DebugStats.vertexMemUsed += (long)STRIDE_BYTES * 6;
    }

    public void put(int index, int position, int uv, byte face) {
        int shared = compress(position, uv, face);
        boolean overwrite = (data.get(index) != 0);
        data.put(index + 0, shared | 0); // TR
        data.put(index + 1, shared | 3); // TL
        data.put(index + 2, shared | 2); // BL
        data.put(index + 3, shared | 1); // BR
        data.put(index + 4, shared | 0); // TR
        data.put(index + 5, shared | 2); // BL
        if (!overwrite) {
            size += STRIDE * 6;
            DebugStats.vertexMemUsed += (long)STRIDE_BYTES * 6;
        }
    }

    public int binarySearch(int key) {
        return BufferUtils.binarySearch(data, key);
    }

    public int binarySearch(int fromIndex, int toIndex, int key) {
        return BufferUtils.binarySearch(data, fromIndex, toIndex, key);
    }

    public int binarySearch(int key, Comparator<? super Integer> c) {
        return BufferUtils.binarySearch(data, key, c);
    }

    public int binarySearch(int fromIndex, int toIndex, int key, Comparator<? super Integer> c) {
        return BufferUtils.binarySearch(data, fromIndex, toIndex, key, c);
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
