package com.github.wnebyte.minecraft.renderer;

import java.util.Arrays;
import org.joml.Vector2f;
import com.github.wnebyte.minecraft.core.Camera;
import com.github.wnebyte.minecraft.fonts.SFont;
import com.github.wnebyte.minecraft.fonts.CharInfo;
import com.github.wnebyte.minecraft.components.Text2D;
import com.github.wnebyte.minecraft.util.Assets;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

public class TextRenderBatch {

    //                              VAO
    // =============================================================
    // Pos                Color                     UV            //
    // float, float       float, float, float       float, float  //
    // =============================================================

    private static final int POS_SIZE = 2;

    private static final int COLOR_SIZE = 3;

    private static final int UV_SIZE = 2;

    private static final int POS_OFFSET = 0;

    private static final int COLOR_OFFSET = POS_OFFSET + (POS_SIZE * Float.BYTES);

    private static final int UV_OFFSET = COLOR_OFFSET + (COLOR_SIZE * Float.BYTES);

    private static final int STRIDE = POS_SIZE + COLOR_SIZE + UV_SIZE;

    private static final int STRIDE_BYTES = STRIDE * Float.BYTES;

    private static final int BATCH_SIZE = 100;

    private static int[] INDICES = {
            0, 1, 3,
            1, 2, 3
    };

    private float[] vertices;

    private int vao;

    private int vbo;

    private int size;

    private Shader shader;

    private SFont font;

    private Camera camera;

    private boolean started;

    public TextRenderBatch(Camera camera) {
        this.vertices = new float[BATCH_SIZE * STRIDE];
        this.size = 0;
        this.camera = camera;
        this.shader = Assets.getShader("C:/users/ralle/dev/java/minecraft/assets/shaders/font.glsl");
        this.font = Assets.getFont("C:/users/ralle/dev/java/minecraft/assets/fonts/Minecraft.ttf", 16);
    }

    public void start() {
        // Generate and bind a Vertex Array Object
        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        // Allocate space for vertices
        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, Float.BYTES * STRIDE * BATCH_SIZE, GL_DYNAMIC_DRAW);

        // Create and upload the indices buffer
        int eboID = glGenBuffers();
        int[] indices = genIndices();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        // Enable the buffer attribute pointers
        glVertexAttribPointer(0, POS_SIZE, GL_FLOAT, false, STRIDE_BYTES, POS_OFFSET);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, COLOR_SIZE, GL_FLOAT, false, STRIDE_BYTES, COLOR_OFFSET);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2, UV_SIZE, GL_FLOAT, false, STRIDE_BYTES, UV_OFFSET);
        glEnableVertexAttribArray(2);

        started = true;
    }

    public void flush() {
        // clear the buffer on the GPU, then upload the CPU contents, then draw
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        // Allocate some memory on the GPU
        glBufferData(GL_ARRAY_BUFFER, Float.BYTES * STRIDE * BATCH_SIZE, GL_DYNAMIC_DRAW);
        // Upload vertex data to the GPU
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);

        // Draw the buffer that we just uploaded
        shader.use();
        // glActiveTexture selects which texture unit subsequent texture state calls will affect
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, font.getTextureId());
        shader.uploadTexture(Shader.U_FONT_TEXTURE, 0);
        shader.uploadMatrix4f(Shader.U_PROJECTION, camera.getProjectionMatrixHUD());

        glBindVertexArray(vao);
        glDrawElements(GL_TRIANGLES, size * 6, GL_UNSIGNED_INT, 0);

        // Reset batch for use on the next draw call
        size = 0;
        Arrays.fill(vertices, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
        shader.detach();
    }

    public void destroy() {
        glDeleteBuffers(vbo);
        glDeleteVertexArrays(vao);
    }

    public void addText2D(Text2D text2D) {
        addText(text2D.getText(), text2D.getX(), text2D.getY(), text2D.getScale(), text2D.getRGB());
    }

    public void addText(String text, float x, float y, float scale, int rgb) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            CharInfo info = font.getCharacter(c);
            if (info.getWidth() == 0) {
                System.err.printf("Warning: (TextRenderBatch) Unknown char: '%c'%n", c);
                continue;
            }

            addCharacter(info, x, y, scale, rgb);
            x += info.getWidth() * scale;
        }
    }

    public void addCharacter(CharInfo info, float x, float y, float scale, int rgb) {
        // if we have no more room - flush and start with a fresh batch
        if (size >= BATCH_SIZE - 4) {
            flush();
        }

        // char info
        int width = info.getWidth();
        int height = info.getHeight();
        Vector2f[] uvs = info.getTexCoords();

        // position
        float x0 = x;
        float y0 = y;
        float x1 = x + scale * width;
        float y1 = y + scale * height;

        // color
        float r = (float)((rgb >> 16) & 0xFF) / 255.0f;
        float g = (float)((rgb >> 8)  & 0xFF) / 255.0f;
        float b = (float)((rgb >> 0)  & 0xFF) / 255.0f;

        // tex coords
        float ux0 = uvs[0].x;
        float uy0 = uvs[0].y;
        float ux1 = uvs[1].x;
        float uy1 = uvs[1].y;

        // load vertex properties
        int index = size * STRIDE;
        vertices[index]     = x1;
        vertices[index + 1] = y0;
        vertices[index + 2] = r;
        vertices[index + 3] = g;
        vertices[index + 4] = b;
        vertices[index + 5] = ux1;
        vertices[index + 6] = uy0;

        index += STRIDE;
        vertices[index]     = x1;
        vertices[index + 1] = y1;
        vertices[index + 2] = r;
        vertices[index + 3] = g;
        vertices[index + 4] = b;
        vertices[index + 5] = ux1;
        vertices[index + 6] = uy1;

        index += STRIDE;
        vertices[index]     = x0;
        vertices[index + 1] = y1;
        vertices[index + 2] = r;
        vertices[index + 3] = g;
        vertices[index + 4] = b;
        vertices[index + 5] = ux0;
        vertices[index + 6] = uy1;

        index += STRIDE;
        vertices[index]     = x0;
        vertices[index + 1] = y0;
        vertices[index + 2] = r;
        vertices[index + 3] = g;
        vertices[index + 4] = b;
        vertices[index + 5] = ux0;
        vertices[index + 6] = uy0;

        size += 4;
    }

    private int[] genIndices() {
        // 3 indices per triangle
        int[] elements = new int[BATCH_SIZE * 3];
        for (int i = 0; i < elements.length; i++) {
            elements[i] = INDICES[(i % 6)] + ((i / 6) * 4);
        }
        return elements;
    }

    public boolean isStarted() {
        return started;
    }
}
