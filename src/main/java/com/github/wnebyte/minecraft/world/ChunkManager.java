package com.github.wnebyte.minecraft.world;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.Future;
import java.nio.ByteBuffer;
import org.joml.Vector2i;
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

    private int vaoID;

    private int vboID;

    private int iboID;

    private int biboID;

    private int cboID;

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
        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);

        glVertexAttribIPointer(0, DATA_SIZE, GL_UNSIGNED_INT, STRIDE_BYTES, DATA_OFFSET);
        glEnableVertexAttribArray(0);

        int size = subchunks.capacity() * (VERTEX_CAPACITY * STRIDE_BYTES);
        int length = size / subchunks.capacity();
        int flags = GL_MAP_PERSISTENT_BIT | GL_MAP_WRITE_BIT | GL_MAP_COHERENT_BIT;
        glBufferStorage(GL_ARRAY_BUFFER, size, flags);
        ByteBuffer buffer = glMapBufferRange(GL_ARRAY_BUFFER, 0, size, flags);
        long base = MemoryUtil.memAddress(buffer);
        for (int offset = 0, i = 0; offset <= size - length && i < subchunks.capacity(); offset += length, i++) {
            ByteBuffer slice = MemoryUtil.memByteBuffer(base + offset, length);
            Subchunk subchunk = new Subchunk(new VertexBuffer(slice));
            subchunk.setFirst(i * VERTEX_CAPACITY);
            subchunks.add(subchunk);
        }
        DebugStats.vertexMemAlloc = size;

        iboID = glGenBuffers();
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, iboID);
        glBufferData(GL_DRAW_INDIRECT_BUFFER,
                (long)drawCommands.capacity() * DrawCommand.SIZE_BYTES, GL_DYNAMIC_DRAW);

        biboID = glGenBuffers();
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, biboID);
        glBufferData(GL_DRAW_INDIRECT_BUFFER,
                (long) transparentDrawCommands.capacity() * DrawCommand.SIZE_BYTES, GL_DYNAMIC_DRAW);

        cboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, cboID);
        glBufferData(GL_ARRAY_BUFFER,
                (long)drawCommands.capacity() * (2 * Integer.BYTES), GL_DYNAMIC_DRAW);

        glVertexAttribIPointer(1, 2, GL_INT, 2 * Integer.BYTES, 0);
        glVertexAttribDivisor(1, 1);
        glEnableVertexAttribArray(1);
    }

    public void loadSpawnChunks() {
        long startTime = System.nanoTime();
        int sqrt = (int)Math.sqrt(World.SPAWN_CHUNK_SIZE);
        for (int x = 0; x < sqrt; x++) {
            for (int z = 0; z < sqrt; z++) {
                Chunk chunk = new Chunk(x, 0, z, map, subchunks);
                map.putChunk(chunk);
                chunk.load();
            }
        }
        for (Chunk chunk : map) {
            chunk.mesh();
        }
        double time = (System.nanoTime() - startTime) * 1E-9;
        System.out.printf("%dx%d chunks initialized in: %.2fs%n",  sqrt, sqrt, time);
        System.out.printf("Vertex mem used:             %.2fMB%n", DebugStats.vertexMemUsed  * 1E-6);
        System.out.printf("Vertex mem allocated:        %.2fMB%n", DebugStats.vertexMemAlloc * 1E-6);
        System.out.printf("Number of subchunks:         %d%n",     subchunks.capacity());
    }

    /*
    Step 1: Deserialize all chunks
    Step 2: Retrieve all neighbours
    Step 3: Mesh all newly loaded chunks and their neighbours
     */
    public void loadChunksAsync(Set<Vector2i> set) {
        Application.submit(() -> {
            Set<Chunk> chunks = new HashSet<>();
            List<Future<?>> futures = new ArrayList<>();
            Future<?> future;
            for (Vector2i ivec2 : set) {
                if (!map.contains(ivec2)) {
                    Chunk chunk = new Chunk(ivec2.x, 0, ivec2.y, map, subchunks);
                    map.putChunk(chunk);
                    future = chunk.loadAsync();
                    futures.add(future);
                    chunk.updateNeighbourRefs();
                    chunks.add(chunk);
                    for (Chunk c : chunk.getNeighbours()) {
                        if (c != null) {
                            chunks.add(c);
                        }
                    }
                }
            }
            if (!futures.isEmpty()) {
                try {
                    for (Future<?> it : futures) it.get();
                    for (Chunk c : chunks) {
                        assert c.isLoaded() : "Chunk is not loaded";
                        c.meshAsync();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /*
    Step 1: Serialize and unmesh all chunks that need to be unloaded
    Step 2: When Step 1 has completed; mesh all neighbours
     */
    // Todo: it's not necessary to wait for step 1 to complete in order to start step 2.
    public void unloadChunksAsync(Set<Chunk> chunks) {
        Application.submit(() -> {
            Set<Chunk> neighbours = new HashSet<>();
            List<Future<?>> futures = new ArrayList<>();
            Future<?> future;
            for (Chunk chunk : chunks) {
                map.removeChunk(chunk);
                future = chunk.unloadAsync();
                futures.add(future);
                for (Chunk c : chunk.getNeighbours()) {
                    if (c != null) {
                        neighbours.add(c);
                    }
                }
            }
            if (!futures.isEmpty()) {
                try {
                    for (Future<?> it : futures) it.get();
                    for (Chunk c : neighbours) {
                        if (c.isLoaded()) {
                            c.meshAsync();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void generateDrawCommands() {
        for (Subchunk subchunk : subchunks) {
            if (subchunk.getState() == Subchunk.State.MESHED) {
                float yMin = subchunk.getSubchunkLevel() * 16;
                Vector3f min = Chunk.toWorldCoords(subchunk.getChunkCoords(), yMin)
                        .sub(0.5f, 0.5f, 0.5f);
                Vector3f max = new Vector3f(min)
                        .add(new Vector3f(16.0f, 16.0f, 16.0f));
                if (camera.getFrustrum().isBoxVisisble(min, max)) {
                    if (subchunk.isBlendable()) {
                        transparentDrawCommands.add(subchunk);
                    } else {
                        drawCommands.add(subchunk);
                    }
                }
            }
        }
    }

    public void render() {
        generateDrawCommands();

        // Render pass 1:
        // set opqaue render states
       // glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS);
        glDepthMask(true);
        glDisable(GL_BLEND);

        // draw opaque geometry
        glBindBuffer(GL_ARRAY_BUFFER, cboID);
        glBufferSubData(GL_ARRAY_BUFFER, 0, drawCommands.getChunkCoords());
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, iboID);
        glBufferSubData(GL_DRAW_INDIRECT_BUFFER, 0, drawCommands.getDrawCommands());
        shader.use();
        shader.uploadMatrix4f(Shader.U_VIEW, camera.getViewMatrix());
        shader.uploadMatrix4f(Shader.U_PROJECTION, camera.getProjectionMatrix());
        glActiveTexture(GL_TEXTURE0);
        texture.bind();
        shader.uploadTexture(Shader.U_TEXTURE, 0);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_BUFFER, BlockMap.getTexCoordsTextureId());
        shader.uploadTexture(Shader.U_TEX_COORDS_TEXTURE, 1);
        glBindVertexArray(vaoID);
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

        glDrawBuffers(Constants.BUFS_NONE_ONE_TWO);
        glClearBufferfv(GL_COLOR, 1, Constants.ZERO_FILLER_VEC);
        glClearBufferfv(GL_COLOR, 2, Constants.ONE_FILLER_VEC);

        // draw transparent geometry
        glBindBuffer(GL_ARRAY_BUFFER, cboID);
        glBufferSubData(GL_ARRAY_BUFFER, 0, transparentDrawCommands.getChunkCoords());
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, biboID);
        glBufferSubData(GL_DRAW_INDIRECT_BUFFER, 0, transparentDrawCommands.getDrawCommands());
        transparentShader.use();
        transparentShader.uploadMatrix4f(Shader.U_VIEW, camera.getViewMatrix());
        transparentShader.uploadMatrix4f(Shader.U_PROJECTION, camera.getProjectionMatrix());
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

        glDrawBuffers(Constants.BUFS_ZERO_NONE_NONE);

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

        drawCommands.reset();
        transparentDrawCommands.reset();
    }

    public void destroy() {
        glDeleteVertexArrays(0);
        glUnmapBuffer(vboID);
        glDeleteBuffers(vboID);
        glDeleteBuffers(iboID);
        glDeleteBuffers(cboID);
        glDeleteBuffers(biboID);
    }

    public Map getMap() {
        return map;
    }
}
