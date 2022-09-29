package com.github.wnebyte.minecraft.world;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.ArrayList;
import com.github.wnebyte.minecraft.core.Scene;
import com.github.wnebyte.minecraft.core.Camera;
import com.github.wnebyte.minecraft.renderer.Shader;
import com.github.wnebyte.minecraft.renderer.Texture;
import com.github.wnebyte.minecraft.renderer.VertexBuffer;
import com.github.wnebyte.minecraft.renderer.DrawCommandBuffer;
import com.github.wnebyte.minecraft.util.Assets;
import static com.github.wnebyte.minecraft.renderer.VertexBuffer.*;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL40.GL_DRAW_INDIRECT_BUFFER;
import static org.lwjgl.opengl.GL43.glMultiDrawArraysIndirect;
import static org.lwjgl.opengl.GL44.glBufferStorage;
import static org.lwjgl.opengl.GL44C.GL_MAP_COHERENT_BIT;
import static org.lwjgl.opengl.GL44C.GL_MAP_PERSISTENT_BIT;

public class World {

    private int vao;

    private int vbo;

    private int ibo;

    private Camera camera;

    private Shader shader;

    private Texture texture;

    private DrawCommandBuffer commands;

    private List<VertexBuffer> buffers;

    public World(Camera camera) {
        this.camera = camera;
        this.shader = Assets.getShader(Assets.DIR + "/shaders/opaque.glsl");
        this.texture = Assets.getTexture(Assets.DIR + "/images/generated/packedTextures.png");
        this.commands = new DrawCommandBuffer(1);
        this.buffers = new ArrayList<>();
    }

    public void start(Scene scene) {
        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        ibo = glGenBuffers();
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, ibo);
        glBufferData(GL_DRAW_INDIRECT_BUFFER, commands.capacity(), GL_STATIC_DRAW);

        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        glVertexAttribPointer(0, POS_SIZE, GL_FLOAT, false, STRIDE_BYTES, POS_OFFSET);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, UV_SIZE, GL_FLOAT, false, STRIDE_BYTES, UV_OFFSET);
        glEnableVertexAttribArray(1);

        long size = (long)buffers.size() *
                VertexBuffer.BUFFER_CAPACITY * STRIDE_BYTES;
        int flags = GL_MAP_PERSISTENT_BIT | GL_MAP_WRITE_BIT | GL_MAP_COHERENT_BIT;
        glBufferStorage(GL_ARRAY_BUFFER, size, flags);
        int offset = 0;
        int length = VertexBuffer.BUFFER_CAPACITY * STRIDE_BYTES;
        for (VertexBuffer buffer : buffers) {
            ByteBuffer data = glMapBufferRange(GL_ARRAY_BUFFER, offset, length, flags);
            offset++;
        }
    }

    public void update(float dt) {}

    public void render() {
        shader.use();
        shader.uploadMatrix4f(Shader.U_PROJECTION, camera.getProjectionMatrix());
        shader.uploadMatrix4f(Shader.U_VIEW, camera.getViewMatrix());
        glActiveTexture(GL_TEXTURE0);
        texture.bind();
        shader.uploadTexture(Shader.U_TEXTURE, 0);
        // param mode specifies what kind of primitive to render
        // param indirect is either an offset, in bytes, into the buffer bound to
        // GL_DRAW_INDIRECT_BUFFER or a pointer to an array struct that holds draw parameters
        // param drawCount the number of elements in the array addresses by indirect
        // param stride is the distance, in bytes, between the elements of the indirect array
        glMultiDrawArraysIndirect(GL_TRIANGLES, 0, commands.capacity(), 0);
        texture.unbind();
        shader.detach();
    }
}
