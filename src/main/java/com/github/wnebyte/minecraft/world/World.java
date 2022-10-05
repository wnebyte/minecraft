package com.github.wnebyte.minecraft.world;

import java.nio.ByteBuffer;
import java.util.Random;

import com.github.wnebyte.minecraft.core.Window;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.system.MemoryUtil;
import com.github.wnebyte.minecraft.core.Camera;
import com.github.wnebyte.minecraft.renderer.*;
import com.github.wnebyte.minecraft.util.*;
import static com.github.wnebyte.minecraft.renderer.VertexBuffer.*;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.GL_TEXTURE_BUFFER;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;
import static org.lwjgl.opengl.GL40.GL_DRAW_INDIRECT_BUFFER;
import static org.lwjgl.opengl.GL40C.glBlendFunci;
import static org.lwjgl.opengl.GL43.glMultiDrawArraysIndirect;
import static org.lwjgl.opengl.GL44.glBufferStorage;
import static org.lwjgl.opengl.GL44C.GL_MAP_COHERENT_BIT;
import static org.lwjgl.opengl.GL44C.GL_MAP_PERSISTENT_BIT;

public class World {

    public static final int CHUNK_CAPACITY = 50;

    public static final int SPAWN_CHUNK_SIZE = 5 * 5;

    public static final float[] quadVertices = {
            -1.0f, -1.0f,  0.0f,  0.0f,  0.0f,
             1.0f, -1.0f,  0.0f,  1.0f,  0.0f,
             1.0f,  1.0f,  0.0f,  1.0f,  1.0f,
             1.0f,  1.0f,  0.0f,  1.0f,  1.0f,
            -1.0f,  1.0f,  0.0f,  0.0f,  1.0f,
            -1.0f, -1.0f,  0.0f,  0.0f,  0.0f
    };

    public static final int POS_SIZE = 3;

    public static final int UV_SIZE = 2;

    public static final int POS_OFFSET = 0;

    public static final int UV_OFFSET = POS_OFFSET + (POS_SIZE * Float.BYTES);

    public static final int STRIDE = POS_SIZE + UV_SIZE;

    public static final int STRIDE_BYTES = STRIDE * Float.BYTES;

    private int opaqueFbo;

    private int transparentFbo;

    private int opaqueTexture;

    private int depthTexture;

    private int accumTexture;

    private int revealTexture;

    private int[] transparentDrawBuffers;

    private float[] zeroFillerVec = { 0.0f, 0.0f, 0.0f, 0.0f };

    private float[] oneFillerVec = { 1.0f, 1.0f, 1.0f, 1.0f };

    private int vao;

    private int vbo;

    private int ibo;

    private int bibo;

    private int cbo;

    private int bcbo;

    private int quadVao;

    private int quadVbo;

    private Camera camera;

    private Shader shader;

    private Shader blendableShader;

    private Shader compositeShader;

    private Shader screenShader;

    private Texture texture;

    private DrawCommandBuffer drawCommands;

    private DrawCommandBuffer blendableDrawCommands;

    private Pool<Key, Subchunk> subchunks;

    private Map map;

    private Random rand;

    private float debounceTime = 0.2f;

    private float debounce = debounceTime;

    public World(Camera camera) {
        this.camera = camera;
        this.shader = Assets.getShader(Assets.DIR + "/shaders/opaque.glsl");
        this.blendableShader = Assets.getShader(Assets.DIR + "/shaders/transparent.glsl");
        this.compositeShader = Assets.getShader(Assets.DIR + "/shaders/composite.glsl");
        this.screenShader = Assets.getShader(Assets.DIR + "/shaders/screen.glsl");
        this.texture = Assets.getTexture(Assets.DIR + "/images/generated/packedTextures.png");
        this.drawCommands = new DrawCommandBuffer(CHUNK_CAPACITY * 16);
        this.blendableDrawCommands = new DrawCommandBuffer(CHUNK_CAPACITY * 16);
        this.subchunks = new Pool<>(2 * CHUNK_CAPACITY * 16);
        this.map = new Map();
        this.rand = new Random();
    }

    public void start() {
        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        glVertexAttribIPointer(0, DATA_SIZE, GL_UNSIGNED_INT, VertexBuffer.STRIDE_BYTES, DATA_OFFSET);
        glEnableVertexAttribArray(0);

        int numBuffers = subchunks.size();
        int size = numBuffers * (VERTEX_CAPACITY * VertexBuffer.STRIDE_BYTES);
        int length = size / numBuffers;
        int flags = GL_MAP_PERSISTENT_BIT | GL_MAP_WRITE_BIT | GL_MAP_COHERENT_BIT;
        glBufferStorage(GL_ARRAY_BUFFER, size, flags);
        ByteBuffer buffer = glMapBufferRange(GL_ARRAY_BUFFER, 0, size, flags);
        long base = MemoryUtil.memAddress(buffer);

        for (int offset = 0, i = 0; offset <= size - length && i < subchunks.size(); offset += length, i++) {
            ByteBuffer slice = MemoryUtil.memByteBuffer(base + offset, length);
            Subchunk subchunk = new Subchunk(new VertexBuffer(slice));
            subchunk.first = (i * VERTEX_CAPACITY);
            subchunk.drawCommandIndex = i;
            subchunks.add(subchunk);
        }

        ibo = glGenBuffers();
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, ibo);
        glBufferData(GL_DRAW_INDIRECT_BUFFER, (long)drawCommands.capacity() * DrawCommand.SIZE_BYTES, GL_DYNAMIC_DRAW);

        bibo = glGenBuffers();
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, bibo);
        glBufferData(GL_DRAW_INDIRECT_BUFFER, (long)blendableDrawCommands.capacity() * DrawCommand.SIZE_BYTES, GL_DYNAMIC_DRAW);

        cbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, cbo);
        glBufferData(GL_ARRAY_BUFFER, (long)(subchunks.size() / 2) * (2 * Integer.BYTES), GL_DYNAMIC_DRAW);

        glVertexAttribIPointer(1, 2, GL_INT, 2 * Integer.BYTES, 0);
        glVertexAttribDivisor(1, 1);
        glEnableVertexAttribArray(1);

        quadVao = glGenVertexArrays();
        glBindVertexArray(quadVao);

        quadVbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, quadVbo);
        glBufferData(GL_ARRAY_BUFFER, quadVertices, GL_STATIC_DRAW);

        glVertexAttribPointer(0, POS_SIZE, GL_FLOAT, false, STRIDE_BYTES, POS_OFFSET);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, UV_SIZE, GL_FLOAT, false, STRIDE_BYTES, UV_OFFSET);
        glEnableVertexAttribArray(1);

        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        // setup framebuffers
        opaqueFbo = glGenFramebuffers();
        transparentFbo = glGenFramebuffers();

        // set up attachments for opaque framebuffer
        opaqueTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, opaqueTexture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA16F, Window.getWidth(), Window.getHeight(),
                0, GL_RGBA, GL_HALF_FLOAT, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glBindTexture(GL_TEXTURE_2D, 0);

        depthTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, depthTexture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, Window.getWidth(), Window.getHeight(),
                0, GL_DEPTH_COMPONENT, GL_FLOAT, 0);
        glBindTexture(GL_TEXTURE_2D, 0);

        glBindFramebuffer(GL_FRAMEBUFFER, opaqueFbo);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, opaqueTexture, 0);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depthTexture, 0);

        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
            System.err.println("ERROR::FRAMEBUFFER:: Opaque framebuffer is not complete!");

        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        // set up attachments for transparent framebuffer
        accumTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, accumTexture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA16F, Window.getWidth(), Window.getHeight(),
                0, GL_RGBA, GL_HALF_FLOAT, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glBindTexture(GL_TEXTURE_2D, 0);

        revealTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, revealTexture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_R8, Window.getWidth(), Window.getHeight(),
                0, GL_RED, GL_FLOAT, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glBindTexture(GL_TEXTURE_2D, 0);

        glBindFramebuffer(GL_FRAMEBUFFER, transparentFbo);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, accumTexture, 0);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, GL_TEXTURE_2D, revealTexture, 0);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depthTexture, 0); // opaque framebuffer's depth texture

        transparentDrawBuffers = new int[]{ GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1 };
        glDrawBuffers(transparentDrawBuffers);

        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
            System.err.println("ERROR::FRAMEBUFFER:: Transparent framebuffer is not complete!");

        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        initMap();
        int x = (int)(Math.sqrt(SPAWN_CHUNK_SIZE) * 16) / 2;
        int y = 51;
        int z = x;
        camera.setPosition(new Vector3f(x, y, z));
    }

    private void initMapTest() {
        for (int i = 0; i < CHUNK_CAPACITY; i++) {
            Chunk chunk = new Chunk(i, 0, 0, map, drawCommands, blendableDrawCommands, subchunks);
            map.put(chunk.getChunkCoords(), chunk);
            chunk.generateTerrain();
            chunk.generateMesh();
        }
    }

    private void initMap() {
        long startTime = System.nanoTime();
        int sqrt = (int)Math.sqrt(SPAWN_CHUNK_SIZE);
        for (int x = 0; x < sqrt; x++) {
            for (int z = 0; z < sqrt; z++) {
                Chunk chunk = new Chunk(x, 0, z, map, drawCommands, blendableDrawCommands, subchunks);
                map.put(chunk.getChunkCoords(), chunk);
                chunk.generateTerrain();
                chunk.generateMesh();
            }
        }
        double time = (System.nanoTime() - startTime) * 1E-9;
        System.out.printf("init map: %.2fs%n", time);
    }

    public void update(float dt) {
        debounce -= dt;
    }

    public void render() {
        glBindVertexArray(vao);
        // Render pass 1:
        // set opqaue render states
        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS);
        glDepthMask(true);
        glDisable(GL_BLEND);
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        // bind opaque framebuffer
        glBindFramebuffer(GL_FRAMEBUFFER, opaqueFbo);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        // draw opaque geometry
        glBindBuffer(GL_ARRAY_BUFFER, cbo);
        glBufferSubData(GL_ARRAY_BUFFER, 0, drawCommands.chunkCoords());
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, ibo);
        glBufferSubData(GL_DRAW_INDIRECT_BUFFER, 0, drawCommands.data());
        shader.use();
        shader.uploadMatrix4f(Shader.U_PROJECTION, camera.getProjectionMatrix());
        shader.uploadMatrix4f(Shader.U_VIEW, camera.getViewMatrix());
        glActiveTexture(GL_TEXTURE0);
        texture.bind();
        shader.uploadTexture(Shader.U_TEXTURE, 0);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_BUFFER, BlockMap.getTexCoordsTextureId());
        shader.uploadTexture(Shader.U_TEX_COORDS_TEXTURE, 1);
        glMultiDrawArraysIndirect(GL_TRIANGLES, 0, drawCommands.size(), 0);
        texture.unbind();
        shader.detach();

        // Render pass 2:
        // set transparent render states
        glDisable(GL_CULL_FACE);
        glDepthMask(false);
        glEnable(GL_BLEND);
        glBlendFunci(0, GL_ONE, GL_ONE); // accumulation blend target
        glBlendFunci(1, GL_ZERO, GL_ONE_MINUS_SRC_COLOR); // revealage blend target
        glBlendEquation(GL_FUNC_ADD);

        // bind transparent framebuffer
        glBindFramebuffer(GL_FRAMEBUFFER, transparentFbo);
        glClearBufferfv(GL_COLOR, 0, zeroFillerVec);
        glClearBufferfv(GL_COLOR, 1, oneFillerVec);

        // draw transparent geometry
        glBindBuffer(GL_ARRAY_BUFFER, cbo);
        glBufferSubData(GL_ARRAY_BUFFER, 0, blendableDrawCommands.chunkCoords());
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, bibo);
        glBufferSubData(GL_DRAW_INDIRECT_BUFFER, 0, blendableDrawCommands.data());
        blendableShader.use();
        blendableShader.uploadMatrix4f(Shader.U_PROJECTION, camera.getProjectionMatrix());
        blendableShader.uploadMatrix4f(Shader.U_VIEW, camera.getViewMatrix());
        glActiveTexture(GL_TEXTURE0);
        texture.bind();
        blendableShader.uploadTexture(Shader.U_TEXTURE, 0);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_BUFFER, BlockMap.getTexCoordsTextureId());
        blendableShader.uploadTexture(Shader.U_TEX_COORDS_TEXTURE, 1);
        glMultiDrawArraysIndirect(GL_TRIANGLES, 0, blendableDrawCommands.size(), 0);
        texture.unbind();
        blendableShader.detach();

        // Render pass 3:
        // set composite render states
        glDepthFunc(GL_ALWAYS);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // bind opaque framebuffer
        glBindFramebuffer(GL_FRAMEBUFFER, opaqueFbo);

        compositeShader.use();

        // draw screen quad
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, accumTexture);
        compositeShader.uploadTexture("accum", 0);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, revealTexture);
        compositeShader.uploadTexture("reveal", 1);
        glBindVertexArray(quadVao);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        compositeShader.detach();

        // Render pass 4:
        // set backbuffer render states
        glDisable(GL_DEPTH_TEST);
        glDepthMask(true); // enable depth writes so glClear won't ignore clearing the depth buffer
        glDisable(GL_BLEND);

        // bind backbuffer
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

        screenShader.use();

        // draw final screen quad
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, opaqueTexture);
        screenShader.uploadTexture("screen", 0);
        glBindVertexArray(quadVao);
        glDrawArrays(GL_TRIANGLES, 0, 6);

        screenShader.detach();
        glBindVertexArray(0);
    }

    public void destroy() {
        glDeleteVertexArrays(0);
        glUnmapBuffer(vbo);
        glDeleteBuffers(vbo);
        glDeleteBuffers(ibo);
        glDeleteBuffers(cbo);
        glDeleteBuffers(bibo);
        glDeleteBuffers(bcbo);
    }
}
