package com.github.wnebyte.minecraft.renderer;

import java.util.Arrays;
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

public class Vertex3DBatchRenderer implements Batch<Vertex3D> {

    private static final int POS_SIZE = 3;

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

    private int vaoID;

    private int vboID;

    private int size;

    private boolean started;

    private final float[] data;

    private final Shader shader;

    private final CapacitySet<Integer> textures;

    public Vertex3DBatchRenderer() {
        this.data = new float[BATCH_SIZE * STRIDE];
        this.shader = Assets.getShader(Assets.DIR + "/shaders/vertex3D.glsl");
        this.textures = new CapacitySet<>(TEX_SLOTS.length);
        this.size = 0;
    }

    @Override
    public void start() {
        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, BATCH_SIZE * STRIDE_BYTES, GL_DYNAMIC_DRAW);

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
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferSubData(GL_ARRAY_BUFFER, 0, data);

        shader.use();
        shader.uploadMatrix4f(Shader.U_VIEW, viewMatrix);
        shader.uploadMatrix4f(Shader.U_PROJECTION, projectionMatrix);
        int i = 0;
        for (int texId : textures) {
            glActiveTexture(GL_TEXTURE0 + i);
            glBindTexture(GL_TEXTURE_2D, texId);
            i++;
        }
        shader.uploadIntArray(Shader.U_TEXTURES, TEX_SLOTS);

        glBindVertexArray(vaoID);
        glDrawArrays(GL_TRIANGLES, 0, size);
        glBindVertexArray(0);

        // Reset batch for use on the next draw call
        Arrays.fill(data, 0, size * STRIDE, 0.0f);
        size = 0;
        glBindTexture(GL_TEXTURE_2D, 0);
        shader.detach();
    }

    @Override
    public void render(Camera camera) {
        render(camera.getViewMatrix(), camera.getProjectionMatrix());
    }

    @Override
    public boolean add(Vertex3D vertex) {
        if (atCapacity(vertex)) {
            return false;
        }
        int index = size * STRIDE;
        loadVertexProperties(index, vertex);
        size++;
        return true;
    }

    private void loadVertexProperties(int index, Vertex3D vertex) {
        Vector3f position = vertex.getPosition();
        Vector3f color = vertex.getColor();
        Vector2f uv = vertex.getTexCoords();
        int texId = vertex.getTexId();
        data[index + 0] = position.x;
        data[index + 1] = position.y;
        data[index + 2] = position.z;
        data[index + 3] = color.x;
        data[index + 4] = color.y;
        data[index + 5] = color.z;
        data[index + 6] = uv.x;
        data[index + 7] = uv.y;
        data[index + 8] = textures.indexOf(texId);
    }

    private boolean atCapacity(Vertex3D element) {
        return (size >= BATCH_SIZE || atTexCapacity(element));
    }

    private boolean atTexCapacity(Vertex3D element) {
        return (element.getTexId() >= 0) && !(textures.add(element.getTexId()));
    }

    @Override
    public void destroy() {
        glDeleteBuffers(vboID);
        glDeleteVertexArrays(vaoID);
    }

    @Override
    public int zIndex() {
        return 0;
    }
}
