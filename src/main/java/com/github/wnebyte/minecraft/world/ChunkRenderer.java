package com.github.wnebyte.minecraft.world;

import java.nio.ByteBuffer;
import org.joml.Vector3i;
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

public class ChunkRenderer {

    private int vaoID;

    private int vboID;

    private int iboID;

    private int tiboID;

    private int biboID;

    private int cboID;

    private final Camera camera;

    private final Shader shader;

    private final Shader transparentShader;

    private final Shader blendableShader;

    private final Shader compositeShader;

    private final Texture texture;

    private final IDrawCommandBuffer drawCommands;

    private final IDrawCommandBuffer transparentDrawCommands;

    private final IDrawCommandBuffer blendableDrawCommands;

    private final Pool<Vector3i, Subchunk> subchunks;

    public ChunkRenderer(Camera camera, Pool<Vector3i, Subchunk> subchunks) {
        this.camera = camera;
        this.shader = Assets.getShader(Assets.DIR + "/shaders/opaque.glsl");
        this.transparentShader = shader;
        this.blendableShader = Assets.getShader(Assets.DIR + "/shaders/transparent.glsl");
        this.compositeShader = Assets.getShader(Assets.DIR + "/shaders/composite.glsl");
        this.texture = Assets.getTexture(Assets.DIR + "/images/generated/packedTextures.png");
        this.drawCommands = new FlatDrawCommandBuffer(World.CHUNK_CAPACITY * 16);
        this.transparentDrawCommands = new FlatDrawCommandBuffer(World.CHUNK_CAPACITY * 16);
        this.blendableDrawCommands = new FlatDrawCommandBuffer(World.CHUNK_CAPACITY * 16);
        this.subchunks = subchunks;
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
            Subchunk subchunk = new Subchunk(new VertexBuffer(slice), i * VERTEX_CAPACITY);
            subchunks.add(subchunk);
        }
        DebugStats.vertexMemAlloc = size;

        iboID = glGenBuffers();
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, iboID);
        glBufferData(GL_DRAW_INDIRECT_BUFFER,
                (long)drawCommands.capacity() * DrawCommand.STRIDE_BYTES, GL_DYNAMIC_DRAW);

        tiboID = glGenBuffers();
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, tiboID);
        glBufferData(GL_DRAW_INDIRECT_BUFFER,
                (long)transparentDrawCommands.capacity() * DrawCommand.STRIDE_BYTES, GL_DYNAMIC_DRAW);

        biboID = glGenBuffers();
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, biboID);
        glBufferData(GL_DRAW_INDIRECT_BUFFER,
                (long)blendableDrawCommands.capacity() * DrawCommand.STRIDE_BYTES, GL_DYNAMIC_DRAW);

        cboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, cboID);
        glBufferData(GL_ARRAY_BUFFER,
                (long)drawCommands.capacity() * (2 * Integer.BYTES), GL_DYNAMIC_DRAW);

        glVertexAttribIPointer(1, 2, GL_INT, 2 * Integer.BYTES, 0);
        glVertexAttribDivisor(1, 1);
        glEnableVertexAttribArray(1);
    }

    private void generateDrawCommands() {
        for (Subchunk subchunk : subchunks) {
            if (subchunk.getState() == Subchunk.State.MESHED) {
                float yMin = subchunk.getSubchunkLevel() * 16;
                Vector3f min = Chunk.toWorldCoords(subchunk.getChunkCoords(), yMin)
                        .sub(0.5f, 0.5f, 0.5f);
                Vector3f max = new Vector3f(min)
                        .add(new Vector3f(16.0f, 16.0f, 16.0f));
                if (camera.getFrustrum().isBoxVisible(min, max)) {
                    Range range;
                    if ((range = subchunk.getVertexBuffer().getRange(0)).size() > 0) {
                        addDrawCommand(subchunk, range, drawCommands);
                    }
                    if ((range = subchunk.getVertexBuffer().getRange(1)).size() > 0) {
                        addDrawCommand(subchunk, range, transparentDrawCommands);
                    }
                    if ((range = subchunk.getVertexBuffer().getRange(2)).size() > 0) {
                        addDrawCommand(subchunk, range, blendableDrawCommands);
                    }
                }
            }
        }
    }

    private void addDrawCommand(Subchunk subchunk, Range range, IDrawCommandBuffer drawCommandBuffer) {
        int first = subchunk.getFirst() + range.getFromIndex();
        int vertexCount = range.size();
        int baseInstance = drawCommandBuffer.size();
        DrawCommand drawCommand = new DrawCommand(vertexCount, 1, first, baseInstance);
        drawCommandBuffer.add(drawCommand, subchunk.getChunkCoords());
    }

    private void draw(int iboID, Shader shader, IDrawCommandBuffer drawCommandBuffer) {
        glBindBuffer(GL_ARRAY_BUFFER, cboID);
        glBufferSubData(GL_ARRAY_BUFFER, 0, drawCommandBuffer.getChunkCoords());
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, iboID);
        glBufferSubData(GL_DRAW_INDIRECT_BUFFER, 0, drawCommandBuffer.getDrawCommands());
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
        glMultiDrawArraysIndirect(GL_TRIANGLES, 0, drawCommandBuffer.size(), 0);
        texture.unbind();
        shader.detach();
        drawCommandBuffer.reset();
    }

    public void render() {
        generateDrawCommands();

        if (drawCommands.size() > 0) {
            // Render pass 1:
            // set opaque render states
            glEnable(GL_CULL_FACE);
            glEnable(GL_DEPTH_TEST);
            glDepthFunc(GL_LESS);
            glDepthMask(true);
            glDisable(GL_BLEND);
            // draw opaque geometry
            draw(iboID, shader, drawCommands);
        }

        if (transparentDrawCommands.size() > 0) {
            // Render pass 2:
            // set transparent render states
            glDisable(GL_CULL_FACE);
            glEnable(GL_DEPTH_TEST);
            glDepthFunc(GL_LESS);
            glDepthMask(true);
            glDisable(GL_BLEND);
            // draw transparent geometry
            draw(tiboID, transparentShader, transparentDrawCommands);
        }

        if (blendableDrawCommands.size() > 0) {
            // Render pass 3:
            // set blendable render states
            glDisable(GL_CULL_FACE);
            glDepthMask(false);
            glEnable(GL_BLEND);
            glBlendFunci(1, GL_ONE, GL_ONE); // accumulation blend target
            glBlendFunci(2, GL_ZERO, GL_ONE_MINUS_SRC_COLOR); // revealage blend target
            glBlendEquation(GL_FUNC_ADD);
            // configure framebuffer
            glDrawBuffers(Constants.BUFS_NONE_ONE_TWO);
            glClearBufferfv(GL_COLOR, 1, Constants.ZERO_FILLER_VEC);
            glClearBufferfv(GL_COLOR, 2, Constants.ONE_FILLER_VEC);
            // draw blendable geometry
            draw(biboID, blendableShader, blendableDrawCommands);
        }

        // Render pass 3:
        // set composite render states
        glDepthFunc(GL_ALWAYS);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        // configure framebuffer
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

        // reset render states
        glDepthFunc(GL_LESS);
    }

    public void destroy() {
        glUnmapBuffer(vboID);
        glDeleteBuffers(vboID);
        glDeleteBuffers(iboID);
        glDeleteBuffers(tiboID);
        glDeleteBuffers(biboID);
        glDeleteBuffers(cboID);
        glDeleteVertexArrays(vaoID);
    }
}
