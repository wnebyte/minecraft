package com.github.wnebyte.minecraft.renderer;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;
import com.github.wnebyte.minecraft.core.Camera;
import com.github.wnebyte.minecraft.core.Transform;
import com.github.wnebyte.minecraft.components.Block;
import com.github.wnebyte.minecraft.util.Assets;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class LightRenderer {

    private static final int POS_SIZE = 3;

    private static final int COLOR_SIZE = 4;

    private static final int POS_OFFSET = 0;

    private static final int COLOR_OFFSET = POS_OFFSET + (POS_SIZE * Float.BYTES);

    private static final int STRIDE = POS_SIZE + COLOR_SIZE;

    private static final int STRIDE_BYTES = STRIDE * Float.BYTES;

    private static final int DEFAULT_MAX_BATCH_SIZE = 100;

    public static final float[][] POS_2D = {
            { -0.5f,  0.5f,  0.5f },
            {  0.5f,  0.5f,  0.5f } ,
            { -0.5f, -0.5f,  0.5f },
            {  0.5f, -0.5f,  0.5f },
            { -0.5f,  0.5f, -0.5f },
            {  0.5f,  0.5f, -0.5f },
            { -0.5f, -0.5f, -0.5f },
            {  0.5f, -0.5f, -0.5f }
    };

    private static final int[] INDICES = {
            1, 0, 2,   3, 1, 2,
            5, 1, 3,   7, 5, 3,
            7, 6, 4,   5, 7, 4,
            0, 4, 6,   2, 0, 6,
            5, 4, 0,   1, 5, 0,
            3, 2, 6,   7, 3, 6
    };

    private int vaoID;

    private int vboID;

    private Camera camera;

    private Shader shader;

    private float[] vertices;

    private Block[] blocks;

    private int size;

    private int maxBatchSize;

    public LightRenderer(Camera camera) {
        this(DEFAULT_MAX_BATCH_SIZE, camera);
    }

    public LightRenderer(int maxBatchSize, Camera camera) {
        this.vertices = new float[36 * STRIDE * maxBatchSize];
        this.blocks = new Block[maxBatchSize];
        this.maxBatchSize = maxBatchSize;
        this.size = 0;
        this.camera = camera;
        this.shader = Assets.getShader("C:/users/ralle/dev/java/minecraft/assets/shaders/nlightsource.glsl");
    }

    public void start() {
        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        glVertexAttribPointer(0, POS_SIZE, GL_FLOAT, false, STRIDE_BYTES, POS_OFFSET);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, COLOR_SIZE, GL_FLOAT, false, STRIDE_BYTES, COLOR_OFFSET);
        glEnableVertexAttribArray(1);
    }

    public void add(Block block) {
        int index = size;
        blocks[index] = block;
        size++;
        loadVertexAttributes(index);
    }

    public boolean rebuffer() {
        boolean rebuffer = false;
        for (int i = 0; i < size; i++) {
            Block block = blocks[i];
            if (block.isDirty()) {
                loadVertexAttributes(i);
                block.setClean();
                rebuffer = true;
            }
        }
        return rebuffer;
    }

    public void render() {
        if (rebuffer()) {
            glBindBuffer(GL_ARRAY_BUFFER, vboID);
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);
        }

        shader.use();
        shader.uploadMatrix4f(Shader.U_VIEW, camera.getViewMatrix());
        shader.uploadMatrix4f(Shader.U_PROJECTION, camera.getProjectionMatrix());
        shader.uploadVec3f(Shader.U_VIEW_POS, camera.getPosition());

        glBindVertexArray(vaoID);
        glDrawArrays(GL_TRIANGLES, 0, 36 * maxBatchSize);

        shader.detach();
    }

    private Vector4f[] genVertexArray(Transform transform) {
        Vector4f[] vec = new Vector4f[8];
        Matrix4f transformMatrix = null;
        boolean isRotated = (transform.rotation != 0.0f);
        if (isRotated) {
            transformMatrix = transform.toMatrix();
        }

        for (int i = 0; i < vec.length; i++) {
            float xAdd = POS_2D[i][0];
            float yAdd = POS_2D[i][1];
            float zAdd = POS_2D[i][2];
            Vector4f pos = new Vector4f(
                    transform.position.x + (xAdd * transform.scale.x),
                    transform.position.y + (yAdd * transform.scale.y),
                    transform.position.z + (zAdd * transform.scale.z),
                    1
            );
            if (isRotated) {
                pos = new Vector4f(xAdd, yAdd, zAdd, 1.0f).mul(transformMatrix);
            }
            vec[i] = pos;
        }

        return vec;
    }

    private void loadVertexAttributes(int index) {
        Block block = blocks[index];
        Vector4f[] vertexArray = genVertexArray(block.transform);
        int offset = index * 36 * STRIDE;

        for (int i = 0; i < INDICES.length; i++) {
            int j = INDICES[i];
            Vector4f pos = vertexArray[j];
            vertices[offset]     = pos.x;
            vertices[offset + 1] = pos.y;
            vertices[offset + 2] = pos.z;
            vertices[offset + 3] = 1.0f;
            vertices[offset + 4] = 1.0f;
            vertices[offset + 5] = 1.0f;
            vertices[offset + 6] = 1.0f;
            offset += STRIDE;
        }
    }
}
