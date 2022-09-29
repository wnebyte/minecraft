package com.github.wnebyte.minecraft.renderer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

import com.github.wnebyte.minecraft.mycomponents.MyBlock;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.joml.Matrix4f;
import com.github.wnebyte.minecraft.core.Camera;
import com.github.wnebyte.minecraft.core.Transform;
import com.github.wnebyte.minecraft.light.Light;
import com.github.wnebyte.minecraft.light.Caster;
import com.github.wnebyte.minecraft.light.PointCaster;
import com.github.wnebyte.minecraft.light.DirectionalCaster;
import com.github.wnebyte.minecraft.util.Assets;
import com.github.wnebyte.minecraft.util.LightBuilder;
import com.github.wnebyte.minecraft.util.Collections;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

public class Batch {

    //                                                   VAO
    // ==============================================================================================================
    // Pos                   Color                         Tex Coords      Tex Id    Mat Id    Normal              //
    // float, float,float    float, float, float, float    float, float    float     float     float, float, float //
    // ==============================================================================================================

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

    /*
    TEXTURE COORDINATES:
    0,1     1,1
    +---------+
    |         |
    |         |
    +---------+
    0,0     1,0
     */

    // 8
    public static final float[] POS = {
            // 8 vertices per cube
            -0.5f,  0.5f,  0.5f,
             0.5f,  0.5f,  0.5f,
            -0.5f, -0.5f,  0.5f,
             0.5f, -0.5f,  0.5f,
            -0.5f,  0.5f, -0.5f,
             0.5f,  0.5f, -0.5f,
            -0.5f, -0.5f, -0.5f,
             0.5f, -0.5f, -0.5f
    };

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

    // TR, TL, BL,    BR, TR, BL

    // 6 * 6 = 36
    // 6 vertex indices per face
    public static final int[] INDICES = {
            // Each set of 6 indices represents one quad
            1, 0, 2,    3, 1, 2,    // Front face
            5, 1, 3,    7, 5, 3,    // Right face
            7, 6, 4,    5, 7, 4,    // Back face
            0, 4, 6,    2, 0, 6,    // Left face
            5, 4, 0,    1, 5, 0,    // Top face
            3, 2, 6,    7, 3, 6     // Bottom face
    };

    // Tex-coords always loop with the triangle going:
    //		top-right, top-left, bottom-left
    //		bottom-right, top-right, bottom-left
    public static final float[][] TEX_COORDS_2D = {
            // ux   uy
            { 1f,   1f }, // TR
            { 0f,   1f }, // TL
            { 0f,   0f }, // BL
            { 1f,   0f }, // BR
            { 1f,   1f }, // TR
            { 0f,   0f }  // BL
    };

    public static final float[][] NORMALS_2D = {
            { 0,  0,  1 },
            { 1,  0,  0 },
            { 0,  0, -1 },
            {-1,  0,  0 },
            { 0,  1,  0 },
            { 0, -1,  0 }
    };

    public static final float[][] TANGENTS_2D = {
            { 1, 0,  0 },
            { 0, 0,  1 },
            {-1, 0,  0 },
            { 0, 0, -1 },
            { 1, 0,  0 },
            {-1, 0,  0 }
    };

    public static final float[][] BI_TANGENTS_2D = {
            { 0,  1,  0 },
            { 0,  1,  0 },
            { 0, -1,  0 },
            { 0, -1,  0 },
            { 0,  0,  1 },
            { 0,  0, -1 }
    };

    public static final int POS_SIZE = 3;

    public static final int TEX_ID_SIZE = 1;

    public static final int TEX_COORDS_SIZE = 2;

    public static final int NORMAL_SIZE = 3;

    public static final int POS_OFFSET = 0;

    public static final int TEX_ID_OFFSET = POS_OFFSET + (POS_SIZE * Float.BYTES);

    public static final int TEX_COORDS_OFFSET = TEX_ID_OFFSET + (TEX_ID_SIZE * Float.BYTES);

    public static final int NORMAL_OFFSET = TEX_COORDS_OFFSET + (TEX_COORDS_SIZE * Float.BYTES);

    public static final int STRIDE = POS_SIZE + TEX_ID_SIZE + TEX_COORDS_SIZE + NORMAL_SIZE;

    public static final int STRIDE_BYTES = STRIDE * Float.BYTES;

    private static final int DEFAULT_MAX_BATCH_SIZE = 100;

    private static final int[] TEX_SLOTS = { 0, 1, 2, 3, 4, 5, 6, 7 };

    private int vaoID;

    private int vboID;

    private int vaoLsID;

    private int vboLsID;

    private Shader shader;

    private Camera camera;

    public float[] vertices;

    private MyBlock[] blocks;

    private int size;

    private int maxBatchSize;

    private boolean hasSpace;

    private List<Texture> textures;

    private boolean started;

    public static final Light LIGHT = new LightBuilder()
            .setAmbient(new Vector3f(0.2f, 0.2f, 0.2f))
            .setDiffuse(new Vector3f(0.5f, 0.5f, 0.5f))
            .setSpecular(new Vector3f(1.0f, 1.0f, 1.0f))
            .build();

    public static final List<PointCaster> POINT_CASTERS = new ArrayList<PointCaster>() {
        { add(new PointCaster(new Vector3f(0.7f, 0.2f, 2.0f), LIGHT,
                1.0f, 0.09f, 0.032f)); }
        { add(new PointCaster(new Vector3f(2.3f, -3.3f, -4.0f), LIGHT,
                1.0f, 0.09f, 0.032f)); }
        { add(new PointCaster(new Vector3f(-4.0f, 2.0f, -12.0f), LIGHT,
                1.0f, 0.09f, 0.032f)); }
        { add(new PointCaster(new Vector3f(0.0f, 0.0f, -3.0f), LIGHT,
                1.0f, 0.09f, 0.032f)); }
    };

    public static final DirectionalCaster DIR_CASTERS =
            new DirectionalCaster(new Vector3f(-0.2f, -1.0f, -0.3f), LIGHT);

    public static final List<Caster> CASTERS = new ArrayList<Caster>() {
        { addAll(POINT_CASTERS); }
        { add(DIR_CASTERS); }
    };

    public Batch(Camera camera) {
        this(DEFAULT_MAX_BATCH_SIZE, camera);
    }

    public Batch(int maxBatchSize, Camera camera) {
        this.vertices = new float[36 * STRIDE * maxBatchSize];
        this.blocks = new MyBlock[maxBatchSize];
        this.size = 0;
        this.hasSpace = true;
        this.textures = new ArrayList<>(16);
        this.maxBatchSize = maxBatchSize;
        this.camera = camera;
        this.shader = Assets.getShader("C:/users/ralle/dev/java/minecraft/assets/shaders/lit.glsl");
    }

    public void start() {
        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        glVertexAttribPointer(0, POS_SIZE, GL_FLOAT, false, STRIDE_BYTES, POS_OFFSET);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, TEX_ID_SIZE, GL_FLOAT, false, STRIDE_BYTES, TEX_ID_OFFSET);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2, TEX_COORDS_SIZE, GL_FLOAT, false, STRIDE_BYTES, TEX_COORDS_OFFSET);
        glEnableVertexAttribArray(2);

        glVertexAttribPointer(3, NORMAL_SIZE, GL_FLOAT, false, STRIDE_BYTES, NORMAL_OFFSET);
        glEnableVertexAttribArray(3);
    }

    public void rebuffer() {
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);
    }

    public void render() {
        if (!started) {
            start();
            started = true;
        }
        if (shouldRebuffer()) {
            glBindBuffer(GL_ARRAY_BUFFER, vboID);
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);
        }

        shader.use();
        shader.uploadMatrix4f(Shader.U_VIEW, camera.getViewMatrix());
        shader.uploadMatrix4f(Shader.U_PROJECTION, camera.getProjectionMatrix());
        shader.uploadVec3f(Shader.U_VIEW_POS, camera.getPosition());
        shader.uploadDirectionalCaster(Shader.U_DIR_LIGHT, DIR_CASTERS);

        int i = 0;
        for (Texture texture : textures) {
            glActiveTexture(GL_TEXTURE0 + i);
            texture.bind();
            i++;
        }
        shader.uploadIntArray(Shader.U_TEXTURES, TEX_SLOTS);

        glBindVertexArray(vaoID);
        glDrawArrays(GL_TRIANGLES, 0, 36 * maxBatchSize);

        for (Texture texture : textures) {
            texture.unbind();
        }

        glBindVertexArray(0);
        shader.detach();
    }

    public void destroy() {
        glDeleteBuffers(vboID);
        glDeleteVertexArrays(vaoID);
        glDeleteBuffers(vboLsID);
        glDeleteVertexArrays(vaoLsID);
    }

    public void add(MyBlock block) {
        int index = size;
        blocks[index] = block;
        size++;
        addTextures(block);
        loadVertexAttributes(index);
        if (size >= maxBatchSize) {
            hasSpace = false;
        }
    }

    public boolean destroy(MyBlock block) {
        for (int i = 0; i < size; i++) {
            MyBlock b = blocks[i];
            if (b.equals(block)) {
                for (int j = i; j < size - 1; j++) {
                    blocks[j] = blocks[j + 1];
                    blocks[j].setDirty();
                }
                size--;
                return true;
            }
        }
        return false;
    }

    private boolean shouldRebuffer() {
        boolean rebuffer = false;
        for (int i = 0; i < size; i++) {
            MyBlock block = blocks[i];
            if (block.isDirty()) {
                loadVertexAttributes(i);
                block.setClean();
                rebuffer = true;
            }
        }
        return rebuffer;
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

    /*
    public void loadVertexAttributes(int index) {
        MyBlock block = blocks[index];
        Vector4f[] vertexArray = genVertexArray(block.transform);
        int offset = index * 36 * STRIDE;

        for (int i = 0; i < Renderer.INDICES.length; i++) {
            int j = Renderer.INDICES[i];
            Vector4f pos = vertexArray[j];
            Vector2f texCoords = block.getTexCoords(i % 6);
            int materialId = (block.getMaterial() == null) ? -1 : materials.indexOf(block.getMaterial());
            float[] normals = NORMALS_2D[i / 6];
            vertices[offset]     = pos.x;
            vertices[offset + 1] = pos.y;
            vertices[offset + 2] = pos.z;
            vertices[offset + 3] = materialId;
            vertices[offset + 4] = texCoords.x;
            vertices[offset + 5] = texCoords.y;
            vertices[offset + 6] = normals[0];
            vertices[offset + 7] = normals[1];
            vertices[offset + 8] = normals[2];
            offset += STRIDE;
        }
    }
     */

    /*
    FRONT
    RIGHT
    BACK
    LEFT
    TOP
    BOTTOM
    */

    private List<Integer> SIDE_INDICES = Arrays.asList(
            0, 1, 2, 3,
            6, 7, 8, 9,
            12,13,14,15,
            18,19,20,21,
            24,25,26,27,
            30,31,32,33
    );

    private List<Integer> TOP_INDICES = Arrays.asList(
            4,
            10,
            16,
            22,
            28,
            34
    );

    private List<Integer> BOTTOM_INDICES = Arrays.asList(
            5,
            11,
            17,
            23,
            29,
            35
    );

    public void loadVertexAttributes(int index) {
        MyBlock block = blocks[index];
        Vector4f[] vertexArray = genVertexArray(block.transform);
        int offset = index * 36 * STRIDE;

        for (int i = 0; i < Batch.INDICES.length; i++) {
            int j = Batch.INDICES[i];
            Vector4f pos = vertexArray[j];
            Vector2f texCoords = block.getTexCoords(i % 6);
            int texId = -1;
            if (SIDE_INDICES.contains(i)) {
                texId = textures.indexOf(block.getSideTexture());
            }
            else if (TOP_INDICES.contains(i)) {
                texId = textures.indexOf(block.getTopTexture());
            }
            else if (BOTTOM_INDICES.contains(i)) {
                texId = textures.indexOf(block.getBottomTexture());
            }
            float[] normals = NORMALS_2D[i / 6];
            vertices[offset]     = pos.x;
            vertices[offset + 1] = pos.y;
            vertices[offset + 2] = pos.z;
            vertices[offset + 3] = texId;
            vertices[offset + 4] = texCoords.x;
            vertices[offset + 5] = texCoords.y;
            vertices[offset + 6] = normals[0];
            vertices[offset + 7] = normals[1];
            vertices[offset + 8] = normals[2];
            offset += STRIDE;
        }
    }

    public int[] genIndices() {
        // 6 indices per quad, six quads per cube
        int[] elements = new int[maxBatchSize * 36];
        for (int i = 0; i < elements.length; i++) {
            elements[i] = Batch.INDICES[(i % 36)] + ((i / 36) * 4);
        }
        return elements;
    }

    public boolean hasSpace() {
        return hasSpace;
    }

    public boolean hasTexture(Texture texture) {
        return textures.contains(texture);
    }

    public boolean hasSpace(List<Texture> textures) {
        int space = textures.size() - Collections.intersection(textures, this.textures);
        return (hasSpace && new HashSet<Texture>(textures).size() + space <= 8);
    }

    public void addTextures(MyBlock block) {
        for (Texture texture : block.getTextures()) {
            if (!hasTexture(texture)) {
                textures.add(texture);
            }
        }
    }

    public MyBlock[] getBlocks() {
        return blocks;
    }
}
