package com.github.wnebyte.minecraft.renderer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class ScreenRenderer {

    private static final int POS_SIZE = 3;

    private static final int UV_SIZE = 2;

    private static final int POS_OFFSET = 0;

    private static final int UV_OFFSET = POS_OFFSET + (POS_SIZE * Float.BYTES);

    private static final int STRIDE = POS_SIZE + UV_SIZE;

    private static final int STRIDE_BYTES = STRIDE * Float.BYTES;

    private static final float[] VERTICES = {
            -1.0f, -1.0f,  0.0f,  0.0f,  0.0f,
             1.0f, -1.0f,  0.0f,  1.0f,  0.0f,
             1.0f,  1.0f,  0.0f,  1.0f,  1.0f,
             1.0f,  1.0f,  0.0f,  1.0f,  1.0f,
            -1.0f,  1.0f,  0.0f,  0.0f,  1.0f,
            -1.0f, -1.0f,  0.0f,  0.0f,  0.0f
    };

    private static int vaoID;

    private static int vboID;

    private static boolean started;

    public static void start() {
        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, VERTICES, GL_STATIC_DRAW);

        glVertexAttribPointer(0, POS_SIZE, GL_FLOAT, false, STRIDE_BYTES, POS_OFFSET);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, UV_SIZE, GL_FLOAT, false, STRIDE_BYTES, UV_OFFSET);
        glEnableVertexAttribArray(1);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        started = true;
    }

    public static void render() {
        if (!started) {
            start();
        }
        glBindVertexArray(vaoID);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindVertexArray(0);
    }
}
