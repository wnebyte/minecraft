package com.github.wnebyte.minecraft.renderer;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
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

public class Vertex2DBatchRenderer {

    private static final int POS_SIZE = 2;

    private static final int COLOR_SIZE = 3;

    private static final int UV_SIZE = 2;

    private static final int TEX_ID_SIZE = 1;

    private static final int POS_OFFSET = 0;

    private static final int COLOR_OFFSET = POS_OFFSET + (POS_SIZE * Float.BYTES);

    private static final int UV_OFFSET = COLOR_OFFSET + (COLOR_SIZE * Float.BYTES);

    private static final int TEX_ID_OFFSET = UV_OFFSET + (UV_SIZE * Float.BYTES);

    private static final int STRIDE = POS_SIZE + COLOR_SIZE + UV_SIZE + TEX_ID_SIZE;

    private static final int STRIDE_BYTES = STRIDE * Float.BYTES;

    private static final int BATCH_SIZE = 100;

    private static final int[] TEX_SLOTS = { 0, 1, 2, 3, 4, 5, 6, 7 };

    private static int[] INDICES = {
            0, 1, 3,
            1, 2, 3
    };

    private float[] data;

    private int vaoID;

    private int vboID;

    private int size;

    private Shader shader;

    private SFont font;

    private Camera camera;

    private List<Texture> textures;

    private boolean started;

    public Vertex2DBatchRenderer(Camera camera) {
        this.camera = camera;
        this.data = new float[BATCH_SIZE * STRIDE];
        this.shader = Assets.getShader(Assets.DIR + "/shaders/vertex2D.glsl");
        this.font = Assets.getFont(Assets.DIR + "/fonts/Minecraft.ttf", 16);
        this.textures = new ArrayList<>(TEX_SLOTS.length);
        this.textures.add(this.font.getTexture());
        this.size = 0;
    }

    public void start() {
        // Generate and bind a Vertex Array Object
        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        // Allocate space for vertices
        vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, BATCH_SIZE * STRIDE_BYTES, GL_DYNAMIC_DRAW);

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

        glVertexAttribPointer(3, TEX_ID_SIZE, GL_FLOAT, false, STRIDE_BYTES, TEX_ID_OFFSET);
        glEnableVertexAttribArray(3);

        started = true;
    }

    public void flush() {
        if (size <= 0) {
            return;
        }
        if (!started) {
            start();
        }
        // Bind the VBO buffer
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        // Upload vertex data to the GPU
        glBufferSubData(GL_ARRAY_BUFFER, 0, data);

        // Draw the buffer that we just uploaded
        shader.use();
        shader.uploadMatrix4f(Shader.U_PROJECTION, camera.getProjectionMatrixHUD());
        for (int i = 0; i < textures.size(); i++) {
            Texture texture = textures.get(i);
            glActiveTexture(GL_TEXTURE0 + i);
            texture.bind();
        }
        shader.uploadIntArray(Shader.U_TEXTURES, TEX_SLOTS);

        glBindVertexArray(vaoID);
        glDrawElements(GL_TRIANGLES, size * 6, GL_UNSIGNED_INT, 0);

        // Reset batch for use on the next draw call
        Arrays.fill(data, 0, size * STRIDE, 0.0f);
        size = 0;
        for (Texture texture : textures) {
            texture.unbind();
        }
        shader.detach();
    }

    public void destroy() {
        glDeleteBuffers(vboID);
        glDeleteVertexArrays(vaoID);
    }

    public boolean addText2D(Text2D text2D) {
        if (size <= BATCH_SIZE - text2D.getText().length()) {
            addText(text2D.getText(), text2D.getX(), text2D.getY(), text2D.getScale(), text2D.getRGB());
            return true;
        } else {
            return false;
        }
    }

    private void addText(String text, float x, float y, float scale, int rgb) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            CharInfo info = font.getCharacter(c);
            if (info.getWidth() == 0) {
                System.err.printf("Warning: (Vertex2DBatchRenderer) Unknown char: '%c'%n", c);
                continue;
            }

            addCharacter(info, x, y, scale, rgb);
            x += info.getWidth() * scale;
        }
    }

    private void addCharacter(CharInfo info, float x, float y, float scale, int rgb) {
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
        int offset = size * STRIDE;
        data[offset + 0] = x1;
        data[offset + 1] = y0;
        data[offset + 2] = r;
        data[offset + 3] = g;
        data[offset + 4] = b;
        data[offset + 5] = ux1;
        data[offset + 6] = uy0;
        data[offset + 7] = 0;
        offset += STRIDE;

        data[offset + 0] = x1;
        data[offset + 1] = y1;
        data[offset + 2] = r;
        data[offset + 3] = g;
        data[offset + 4] = b;
        data[offset + 5] = ux1;
        data[offset + 6] = uy1;
        data[offset + 7] = 0;
        offset += STRIDE;

        data[offset + 0] = x0;
        data[offset + 1] = y1;
        data[offset + 2] = r;
        data[offset + 3] = g;
        data[offset + 4] = b;
        data[offset + 5] = ux0;
        data[offset + 6] = uy1;
        data[offset + 7] = 0;
        offset += STRIDE;

        data[offset + 0] = x0;
        data[offset + 1] = y0;
        data[offset + 2] = r;
        data[offset + 3] = g;
        data[offset + 4] = b;
        data[offset + 5] = ux0;
        data[offset + 6] = uy0;
        data[offset + 7] = 0;

        size += 4;
    }

    public boolean addQuad(float x, float y, float width, float height, float scale, Texture texture, Vector2f[] uvs, int rgb) {
        // if we have no more room - flush and start with a fresh batch
        if (size >= BATCH_SIZE - 4) {
            flush();
            return false;
        }

        if (texture != null && !textures.contains(texture)) {
            textures.add(texture);
        }

        // position
        float x0 = x;
        float y0 = y;
        float x1 = x + scale * width;
        float y1 = y + scale * height;

        // color
        float r = (float)((rgb >> 16) & 0xFF) / 255.0f;
        float g = (float)((rgb >> 8)  & 0xFF) / 255.0f;
        float b = (float)((rgb >> 0)  & 0xFF) / 255.0f;

        // texId
        int texId = (textures == null) ? -1 : textures.indexOf(texture);

        // load vertex properties
        int offset = size * STRIDE;
        data[offset + 0] = x1;
        data[offset + 1] = y0;
        data[offset + 2] = r;
        data[offset + 3] = g;
        data[offset + 4] = b;
        data[offset + 5] = (uvs != null && uvs.length >= 3) ? uvs[0].x : 1.0f;
        data[offset + 6] = (uvs != null && uvs.length >= 3) ? uvs[0].y : 0.0f;
        data[offset + 7] = texId;
        offset += STRIDE;

        data[offset + 0] = x1;
        data[offset + 1] = y1;
        data[offset + 2] = r;
        data[offset + 3] = g;
        data[offset + 4] = b;
        data[offset + 5] = (uvs != null && uvs.length >= 3) ? uvs[1].x : 1.0f;
        data[offset + 6] = (uvs != null && uvs.length >= 3) ? uvs[1].y : 1.0f;
        data[offset + 7] = texId;
        offset += STRIDE;

        data[offset + 0] = x0;
        data[offset + 1] = y1;
        data[offset + 2] = r;
        data[offset + 3] = g;
        data[offset + 4] = b;
        data[offset + 5] = (uvs != null && uvs.length >= 3) ? uvs[2].x : 0.0f;
        data[offset + 6] = (uvs != null && uvs.length >= 3) ? uvs[2].y : 1.0f;
        data[offset + 7] = texId;
        offset += STRIDE;

        data[offset + 0] = x0;
        data[offset + 1] = y0;
        data[offset + 2] = r;
        data[offset + 3] = g;
        data[offset + 4] = b;
        data[offset + 5] = (uvs != null && uvs.length >= 3) ? uvs[3].x : 0.0f;
        data[offset + 6] = (uvs != null && uvs.length >= 3) ? uvs[3].y : 0.0f;
        data[offset + 7] = texId;

        size += 4;

        return true;
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

    public int size() {
        return size;
    }
}
