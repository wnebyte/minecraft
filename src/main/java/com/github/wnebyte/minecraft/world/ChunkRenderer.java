package com.github.wnebyte.minecraft.world;

import java.nio.ByteBuffer;
import org.joml.Vector3i;
import org.joml.Vector3f;
import org.joml.Matrix4f;
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

    private int ccboID;

    private Framebuffer depthFBO;

    private final Camera camera;

    private final Shader shader;

    private final Shader transparentShader;

    private final Shader blendableShader;

    private final Shader compositeShader;

    private final Shader depthShader;

    private final Texture texture;

    private final IDrawCommandBuffer drawCommands;

    private final IDrawCommandBuffer transparentDrawCommands;

    private final IDrawCommandBuffer blendableDrawCommands;

    private final Pool<Vector3i, Subchunk> subchunks;

    private Vector3f lightPos = new Vector3f(0f, 0f, 0f);

    public ChunkRenderer(Camera camera, Pool<Vector3i, Subchunk> subchunks, Texture texture) {
        this.camera = camera;
        this.shader = Assets.getShader(Assets.DIR + "/shaders/opaque.glsl");
        this.transparentShader = shader;
        this.blendableShader = Assets.getShader(Assets.DIR + "/shaders/transparent.glsl");
        this.compositeShader = Assets.getShader(Assets.DIR + "/shaders/composite.glsl");
        this.depthShader = Assets.getShader(Assets.DIR + "/shaders/depth.glsl");
       // this.texture = Assets.getTexture(Assets.DIR + "/images/generated/packedTextures.png");
        this.texture = texture;
        this.drawCommands = new PrimitiveDrawCommandBuffer(World.CHUNK_CAPACITY * 16);
        this.transparentDrawCommands = new PrimitiveDrawCommandBuffer(World.CHUNK_CAPACITY * 16);
        this.blendableDrawCommands = new PrimitiveDrawCommandBuffer(World.CHUNK_CAPACITY * 16);
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

        ccboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, ccboID);
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
                    Slice slice;
                    if ((slice = subchunk.getVertexBuffer().getSlice(0)).size() > 0) {
                        addDrawCommand(subchunk, slice, drawCommands);
                    }
                    if ((slice = subchunk.getVertexBuffer().getSlice(1)).size() > 0) {
                        addDrawCommand(subchunk, slice, transparentDrawCommands);
                    }
                    if ((slice = subchunk.getVertexBuffer().getSlice(2)).size() > 0) {
                        addDrawCommand(subchunk, slice, blendableDrawCommands);
                    }
                }
            }
        }
    }

    private void addDrawCommand(Subchunk subchunk, Slice slice, IDrawCommandBuffer drawCommandBuffer) {
        int first = subchunk.getFirst() + slice.getFromIndex();
        int vertexCount = slice.size();
        int baseInstance = drawCommandBuffer.size();
        DrawCommand drawCommand = new DrawCommand(vertexCount, 1, first, baseInstance);
        drawCommandBuffer.add(drawCommand, subchunk.getChunkCoords());
    }

    private void drawShadows(int iboID, Shader shader, IDrawCommandBuffer drawCommandBuffer, Matrix4f[] mats) {
        // buffer data
        glBindBuffer(GL_ARRAY_BUFFER, ccboID);
        glBufferSubData(GL_ARRAY_BUFFER, 0, drawCommandBuffer.getChunkCoords());
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, iboID);
        glBufferSubData(GL_DRAW_INDIRECT_BUFFER, 0, drawCommandBuffer.getDrawCommands());
        shader.use();
        // upload uniforms
        shader.uploadMatrix4fArray(Shader.U_MATS, mats);
        shader.uploadFloat(Shader.U_FAR_PLANE, camera.getZFar());
        shader.uploadVec3f(Shader.U_LIGHT_POS, lightPos);
        // bind and upload 3D texture
        glActiveTexture(GL_TEXTURE0);
        texture.bind();
        shader.uploadTexture(Shader.U_TEXTURE, 0);
        // bind and upload uv texture
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_BUFFER, BlockMap.getTexCoordsTextureId());
        shader.uploadTexture(Shader.U_TEX_COORDS_TEXTURE, 1);
        glActiveTexture(GL_TEXTURE2);
        // bind VAO and issue draw command
        glBindVertexArray(vaoID);
        glMultiDrawArraysIndirect(GL_TRIANGLES, 0, drawCommandBuffer.size(), 0);
        // reset
        texture.unbind();
        shader.detach();
    }

    private void draw(int iboID, Shader shader, IDrawCommandBuffer drawCommandBuffer, Texture depthAttachment) {
        // buffer data
        glBindBuffer(GL_ARRAY_BUFFER, ccboID);
        glBufferSubData(GL_ARRAY_BUFFER, 0, drawCommandBuffer.getChunkCoords());
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, iboID);
        glBufferSubData(GL_DRAW_INDIRECT_BUFFER, 0, drawCommandBuffer.getDrawCommands());
        shader.use();
        // upload uniforms
        shader.uploadMatrix4f(Shader.U_VIEW, camera.getViewMatrix());
        shader.uploadMatrix4f(Shader.U_PROJECTION, camera.getProjectionMatrix());
        shader.uploadVec3f(Shader.U_VIEW_POS, camera.getPosition());
        shader.uploadVec3f(Shader.U_LIGHT_POS, lightPos);
        shader.uploadFloat(Shader.U_FAR_PLANE, camera.getZFar());
        // bind and upload 3D texture
        glActiveTexture(GL_TEXTURE0);
        texture.bind();
        shader.uploadTexture(Shader.U_TEXTURE, 0);
        // bind and upload uv texture
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_BUFFER, BlockMap.getTexCoordsTextureId());
        shader.uploadTexture(Shader.U_TEX_COORDS_TEXTURE, 1);
        glActiveTexture(GL_TEXTURE2);
        // bind and upload depth texture
        glBindTexture(GL_TEXTURE_CUBE_MAP, depthAttachment.getId());
        shader.uploadTexture(Shader.U_SHADOW_MAP, 3);
        // bind VAO and issue draw command
        glBindVertexArray(vaoID);
        glMultiDrawArraysIndirect(GL_TRIANGLES, 0, drawCommandBuffer.size(), 0);
        // reset
        texture.unbind();
        shader.detach();
        drawCommandBuffer.reset();
    }

    private Matrix4f getLightSpaceMatrix() {
        // calculate transformations
        Matrix4f lightProjection = new Matrix4f().identity();
        lightProjection.ortho(-100, 100, -100, 100, camera.getZNear(), camera.getZFar());
        Matrix4f lightView = new Matrix4f().identity();
        // eye - center - up
        lightView.lookAt(new Vector3f(lightPos), new Vector3f(camera.getPosition()), new Vector3f(camera.getUp()));
        Matrix4f lightSpaceMatrix = lightProjection.mul(lightView);
        return lightSpaceMatrix;
    }

    public Matrix4f[] getLightSpaceMatrices() {
        float aspect = Application.getWindow().getAspectRatio();
        float zNear = camera.getZNear();
        float zFar = camera.getZFar();
        Matrix4f lightProjection = new Matrix4f().identity();
        lightProjection.perspective((float)Math.toRadians(90.0f), aspect, zNear, zFar);
        Matrix4f[] mats = new Matrix4f[]{
                new Matrix4f().identity().lookAt(
                        new Vector3f(lightPos), new Vector3f(lightPos).add(new Vector3f( 1.0f, 0.0f,  0.0f)), new Vector3f(0.0f, -1.0f,  0.0f)),
                new Matrix4f().identity().lookAt(
                        new Vector3f(lightPos), new Vector3f(lightPos).add(new Vector3f(-1.0f, 0.0f,  0.0f)), new Vector3f(0.0f, -1.0f,  0.0f)),
                new Matrix4f().identity().lookAt(
                        new Vector3f(lightPos), new Vector3f(lightPos).add(new Vector3f(0.0f,  1.0f,  0.0f)), new Vector3f(0.0f,  0.0f,  1.0f)),
                new Matrix4f().identity().lookAt(
                        new Vector3f(lightPos), new Vector3f(lightPos).add(new Vector3f(0.0f, -1.0f,  0.0f)), new Vector3f(0.0f,  0.0f, -1.0f)),
                new Matrix4f().identity().lookAt(
                        new Vector3f(lightPos), new Vector3f(lightPos).add(new Vector3f(0.0f,  0.0f,  1.0f)), new Vector3f(0.0f, -1.0f,  0.0f)),
                new Matrix4f().identity().lookAt(
                        new Vector3f(lightPos), new Vector3f(lightPos).add(new Vector3f(0.0f,  0.0f, -1.0f)), new Vector3f(0.0f, -1.0f,  0.0f))

        };
        Matrix4f[] muls = new Matrix4f[mats.length];
        for (int i = 0; i < mats.length; i++) {
            Matrix4f mat = mats[i];
            muls[i] = new Matrix4f(lightProjection).mul(mat);
        }
        return muls;
    }

    public void render() {
        generateDrawCommands();
        Matrix4f[] mats = getLightSpaceMatrices();
        Framebuffer framebuffer = Application.getFramebuffer();
        Framebuffer depthFramebuffer = Application.getDepthFramebuffer();
        Texture depthAttachment = depthFramebuffer.getDepthAttachment();
        boolean compose = false;
        boolean shadows = true;

        if (shadows) {
            // Shadow render pass:
            // bind depth framebuffer
            depthFramebuffer.bind();
            // configure depth framebuffer
            glClear(GL_DEPTH_BUFFER_BIT);
            // draw shadows
            drawShadows(iboID, depthShader, drawCommands, mats);
            // unbind depth framebuffer
            depthFramebuffer.unbind();
            // bind primary framebuffer
            framebuffer.bind();
        }

        if (drawCommands.size() > 0) {
            // Render pass 1:
            // set opaque render states
            glEnable(GL_CULL_FACE);
            glEnable(GL_DEPTH_TEST);
            glDepthFunc(GL_LESS);
            glDepthMask(true);
            glDisable(GL_BLEND);
            // configure framebuffer
            glDrawBuffers(Constants.BUFS_ZERO_NONE_NONE);
            // draw opaque geometry
            draw(iboID, shader, drawCommands, depthAttachment);
            compose = true;
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
            draw(tiboID, transparentShader, transparentDrawCommands, depthAttachment);
            compose = true;
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
            draw(biboID, blendableShader, blendableDrawCommands, depthAttachment);
            compose = true;
        }

        if (compose) {
            // Render pass 4:
            // set composite render states
            glDepthFunc(GL_ALWAYS);
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            // configure framebuffer
            glDrawBuffers(Constants.BUFS_ZERO_NONE_NONE);
            // draw screen quad
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
    }

    public void destroy() {
        glUnmapBuffer(vboID);
        glDeleteBuffers(vboID);
        glDeleteBuffers(iboID);
        glDeleteBuffers(tiboID);
        glDeleteBuffers(biboID);
        glDeleteBuffers(ccboID);
        glDeleteVertexArrays(vaoID);
    }

    public void setLightPos(Vector3f lightPos) {
        this.lightPos.set(lightPos);
    }
}
