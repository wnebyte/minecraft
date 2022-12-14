package com.github.wnebyte.minecraft.renderer;

import java.util.*;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Matrix4f;
import com.github.wnebyte.minecraft.core.Camera;
import com.github.wnebyte.minecraft.util.Assets;
import com.github.wnebyte.minecraft.util.CapacitySet;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

public class Vertex2DBatchRenderer implements Batch<Vertex2D> {

    private static int[] genIndices() {
        int[] elements = new int[BATCH_SIZE * 3];
        for (int i = 0; i < elements.length; i++) {
            elements[i] = INDICES[(i % 6)] + ((i / 6) * 4);
        }
        return elements;
    }

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

    private int vaoID;

    private int vboID;

    private int size;

    private boolean started;

    private final float[] data;

    private final Shader shader;

    private final CapacitySet<Integer> textures;

    private final int zIndex;

    private final boolean blend;

    public Vertex2DBatchRenderer(int zIndex, boolean blend) {
        this.zIndex = zIndex;
        this.blend = blend;
        this.data = new float[BATCH_SIZE * STRIDE];
        this.shader = Assets.getShader(Assets.DIR + "/shaders/vertex2D.glsl");
        this.textures = new CapacitySet<>(TEX_SLOTS.length);
        this.size = 0;
    }

    @Override
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

    @Override
    public void render(Matrix4f viewMatrix, Matrix4f projectionMatrix) {
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
        shader.uploadInt(Shader.Z_INDEX, zIndex);
        shader.uploadMatrix4f(Shader.U_PROJECTION, projectionMatrix);
        int i = 0;
        for (int texId : textures) {
            glActiveTexture(GL_TEXTURE0 + i);
            glBindTexture(GL_TEXTURE_2D, texId);
            i++;
        }
        shader.uploadIntArray(Shader.U_TEXTURES, TEX_SLOTS);

        // set render states
        if (blend) {
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        }

        glBindVertexArray(vaoID);
        glDrawElements(GL_TRIANGLES, size + (size / 2), GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);

        // reset render states
        if (blend) {
            glDisable(GL_BLEND);
        }

        // Reset batch for use on the next draw call
        Arrays.fill(data, 0, size * STRIDE, 0.0f);
        size = 0;
        glBindTexture(GL_TEXTURE_2D, 0);
        shader.detach();
    }

    @Override
    public void render(Camera camera) {
        render(camera.getViewMatrixHUD(), camera.getProjectionMatrixHUD());
    }

    @Override
    public boolean add(Vertex2D vertex) {
        if (vertex.getZIndex() != zIndex || vertex.isBlend() ^ blend || atCapacity(vertex)) {
            return false;
        }
        int index = size * STRIDE;
        loadVertexProperties(index, vertex);
        size++;
        return true;
    }

    private void loadVertexProperties(int index, Vertex2D vertex) {
        Vector2f position = vertex.getPosition();
        Vector3f color = vertex.getColor();
        Vector2f uv = vertex.getTexCoords();
        int texId = vertex.getTexId();
        data[index + 0] = position.x;
        data[index + 1] = position.y;
        data[index + 2] = color.x;
        data[index + 3] = color.y;
        data[index + 4] = color.z;
        data[index + 5] = uv.x;
        data[index + 6] = uv.y;
        data[index + 7] = textures.indexOf(texId);
    }

    private boolean atCapacity(Vertex2D vertex) {
        return (size >= BATCH_SIZE || atTexCapacity(vertex));
    }

    private boolean atTexCapacity(Vertex2D vertex) {
        return ((vertex.getTexId() >= 0) && !(textures.add(vertex.getTexId())));
    }

    @Override
    public void destroy() {
        glDeleteBuffers(vboID);
        glDeleteVertexArrays(vaoID);
    }

    @Override
    public int zIndex() {
        return zIndex;
    }
}
