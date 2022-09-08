package com.github.wnebyte.minecraft.renderer;

import java.util.List;
import java.util.ArrayList;
import org.joml.Vector2f;
import org.joml.Vector3f;
import com.github.wnebyte.minecraft.core.Camera;
import com.github.wnebyte.minecraft.core.Transform;
import com.github.wnebyte.minecraft.components.Block;
import com.github.wnebyte.minecraft.util.Assets;
import org.joml.Vector4f;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

public class Renderer {

    //                                                   VAO
    // ================================================================================================================
    // Pos                      Color                            Tex Coords         Tex Id       Normal              //
    // float, float,float       float, float, float, float       float, float       float        float, float, float //
    // ================================================================================================================

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
    private static final float[] POS = {
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

    private static final int COLOR_SIZE = 4;

    private static final int TEX_COORDS_SIZE = 2;

    private static final int TEX_ID_SIZE = 1;

    private static final int NORMAL_SIZE = 3;

    private static final int POS_OFFSET = 0;

    private static final int COLOR_OFFSET = POS_OFFSET + (POS_SIZE * Float.BYTES);

    private static final int TEX_COORDS_OFFSET = COLOR_OFFSET + (COLOR_SIZE * Float.BYTES);

    private static final int TEX_ID_OFFSET = TEX_COORDS_OFFSET + (TEX_COORDS_SIZE * Float.BYTES);

    private static final int NORMAL_OFFSET = TEX_ID_OFFSET + (TEX_ID_SIZE * Float.BYTES);

    private static final int STRIDE = POS_SIZE + COLOR_SIZE + TEX_COORDS_SIZE + TEX_ID_SIZE + NORMAL_SIZE;

    private static final int STRIDE_BYTES = STRIDE * Float.BYTES;

    private static final int DEFAULT_MAX_BATCH_SIZE = 100;

    private static final int[] TEX_SLOTS = { 0, 1, 2, 3, 4, 5, 6, 7 };

    private static final int MAX_TEX_SLOTS = TEX_SLOTS.length;

    private int vaoID;

    private int vboID;

    private int vaoLsID;

    private int vboLsID;

    private Shader shader;

    private Shader shaderLs;

    private Camera camera;

    public float[] vertices;

    public float[] verticesLs;

    private Block[] blocks;

    private int size;

    private int maxBatchSize;

    private boolean hasSpace;

    private List<Texture> textures;

    private Texture normal;

    private Block lightSource;

    public Renderer(Camera camera) {
        this(DEFAULT_MAX_BATCH_SIZE, camera);
    }

    public Renderer(int maxBatchSize, Camera camera) {
        this.vertices = new float[36 * STRIDE * maxBatchSize];
        this.verticesLs = new float[36 * STRIDE];
        this.blocks = new Block[maxBatchSize];
        this.size = 0;
        this.hasSpace = true;
        this.textures = new ArrayList<>(MAX_TEX_SLOTS);
        this.maxBatchSize = maxBatchSize;
        this.camera = camera;
        this.shader = Assets.getShader(
                "C:/users/ralle/dev/java/minecraft/assets/shaders/block.glsl");
        this.shaderLs = Assets.getShader(
                "C:/users/ralle/dev/java/minecraft/assets/shaders/lightsource.glsl");
        this.normal = Assets.getTexture(
                "C:/users/ralle/dev/java/minecraft/assets/images/normal.jpg");
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

        glVertexAttribPointer(2, TEX_COORDS_SIZE, GL_FLOAT, false, STRIDE_BYTES, TEX_COORDS_OFFSET);
        glEnableVertexAttribArray(2);

        glVertexAttribPointer(3, TEX_ID_SIZE, GL_FLOAT, false, STRIDE_BYTES, TEX_ID_OFFSET);
        glEnableVertexAttribArray(3);

        glVertexAttribPointer(4, NORMAL_SIZE, GL_FLOAT, false, STRIDE_BYTES, NORMAL_OFFSET);
        glEnableVertexAttribArray(4);

        startLs();
    }

    private void startLs() {
        vaoLsID = glGenVertexArrays();
        glBindVertexArray(vaoLsID);

        vboLsID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboLsID);
        glBufferData(GL_ARRAY_BUFFER, verticesLs, GL_STATIC_DRAW);

        glVertexAttribPointer(0, POS_SIZE, GL_FLOAT, false, STRIDE_BYTES, POS_OFFSET);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, COLOR_SIZE, GL_FLOAT, false, STRIDE_BYTES, COLOR_OFFSET);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2, TEX_COORDS_SIZE, GL_FLOAT, false, STRIDE_BYTES, TEX_COORDS_OFFSET);
        glEnableVertexAttribArray(2);

        glVertexAttribPointer(3, TEX_ID_SIZE, GL_FLOAT, false, STRIDE_BYTES, TEX_ID_OFFSET);
        glEnableVertexAttribArray(3);

        glVertexAttribPointer(4, NORMAL_SIZE, GL_FLOAT, false, STRIDE_BYTES, NORMAL_OFFSET);
        glEnableVertexAttribArray(4);
    }

    public void destroy() {
        glDeleteBuffers(vboID);
        glDeleteVertexArrays(vaoID);
        glDeleteBuffers(vboLsID);
        glDeleteVertexArrays(vaoLsID);
    }

    public void add(Block block) {
        int index = size;
        blocks[index] = block;
        size++;
        addTexture(block);
        loadVertexAttributes(index);
        if (size >= maxBatchSize) {
            hasSpace = false;
        }
    }

    public void addLs(Block block) {
        loadVertexAttributesLs(block);
        this.lightSource = block;
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

    public boolean rebufferLs() {
        boolean rebuffer = false;
        Block ls = lightSource;
        if (ls.isDirty()) {
            loadVertexAttributesLs(ls);
            ls.setClean();
            rebuffer = true;
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
        shader.uploadVec3f(Shader.U_LIGHT_POS, lightSource.transform.position);
        int i = 0;
        for (Texture texture : textures) {
            glActiveTexture(GL_TEXTURE0 + (i + 1));
            texture.bind();
            i++;
        }
        shader.uploadIntArray(Shader.U_TEXTURES, TEX_SLOTS);

        glBindVertexArray(vaoID);
        glDrawArrays(GL_TRIANGLES, 0, 36 * maxBatchSize);

        for (Texture texture : textures) {
            texture.unbind();
        }
        shader.detach();

        renderLs();
    }

    private void renderLs() {
        if (rebufferLs()) {
            glBindBuffer(GL_ARRAY_BUFFER, vboLsID);
            glBufferSubData(GL_ARRAY_BUFFER, 0, verticesLs);
        }

        shaderLs.use();
        shaderLs.uploadMatrix4f(Shader.U_VIEW, camera.getViewMatrix());
        shaderLs.uploadMatrix4f(Shader.U_PROJECTION, camera.getProjectionMatrix());

        glBindVertexArray(vaoLsID);
        glDrawArrays(GL_TRIANGLES, 0, 36);

        shaderLs.detach();
    }

    private Vector3f[] genVertexArray(Transform transform) {
        Vector3f[] vec = new Vector3f[8];
        vec[0] = new Vector3f(
                transform.position.x + (-0.5f * transform.scale.x),
                transform.position.y + ( 0.5f * transform.scale.y),
                transform.position.z + ( 0.5f * transform.scale.z));
        vec[1] = new Vector3f(
                transform.position.x + (0.5f * transform.scale.x),
                transform.position.y + (0.5f * transform.scale.y),
                transform.position.z + (0.5f * transform.scale.z));
        vec[2] = new Vector3f(
                transform.position.x + (-0.5f * transform.scale.x),
                transform.position.y + (-0.5f * transform.scale.y),
                transform.position.z + ( 0.5f * transform.scale.z));
        vec[3] = new Vector3f(
                transform.position.x + ( 0.5f * transform.scale.x),
                transform.position.y + (-0.5f * transform.scale.y),
                transform.position.z + ( 0.5f * transform.scale.z));
        vec[4] = new Vector3f(
                transform.position.x + (-0.5f * transform.scale.x),
                transform.position.y + ( 0.5f * transform.scale.y),
                transform.position.z + (-0.5f * transform.scale.z));
        vec[5] = new Vector3f(
                transform.position.x + ( 0.5f * transform.scale.x),
                transform.position.y + ( 0.5f * transform.scale.y),
                transform.position.z + (-0.5f * transform.scale.z));
        vec[6] = new Vector3f(
                transform.position.x + (-0.5f * transform.scale.x),
                transform.position.y + (-0.5f * transform.scale.y),
                transform.position.z + (-0.5f * transform.scale.z));
        vec[7] = new Vector3f(
                transform.position.x + ( 0.5f * transform.scale.x),
                transform.position.y + (-0.5f * transform.scale.y),
                transform.position.z + (-0.5f * transform.scale.z));
        return vec;
    }

    public boolean destroyIfExists(Block block) {
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

    public void loadVertexAttributes(int index) {
        Block block = blocks[index];
        Vector4f color = block.getColor();
        Vector3f[] vertexArray = genVertexArray(block.getTransform());
        int offset = index * 36 * STRIDE;

        for (int i = 0; i < Renderer.INDICES.length; i++) {
            int j = Renderer.INDICES[i];
            Vector3f pos = vertexArray[j];
            Vector2f texCoords = block.getTexCoords(i % 6);
            int texId = (block.getTexture() == null) ? 0 : textures.indexOf(block.getTexture()) + 1;
            float[] normals       = NORMALS_2D[i / 6];
            vertices[offset]      = pos.x;
            vertices[offset + 1]  = pos.y;
            vertices[offset + 2]  = pos.z;
            vertices[offset + 3]  = color.x;
            vertices[offset + 4]  = color.y;
            vertices[offset + 5]  = color.z;
            vertices[offset + 6]  = color.w;
            vertices[offset + 7]  = texCoords.x;
            vertices[offset + 8]  = texCoords.y;
            vertices[offset + 9]  = texId;
            vertices[offset + 10] = normals[0];
            vertices[offset + 11] = normals[1];
            vertices[offset + 12] = normals[2];
            offset += STRIDE;
        }
    }

    private void loadVertexAttributesLs(Block block) {
        Vector4f color = block.getColor();
        Vector3f[] vertexArray = genVertexArray(block.getTransform());
        int offset = 0;

        for (int i = 0; i < Renderer.INDICES.length; i++) {
            int j = Renderer.INDICES[i];
            Vector3f pos = vertexArray[j];
            Vector2f texCoords = new Vector2f(0.0f, 0.0f);
            int texId = 0;
            float[] normals         = NORMALS_2D[i / 6];
            verticesLs[offset]      = pos.x;
            verticesLs[offset + 1]  = pos.y;
            verticesLs[offset + 2]  = pos.z;
            verticesLs[offset + 3]  = color.x;
            verticesLs[offset + 4]  = color.y;
            verticesLs[offset + 5]  = color.z;
            verticesLs[offset + 6]  = color.w;
            verticesLs[offset + 7]  = texCoords.x;
            verticesLs[offset + 8]  = texCoords.y;
            verticesLs[offset + 9]  = texId;
            verticesLs[offset + 10] = normals[0];
            verticesLs[offset + 11] = normals[1];
            verticesLs[offset + 12] = normals[2];
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

    public boolean hasTextureSpace() {
        return (textures.size() < MAX_TEX_SLOTS);
    }

    public boolean hasTexture(Texture texture) {
        return textures.contains(texture);
    }

    private void addTexture(Block block) {
        Texture texture = block.getTexture();
        if (texture != null && !hasTexture(texture)) {
            textures.add(texture);
        }
    }
}
