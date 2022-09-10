package com.github.wnebyte.minecraft.renderer;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import com.github.wnebyte.minecraft.light.Caster;
import com.github.wnebyte.minecraft.light.DirectionalCaster;
import com.github.wnebyte.minecraft.light.Light;
import com.github.wnebyte.minecraft.light.PointCaster;
import com.github.wnebyte.minecraft.util.LightBuilder;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.joml.Matrix4f;
import com.github.wnebyte.minecraft.core.Camera;
import com.github.wnebyte.minecraft.core.Transform;
import com.github.wnebyte.minecraft.components.Block;
import com.github.wnebyte.minecraft.util.Assets;
import com.github.wnebyte.minecraft.util.Collections;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

public class Renderer {

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

    // 6 * 6 = 36
    // 6 vertex indices per face
    private static final int[] INDICES = {
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
    private static final float[][] TEX_COORDS_2D = {
            // ux   uy
            { 1f,   1f }, // TR
            { 0f,   1f }, // TL
            { 0f,   0f }, // BL
            { 1f,   0f }, // BR
            { 1f,   1f }, // TR
            { 0f,   0f }  // BL
    };

    private static final float[][] NORMALS_2D = {
            { 0,  0,  1 },
            { 1,  0,  0 },
            { 0,  0, -1 },
            {-1,  0,  0 },
            { 0,  1,  0 },
            { 0, -1,  0 }
    };

    private static final float[][] TANGENTS_2D = {
            { 1, 0,  0 },
            { 0, 0,  1 },
            {-1, 0,  0 },
            { 0, 0, -1 },
            { 1, 0,  0 },
            {-1, 0,  0 }
    };

    private static final float[][] BI_TANGENTS_2D = {
            { 0,  1,  0 },
            { 0,  1,  0 },
            { 0, -1,  0 },
            { 0, -1,  0 },
            { 0,  0,  1 },
            { 0,  0, -1 }
    };

    private static final int POS_SIZE = 3;

    private static final int MAT_ID_SIZE = 1;

    private static final int TEX_COORDS_SIZE = 2;

    private static final int NORMAL_SIZE = 3;

    private static final int POS_OFFSET = 0;

    private static final int MAT_ID_OFFSET = POS_OFFSET + (POS_SIZE * Float.BYTES);

    private static final int TEX_COORDS_OFFSET = MAT_ID_OFFSET + (MAT_ID_SIZE * Float.BYTES);

    private static final int NORMAL_OFFSET = TEX_COORDS_OFFSET + (TEX_COORDS_SIZE * Float.BYTES);

    private static final int STRIDE = POS_SIZE + MAT_ID_SIZE + TEX_COORDS_SIZE + NORMAL_SIZE;

    private static final int STRIDE_BYTES = STRIDE * Float.BYTES;

    private static final int DEFAULT_MAX_BATCH_SIZE = 100;

    private static final int[] TEX_SLOTS = { 0, 1, 2, 3, 4, 5, 6, 7 };

    private int vaoID;

    private int vboID;

    private int vaoLsID;

    private int vboLsID;

    private Shader shader;

    private Camera camera;

    public float[] vertices;

    private Block[] blocks;

    private int size;

    private int maxBatchSize;

    private boolean hasSpace;

    private List<Material> materials;

    public static Light light = new LightBuilder()
            .setAmbient(new Vector3f(0.2f, 0.2f, 0.2f))
            .setDiffuse(new Vector3f(0.5f, 0.5f, 0.5f))
            .setSpecular(new Vector3f(1.0f, 1.0f, 1.0f))
            .build();

    public static List<PointCaster> pointCasters = new ArrayList<PointCaster>() {
        { add(new PointCaster(new Vector3f(0.7f, 0.2f, 2.0f), light,
                1.0f, 0.09f, 0.032f)); }
        { add(new PointCaster(new Vector3f(2.3f, -3.3f, -4.0f), light,
                1.0f, 0.09f, 0.032f)); }
        { add(new PointCaster(new Vector3f(-4.0f, 2.0f, -12.0f), light,
                1.0f, 0.09f, 0.032f)); }
        { add(new PointCaster(new Vector3f(0.0f, 0.0f, -3.0f), light,
                1.0f, 0.09f, 0.032f)); }
    };

    public static DirectionalCaster dirCaster = new DirectionalCaster(new Vector3f(-0.2f, -1.0f, -0.3f), light);

    public static List<Caster> casters = new ArrayList<Caster>() {
        { addAll(pointCasters); }
        { add(dirCaster); }
    };

    public Renderer(Camera camera) {
        this(DEFAULT_MAX_BATCH_SIZE, camera);
    }

    public Renderer(int maxBatchSize, Camera camera) {
        this.vertices = new float[36 * STRIDE * maxBatchSize];
        this.blocks = new Block[maxBatchSize];
        this.size = 0;
        this.hasSpace = true;
        this.materials = new ArrayList<>(8);
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

        glVertexAttribPointer(1, MAT_ID_SIZE, GL_FLOAT, false, STRIDE_BYTES, MAT_ID_OFFSET);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2, TEX_COORDS_SIZE, GL_FLOAT, false, STRIDE_BYTES, TEX_COORDS_OFFSET);
        glEnableVertexAttribArray(2);

        glVertexAttribPointer(3, NORMAL_SIZE, GL_FLOAT, false, STRIDE_BYTES, NORMAL_OFFSET);
        glEnableVertexAttribArray(3);
    }

    public void add(Block block) {
        int index = size;
        blocks[index] = block;
        size++;
        addMaterial(block.getMaterial());
        loadVertexAttributes(index);
        if (size >= maxBatchSize) {
            hasSpace = false;
        }
    }

    public boolean destroy(Block block) {
        for (int i = 0; i < size; i++) {
            Block b = blocks[i];
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
        shader.uploadDirectionalCaster(Shader.U_DIR_LIGHT, dirCaster);

        int i = 0;
        for (Material material : materials) {
            glActiveTexture(GL_TEXTURE0 + i);
            material.getDiffuseMap().bind();
        }
        shader.uploadIntArray(Shader.U_DIFFUSE_MAPS, new int[]{ 0, 1, 2, 3, 4, 5, 6, 7 });

        i = 0;
        for (Material material : materials) {
            glActiveTexture(GL_TEXTURE8 + i);
            material.getSpecularMap().bind();
        }
        shader.uploadIntArray(Shader.U_SPECULAR_MAPS, new int[]{ 8, 9, 10, 11, 12, 13, 14, 15 });

        glBindVertexArray(vaoID);
        glDrawArrays(GL_TRIANGLES, 0, 36 * maxBatchSize);

        for (Material material : materials) {
            for (Texture texture : material.getTextures()) {
                texture.unbind();
            }
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

    public void loadVertexAttributes(int index) {
        Block block = blocks[index];
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

    public int[] genIndices() {
        // 6 indices per quad, six quads per cube
        int[] elements = new int[maxBatchSize * 36];
        for (int i = 0; i < elements.length; i++) {
            elements[i] = Renderer.INDICES[(i % 36)] + ((i / 36) * 4);
        }
        return elements;
    }

    public boolean hasSpace() {
        return hasSpace;
    }

    public boolean hasMaterialSpace() {
        return (materials.size() < 8);
    }

    public boolean hasMaterial(Material material) {
        return materials.contains(material);
    }

    private void addMaterial(Material material) {
        if (material != null && !hasMaterial(material)) {
            materials.add(material);
        }
    }

    public Block[] getBlocks() {
        return blocks;
    }
}
