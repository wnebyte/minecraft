package com.github.wnebyte.minecraft.renderer;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import com.github.wnebyte.minecraft.util.DebugStats;
import com.github.wnebyte.minecraft.util.Range;

public class VertexBuffer {

    /*
    ###########################
    #        UTILITIES        #
    ###########################
    */

    static int compress(int position, int uv, byte face) {
        return (position << 16) | (uv << 6) | (face << 3);
    }

    static int compress(int position, int uv, byte face, byte colorByBiome) {
        return (position << 16) | (uv << 6) | (face << 3) | (colorByBiome << 2);
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

    public static final int VERTEX_CAPACITY = 10_000;

    public static final int POSITION_BITMASK = 0xFFFF0000;

    public static final int UV_BITMASK = 0xFFC0;

    public static final int FACE_BITMASK = 0x38;

    public static final int COLOR_BY_BIOME_BITMASK = 0x4;

    public static final int VERTEX_BITMASK = 0x3;

    /*
    ###########################
    #          FIELDS         #
    ###########################
    */

    private final IntBuffer data;

    private final Range[] ranges;

    private int size;

    /*
    ###########################
    #       CONSTRUCTORS      #
    ###########################
    */

    public VertexBuffer(ByteBuffer buffer) {
        this(buffer, 3);
    }

    public VertexBuffer(ByteBuffer buffer, int nRanges) {
        this.data = buffer.asIntBuffer();
        this.ranges = new Range[nRanges];
        for (int i = 0; i < nRanges; i++) {
            ranges[i] = new Range();
        }
        this.size = 0;
    }

    /*
    ###########################
    #          METHODS        #
    ###########################
    */

    public void reset() {
        data.clear();
        for (Range range : ranges) { range.set(0, 0); }
        DebugStats.vertexMemUsed -= (long)STRIDE_BYTES * size;
        size = 0;
    }

    // position index - 16 bits
    // uv index       - 10 bits
    // face index     - 3 bits
    // colorByBiome   - 1 bit
    // vertex index   - 2 bits
    // TR, TL, BL, BR, TR, BL
    public void append(int index, int position, int uv, byte face, boolean colorByBiome) {
        assert (size <= capacity() - 6) : String.format("Error: (VertexBuffer) Overflow: %d", size + 6);
        int shared = compress(position, uv, face, (byte)(colorByBiome ? 1 : 0));
        data.put(shared | 0); // TR
        data.put(shared | 3); // TL
        data.put(shared | 2); // BL
        data.put(shared | 1); // BR
        data.put(shared | 0); // TR
        data.put(shared | 2); // BL
        size += STRIDE * 6;
        Range range = ranges[index];
        range.addToIndex(STRIDE * 6);
        for (int i = index + 1; i < ranges.length; i++) {
            Range r = ranges[i];
            r.setFromIndex(range.getToIndex());
            r.setToIndex(range.getToIndex());
        }
        DebugStats.vertexMemUsed += (long)STRIDE_BYTES * 6;
    }

    public Range getRange(int index) {
        return ranges[index];
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
