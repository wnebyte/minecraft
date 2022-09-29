package com.github.wnebyte.minecraft.renderer;

import java.util.Arrays;
import java.nio.FloatBuffer;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL44.glBufferStorage;
import static org.lwjgl.opengl.GL44C.GL_MAP_COHERENT_BIT;
import static org.lwjgl.opengl.GL44C.GL_MAP_PERSISTENT_BIT;

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

    private int vao;

    private int vbo;

    private float[] data;

    private FloatBuffer buffer;

    private int size;

    private boolean dirty = true;

    public VertexBuffer() {
        this(DEFAULT_CAPACITY);
    }

    public VertexBuffer(int capacity) {
        data = new float[capacity];
        start();
    }

    private void start() {
        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        glVertexAttribPointer(0, POS_SIZE, GL_FLOAT, false, STRIDE_BYTES, POS_OFFSET);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, UV_SIZE, GL_FLOAT, false, STRIDE_BYTES, UV_OFFSET);
        glEnableVertexAttribArray(1);

        long size = VertexBuffer.BUFFER_CAPACITY * STRIDE_BYTES;
        int flags = GL_MAP_PERSISTENT_BIT | GL_MAP_WRITE_BIT | GL_MAP_COHERENT_BIT;
        glBufferStorage(GL_ARRAY_BUFFER, size, flags);
        buffer = glMapBufferRange(GL_ARRAY_BUFFER, 0, size, flags).asFloatBuffer();
    }

    public void destroy() {
        glDeleteVertexArrays(vao);
        glDeleteBuffers(vbo);
    }

    public void reset(int capacity) {
        data = new float[capacity];
        size = 0;
        dirty = true;
    }

    public void extend(int amount) {
        float[] newData = Arrays.copyOf(data, data.length + amount);
        data = newData;
    }

    public int size() {
        return size;
    }

    public int capacity() {
        return data.length;
    }

    public int remaining() {
        return data.length - size;
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

    public void bufferData() {
        glBindVertexArray(vao);

        if (size > 0 && dirty) {
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);
        }

        dirty = false;
        glBindVertexArray(0);
    }

    public void draw() {
        glBindVertexArray(vao);
        glDrawArrays(GL_TRIANGLES, 0, size);
    }

    public void appendQuad(Vector3f tl, Vector3f tr, Vector3f bl, Vector3f br,
                       Vector2f uv0, Vector2f uv1, Vector2f uv2, Vector2f uv3) {
        // TR
        buffer.put(tr.x);
        buffer.put(tr.y);
        buffer.put(tr.z);
        buffer.put(uv1.x);
        buffer.put(uv1.y);

        // TL
        buffer.put(tl.x);
        buffer.put(tl.y);
        buffer.put(tl.z);
        buffer.put(uv0.x);
        buffer.put(uv0.y);

        // BL
        buffer.put(bl.x);
        buffer.put(bl.y);
        buffer.put(bl.z);
        buffer.put(uv2.x);
        buffer.put(uv2.y);

        // BR
        buffer.put(br.x);
        buffer.put(br.y);
        buffer.put(br.z);
        buffer.put(uv3.x);
        buffer.put(uv3.y);

        // TR
        buffer.put(tr.x);
        buffer.put(tr.y);
        buffer.put(tr.z);
        buffer.put(uv1.x);
        buffer.put(uv1.y);

        // BL
        buffer.put(bl.x);
        buffer.put(bl.y);
        buffer.put(bl.z);
        buffer.put(uv2.x);
        buffer.put(uv2.y);

        size += STRIDE * 6;
    }

    /*
    public void appendQuad(Vector3f tl, Vector3f tr, Vector3f bl, Vector3f br,
                           Vector2f uv0, Vector2f uv1, Vector2f uv2, Vector2f uv3) {
        // uvs:
        // TR
        // BR
        // BL
        // TL

        // TR
        data[size + 0]  = tr.x;
        data[size + 1]  = tr.y;
        data[size + 2]  = tr.z;
        data[size + 3]  = uv1.x;
        data[size + 4]  = uv1.y;

        // TL
        data[size + 5]  = tl.x;
        data[size + 6]  = tl.y;
        data[size + 7]  = tl.z;
        data[size + 8]  = uv0.x;
        data[size + 9]  = uv0.y;

        // BL
        data[size + 10] = bl.x;
        data[size + 11] = bl.y;
        data[size + 12] = bl.z;
        data[size + 13] = uv2.x;
        data[size + 14] = uv2.y;

        // BR
        data[size + 15] = br.x;
        data[size + 16] = br.y;
        data[size + 17] = br.z;
        data[size + 18] = uv3.x;
        data[size + 19] = uv3.y;

        // TR
        data[size + 20] = tr.x;
        data[size + 21] = tr.y;
        data[size + 22] = tr.z;
        data[size + 23] = uv1.x;
        data[size + 24] = uv1.y;

        // BL
        data[size + 25] = bl.x;
        data[size + 26] = bl.y;
        data[size + 27] = bl.z;
        data[size + 28] = uv2.x;
        data[size + 29] = uv2.y;

        size += STRIDE * 6;
    }
     */
}
