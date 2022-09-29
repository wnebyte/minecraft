package com.github.wnebyte.minecraft.renderer;

import java.util.List;
import java.util.Arrays;
import java.util.Set;
import java.util.ArrayList;
import com.github.wnebyte.minecraft.componenets.Block;
import com.github.wnebyte.minecraft.core.Camera;
import com.github.wnebyte.minecraft.util.Assets;
import com.github.wnebyte.minecraft.util.Sets;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL40.GL_DRAW_INDIRECT_BUFFER;
import static org.lwjgl.opengl.GL43.glMultiDrawArraysIndirect;

public class MultiDraw {

    private static float[] genVertices() {
        float[] vertices = new float[36 * STRIDE];
        int offset = 0;
        for (int i = 0; i < INDICES.length; i++) {
            int index = INDICES[i];
            float[] vertex = VERTICES[index];
            float[] uv = UVS[i % 6];
            vertices[offset + 0] = vertex[0];
            vertices[offset + 1] = vertex[1];
            vertices[offset + 2] = vertex[2];
            vertices[offset + 3] = uv[0];
            vertices[offset + 4] = uv[1];
            vertices[offset + 5] = 0;
            offset += STRIDE;
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

    public static final float[][] VERTICES = {
            { -0.5f,  0.5f,  0.5f },
            {  0.5f,  0.5f,  0.5f } ,
            { -0.5f, -0.5f,  0.5f },
            {  0.5f, -0.5f,  0.5f },
            { -0.5f,  0.5f, -0.5f },
            {  0.5f,  0.5f, -0.5f },
            { -0.5f, -0.5f, -0.5f },
            {  0.5f, -0.5f, -0.5f }
    };

    public static final int[] INDICES = {
            // Each set of 6 indices represents one quad
            1, 0, 2,    3, 1, 2,    // Front face
            5, 1, 3,    7, 5, 3,    // Right face
            7, 6, 4,    5, 7, 4,    // Back face
            0, 4, 6,    2, 0, 6,    // Left face
            5, 4, 0,    1, 5, 0,    // Top face
            3, 2, 6,    7, 3, 6     // Bottom face
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

    public static final int POS_SIZE = 3;

    public static final int UV_SIZE = 2;

    public static final int POS_OFFSET = 0;

    public static final int UV_OFFSET = POS_OFFSET + (POS_SIZE * Float.BYTES);

    public static final int STRIDE = POS_SIZE + UV_SIZE;

    public static final int STRIDE_BYTES = STRIDE * Float.BYTES;

    public static final int NUM_COMMANDS = 2;

    public static final int[] TEX_SLOTS = { 0, 1, 2, 3, 4, 5, 6, 7 };

    private int vao;

    private int vbo;

    private int ibo;

    private Shader shader;

    private Camera camera;

    private List<Texture> textures;

    private float[] vertices;

    private Block[] blocks;

    private Block[][][] data;

    private int size;

    private boolean rebuffer;

    public MultiDraw(Camera camera) {
        this.camera = camera;
        this.shader = Assets.getShader("C:/users/ralle/dev/java/minecraft/assets/shaders/cube.glsl");
        this.textures = Arrays.asList(
                Assets.getTexture("C:/users/ralle/dev/java/minecraft/assets/images/blocks/birch_log.png"),
                Assets.getTexture("C:/users/ralle/dev/java/minecraft/assets/images/blocks/dirt.png"),
                Assets.getTexture("C:/users/ralle/dev/java/minecraft/assets/images/blocks/birch_log_top.png"));
        this.vertices = new float[36 * STRIDE * 4096];
        this.blocks = new Block[4096];
        this.data = new Block[16][16][16];
        this.size = 0;
        this.init();
    }

    private void init() {
        List<Block> tmp = new ArrayList<>();
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                for (int z = 0; z < 8; z++) {
                    Block block = new Block(1, x * 1.2f, y * 1.2f, z * 1.2f);
                    data[x][y][z] = block;
                    tmp.add(block);
                }
            }
        }
        for (Block block : tmp) {
            add(block);
        }
    }

    public void start() {
        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        glVertexAttribPointer(0, POS_SIZE, GL_FLOAT, false, STRIDE_BYTES, POS_OFFSET);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, UV_SIZE, GL_FLOAT, false, STRIDE_BYTES, UV_OFFSET);
        glEnableVertexAttribArray(1);

        int index = 0;
        int[] vDrawCommands = new int[4 * size];
        for (int i = 0; i < size; i++) {
            vDrawCommands[index + 0] = 36;     // vertexCount
            vDrawCommands[index + 1] = 1;      // instanceCount (how many copies of the geometry we should draw)
            vDrawCommands[index + 2] = i * 36; // first (0)
            vDrawCommands[index + 3] = i;      // baseInstance
            index += 4;
        }

        ibo = glGenBuffers();
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, ibo);
        glBufferData(GL_DRAW_INDIRECT_BUFFER, vDrawCommands, GL_STATIC_DRAW);
    }

    public void add(Block block) {
        int index = size;
        blocks[index] = block;
        size++;
        loadVertexAttributes(index);
        rebuffer = true;
    }

    public void render() {
        if (rebuffer) {
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);
            rebuffer = false;
        }
        shader.use();
        shader.uploadMatrix4f(Shader.U_PROJECTION, camera.getProjectionMatrix());
        shader.uploadMatrix4f(Shader.U_VIEW, camera.getViewMatrix());
        for (int i = 0; i < textures.size(); i++) {
            Texture texture = textures.get(i);
            glActiveTexture(GL_TEXTURE0 + i);
            texture.bind();
        }
        shader.uploadIntArray(Shader.U_TEXTURES, TEX_SLOTS);
        // param mode specifies what kind of primitive to render
        // param indirect is either an offset, in bytes, into the buffer bound to
        // GL_DRAW_INDIRECT_BUFFER or a pointer to an array struct that holds draw parameters
        // param drawCount the number of elements in the array addresses by indirect
        // param stride is the distance, in bytes, between the elements of the indirect array
        glMultiDrawArraysIndirect(GL_TRIANGLES, 0, size, 0);
        for (Texture texture : textures) {
            texture.unbind();
        }
        shader.detach();
    }

    public static final Set<Integer> FRONT_FACE   = Sets.of(INDICES, 0, 6);

    public static final Set<Integer> RIGHT_FACE   = Sets.of(INDICES, 6, 12);

    public static final Set<Integer> BACK_FACE    = Sets.of(INDICES, 12, 18);

    public static final Set<Integer> LEFT_FACE    = Sets.of(INDICES, 18, 24);

    public static final Set<Integer> TOP_FACE     = Sets.of(INDICES, 24, 30);

    public static final Set<Integer> BOTTOM_FACE = Sets.of(INDICES, 30, 36);

    public void loadVertexAttributes(int index) {
        Block block = blocks[index];
        int offset = index * 36 * STRIDE;
        //Vector3i v = Chunk.to3D(index);

        for (int i : INDICES) {
            float[] verts = VERTICES[i];
            float[] uv = UVS[i % 6];
            /*
            if ((isFrontFace(verts, i)      && !Chunk.isAir(data, v.x, v.y, v.z - 1)) ||
                    (isRightFace(verts, i)  && !Chunk.isAir(data, v.x + 1, v.y, v.z)) ||
                    (isBackFace(verts, i)   && !Chunk.isAir(data, v.x, v.y, v.z + 1)) ||
                    (isLeftFace(verts, i)   && !Chunk.isAir(data, v.x - 1, v.y, v.z)) ||
                    (isTopFace(verts, i)    && !Chunk.isAir(data, v.x, v.y + 1, v.z)) ||
                    (isBottomFace(verts, i) && !Chunk.isAir(data, v.x, v.y - 1, v.z))) {
                continue;
            }
             */
            vertices[offset + 0] = block.x + (verts[0] * 1.0f);
            vertices[offset + 1] = block.y + (verts[1] * 1.0f);
            vertices[offset + 2] = block.z + (verts[2] * 1.0f);
            vertices[offset + 3] = uv[0];
            vertices[offset + 4] = uv[1];
            offset += STRIDE;
        }
    }

    public boolean isFrontFace(float[] verts, int index) {
        return (Arrays.equals(VERTICES[0], verts) && FRONT_FACE.contains(index));
    }

    public boolean isRightFace(float[] verts, int index) {
        return (Arrays.equals(VERTICES[1], verts) && RIGHT_FACE.contains(index));
    }

    public boolean isBackFace(float[] verts, int index) {
        return (Arrays.equals(VERTICES[2], verts) && BACK_FACE.contains(index));
    }

    public boolean isLeftFace(float[] verts, int index) {
        return (Arrays.equals(VERTICES[3], verts) && LEFT_FACE.contains(index));
    }

    public boolean isTopFace(float[] verts, int index) {
        return (Arrays.equals(VERTICES[4], verts) && TOP_FACE.contains(index));
    }

    public boolean isBottomFace(float[] verts, int index) {
        return (Arrays.equals(VERTICES[5], verts) && BOTTOM_FACE.contains(index));
    }

}
