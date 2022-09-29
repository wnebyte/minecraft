package com.github.wnebyte.minecraft.renderer;

import com.github.wnebyte.minecraft.core.Camera;
import com.github.wnebyte.minecraft.util.Assets;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL40.GL_DRAW_INDIRECT_BUFFER;
import static org.lwjgl.opengl.GL43.glMultiDrawElementsIndirect;

public class MultiDrawElementsIndirectRendererCube {

    private static float[] toArray(float[][] array) {
        float[] vertices = new float[array.length * array[0].length];
        int index = 0;
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                vertices[index++] = array[i][j];
            }
        }
        return vertices;
    }

    // The 8 vertices will look like this:
    //   v4 ----------- v5
    //   /|            /|      Axis orientation
    //  / |           / |
    // v0 --------- v1  |      y
    // |  |         |   |      |
    // |  v6 -------|-- v7     +--- x
    // | /          |  /      /
    // |/           | /      z
    // v2 --------- v3

    private static final float[][] VERTICES = {
            /* front face */
            { -1.0f, -1.0f,  1.0f },
            {  1.0f, -1.0f,  1.0f },
            { -1.0f,  1.0f,  1.0f },
            {  1.0f,  1.0f,  1.0f },
            /* back face */
            {  1.0f, -1.0f, -1.0f },
            { -1.0f, -1.0f, -1.0f },
            {  1.0f,  1.0f, -1.0f },
            { -1.0f,  1.0f, -1.0f },
            /* left  face */
            { -1.0f, -1.0f, -1.0f },
            { -1.0f, -1.0f,  1.0f },
            { -1.0f,  1.0f, -1.0f },
            { -1.0f,  1.0f,  1.0f },
            /* right face */
            {  1.0f, -1.0f,  1.0f },
            {  1.0f, -1.0f, -1.0f },
            {  1.0f,  1.0f,  1.0f },
            {  1.0f,  1.0f, -1.0f },
            /* top face */
            { -1.0f,  1.0f,  1.0f },
            {  1.0f,  1.0f,  1.0f },
            { -1.0f,  1.0f, -1.0f },
            {  1.0f,  1.0f, -1.0f },
            /* bottom face */
            {  1.0f, -1.0f,  1.0f },
            { -1.0f, -1.0f,  1.0f },
            {  1.0f, -1.0f, -1.0f },
            { -1.0f, -1.0f, -1.0f },
    };

    private static final int[] INDICES = {
            0, 1, 2,  2, 1, 3,	/* front */
            4, 5, 6,  6, 5, 7,	/* back */
            8, 9,10, 10, 9,11,	/* left */
            12,13,14, 14,13,15,	/* right */
            16,17,18, 18,17,19,	/* top */
            20,21,22, 22,21,23	/* bottom */
    };

    public static final float[][] UVS = {
            // ux   uy
            { 1f,   1f }, // TR
            { 0f,   1f }, // TL
            { 0f,   0f }, // BL
            { 1f,   0f }, // BR
            { 1f,   1f }, // TR
            { 0f,   0f }  // BL
    };

    private static final int POS_SIZE = 3;

    private static final int POS_OFFSET = 0;

    private static final int STRIDE = POS_SIZE;

    private static final int STRIDE_BYTES = STRIDE * Float.BYTES;

    private static final int NUM_COMMANDS = 1;

    private Shader shader;

    private Camera camera;

    public MultiDrawElementsIndirectRendererCube(Camera camera) {
        this.camera = camera;
        this.shader = Assets.getShader("C:/users/ralle/dev/java/minecraft/assets/shaders/cube.glsl");
    }

    public void start() {
        int vao = glGenVertexArrays();
        glBindVertexArray(vao);

        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, toArray(VERTICES), GL_STATIC_DRAW);

        int ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, INDICES, GL_STATIC_DRAW);

        glVertexAttribPointer(0, POS_SIZE, GL_FLOAT, false, STRIDE_BYTES, POS_OFFSET);
        glEnableVertexAttribArray(0);

        int index = 0;
        int[] vDrawCommands = new int[5 * NUM_COMMANDS];
        for (int i = 0; i < NUM_COMMANDS; i++) {
            vDrawCommands[index]     = 36;     // vertexCount
            vDrawCommands[index + 1] = 1;      // instanceCount (how many copies of the geometry we should draw)
            vDrawCommands[index + 2] = 0;      // firstIndex
            vDrawCommands[index + 3] = i * 24; // baseVertex (unique to drawElements)
            vDrawCommands[index + 4] = i;      // baseInstance
            index += 5;
        }

        int ibo = glGenBuffers();
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, ibo);
        glBufferData(GL_DRAW_INDIRECT_BUFFER, vDrawCommands, GL_STATIC_DRAW);
    }

    public void render() {
        shader.use();
        shader.uploadMatrix4f(Shader.U_PROJECTION, camera.getProjectionMatrix());
        shader.uploadMatrix4f(Shader.U_VIEW, camera.getViewMatrix());
        // param mode specifies what kind of primitive to render
        // param indirect is either an offset, in bytes, into the buffer bound to
        // GL_DRAW_INDIRECT_BUFFER or a pointer to an array struct that holds draw parameters
        // param drawCount the number of elements in the array addresses by indirect
        // param stride is the distance, in bytes, between the elements of the indirect array
        glMultiDrawElementsIndirect(GL_TRIANGLES, GL_UNSIGNED_INT, 0, NUM_COMMANDS, 0);
        shader.detach();
    }
}
