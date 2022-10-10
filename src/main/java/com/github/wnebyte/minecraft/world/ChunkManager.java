package com.github.wnebyte.minecraft.world;

import java.nio.ByteBuffer;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;
import com.github.wnebyte.minecraft.core.Application;
import com.github.wnebyte.minecraft.core.Camera;
import com.github.wnebyte.minecraft.renderer.*;
import com.github.wnebyte.minecraft.util.*;
import static com.github.wnebyte.minecraft.renderer.VertexBuffer.*;
import static org.lwjgl.opengl.GL11.GL_INT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL30.glVertexAttribIPointer;
import static org.lwjgl.opengl.GL31.GL_TEXTURE_BUFFER;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;
import static org.lwjgl.opengl.GL40.GL_DRAW_INDIRECT_BUFFER;
import static org.lwjgl.opengl.GL40C.glBlendFunci;
import static org.lwjgl.opengl.GL43.glMultiDrawArraysIndirect;
import static org.lwjgl.opengl.GL44.glBufferStorage;
import static org.lwjgl.opengl.GL44C.GL_MAP_COHERENT_BIT;
import static org.lwjgl.opengl.GL44C.GL_MAP_PERSISTENT_BIT;

public class ChunkManager {

    private int vao;

    private int vbo;

    private int ibo;

    private int bibo;

    private int cbo;

    private Camera camera;

    private Shader shader;

    private Shader transparentShader;

    private Shader compositeShader;

    private Texture texture;

    private DrawCommandBuffer drawCommands;

    private DrawCommandBuffer transparentDrawCommands;

    private Pool<Key, Subchunk> subchunks;

    private Map map;

    public ChunkManager(Camera camera, Map map) {
        this.camera = camera;
        this.shader = Assets.getShader(Assets.DIR + "/shaders/opaque.glsl");
        this.transparentShader = Assets.getShader(Assets.DIR + "/shaders/transparent.glsl");
        this.compositeShader = Assets.getShader(Assets.DIR + "/shaders/composite.glsl");
        this.texture = Assets.getTexture(Assets.DIR + "/images/generated/packedTextures.png");
        this.drawCommands = new DrawCommandBuffer(World.CHUNK_CAPACITY * 16);
        this.transparentDrawCommands = new DrawCommandBuffer(World.CHUNK_CAPACITY * 16);
        this.subchunks = new Pool<>(2 * World.CHUNK_CAPACITY * 16);
        this.map = map;
    }

    public void start() {
        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        glVertexAttribIPointer(0, DATA_SIZE, GL_UNSIGNED_INT, STRIDE_BYTES, DATA_OFFSET);
        glEnableVertexAttribArray(0);

        int numBuffers = subchunks.size();
        int size = numBuffers * (VERTEX_CAPACITY * STRIDE_BYTES);
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
        DebugStats.vertexMemAlloc = size;

        ibo = glGenBuffers();
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, ibo);
        glBufferData(GL_DRAW_INDIRECT_BUFFER,
                (long)drawCommands.capacity() * DrawCommand.SIZE_BYTES, GL_DYNAMIC_DRAW);

        bibo = glGenBuffers();
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, bibo);
        glBufferData(GL_DRAW_INDIRECT_BUFFER,
                (long) transparentDrawCommands.capacity() * DrawCommand.SIZE_BYTES, GL_DYNAMIC_DRAW);

        cbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, cbo);
        glBufferData(GL_ARRAY_BUFFER,
                (long)(subchunks.size() / 2) * (2 * Integer.BYTES), GL_DYNAMIC_DRAW);

        glVertexAttribIPointer(1, 2, GL_INT, 2 * Integer.BYTES, 0);
        glVertexAttribDivisor(1, 1);
        glEnableVertexAttribArray(1);

        initMap();
        int x = (int)(Math.sqrt(World.SPAWN_CHUNK_SIZE) * 16) / 2;
        int y = 51;
        int z = x;
    }

    private void initMap() {
        long startTime = System.nanoTime();
        int sqrt = (int)Math.sqrt(World.SPAWN_CHUNK_SIZE);
        for (int x = 0; x < sqrt; x++) {
            for (int z = 0; z < sqrt; z++) {
                Chunk chunk = new Chunk(x, 0, z, map, drawCommands, transparentDrawCommands, subchunks);
                map.put(chunk.getChunkCoords(), chunk);
                chunk.generateTerrain();
            }
        }
        for (Chunk chunk : map) {
            chunk.updateNeighbourRefs();
            chunk.generateMesh();
        }
        double time = (System.nanoTime() - startTime) * 1E-9;
        System.out.printf("%dx%d chunks initialized in: %.2fs%n",  sqrt, sqrt, time);
        System.out.printf("Vertex mem used:             %.2fMB%n", DebugStats.vertexMemUsed  * 1E-6);
        System.out.printf("Vertex mem allocated:        %.2fMB%n", DebugStats.vertexMemAlloc * 1E-6);
        System.out.printf("Number of subchunks:         %d%n",     subchunks.size());
    }

    public void render() {
        // Render pass 1:
        // set opqaue render states
       // glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS);
        glDepthMask(true);
        glDisable(GL_BLEND);

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
        glBindVertexArray(vao);
        glMultiDrawArraysIndirect(GL_TRIANGLES, 0, drawCommands.size(), 0);
        texture.unbind();
        shader.detach();

        // Render pass 2:
        // set transparent render states
       // glDisable(GL_CULL_FACE);
        glDepthMask(false);
        glEnable(GL_BLEND);
        glBlendFunci(1, GL_ONE, GL_ONE); // accumulation blend target
        glBlendFunci(2, GL_ZERO, GL_ONE_MINUS_SRC_COLOR); // revealage blend target
        glBlendEquation(GL_FUNC_ADD);

        int[] bufs = { GL_NONE, GL_COLOR_ATTACHMENT1, GL_COLOR_ATTACHMENT2 };
        glDrawBuffers(bufs);
        glClearBufferfv(GL_COLOR, 1, Application.ZERO_FILLER_VEC);
        glClearBufferfv(GL_COLOR, 2, Application.ONE_FILLER_VEC);

        // draw transparent geometry
        glBindBuffer(GL_ARRAY_BUFFER, cbo);
        glBufferSubData(GL_ARRAY_BUFFER, 0, transparentDrawCommands.chunkCoords());
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, bibo);
        glBufferSubData(GL_DRAW_INDIRECT_BUFFER, 0, transparentDrawCommands.data());
        transparentShader.use();
        transparentShader.uploadMatrix4f(Shader.U_PROJECTION, camera.getProjectionMatrix());
        transparentShader.uploadMatrix4f(Shader.U_VIEW, camera.getViewMatrix());
        glActiveTexture(GL_TEXTURE0);
        texture.bind();
        transparentShader.uploadTexture(Shader.U_TEXTURE, 0);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_BUFFER, BlockMap.getTexCoordsTextureId());
        transparentShader.uploadTexture(Shader.U_TEX_COORDS_TEXTURE, 1);
        glMultiDrawArraysIndirect(GL_TRIANGLES, 0, transparentDrawCommands.size(), 0);
        texture.unbind();
        transparentShader.detach();

        // Render pass 3:
        // set composite render states
        glDepthFunc(GL_ALWAYS);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        bufs = new int[]{ GL_COLOR_ATTACHMENT0, GL_NONE, GL_NONE };
        glDrawBuffers(bufs);

        // draw screen quad
        Framebuffer framebuffer = Application.getFramebuffer();
        compositeShader.use();
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, framebuffer.getColorAttachment(1).getId());
        compositeShader.uploadTexture(Shader.ACCUM, 0);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, framebuffer.getColorAttachment(2).getId());
        compositeShader.uploadTexture(Shader.REVEAL, 1);
        ScreenRenderer.render();
        compositeShader.detach();
    }

    public void destroy() {
        glDeleteVertexArrays(0);
        glUnmapBuffer(vbo);
        glDeleteBuffers(vbo);
        glDeleteBuffers(ibo);
        glDeleteBuffers(cbo);
        glDeleteBuffers(bibo);
    }

    public Map getMap() {
        return map;
    }
}
