package com.github.wnebyte.minecraft.world;

import java.nio.ByteBuffer;

import com.github.wnebyte.minecraft.util.JMath;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.system.MemoryUtil;
import com.github.wnebyte.minecraft.core.Scene;
import com.github.wnebyte.minecraft.core.Camera;
import com.github.wnebyte.minecraft.renderer.*;
import com.github.wnebyte.minecraft.componenets.Chunk;
import com.github.wnebyte.minecraft.componenets.Map;
import com.github.wnebyte.minecraft.util.Assets;
import com.github.wnebyte.minecraft.util.Pool;
import static com.github.wnebyte.minecraft.renderer.VertexBuffer.*;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;
import static org.lwjgl.opengl.GL40.GL_DRAW_INDIRECT_BUFFER;
import static org.lwjgl.opengl.GL43.glMultiDrawArraysIndirect;
import static org.lwjgl.opengl.GL44.glBufferStorage;
import static org.lwjgl.opengl.GL44C.GL_MAP_COHERENT_BIT;
import static org.lwjgl.opengl.GL44C.GL_MAP_PERSISTENT_BIT;

public class World {

    public static final int CHUNK_CAPACITY = 2;

    private int vao;

    private int vbo;

    private int ibo;

    private int cbo;

    private Camera camera;

    private Shader shader;

    private Texture texture;

    private DrawCommandBuffer drawCommands;

    private Pool<Vector3i, VertexBuffer> subchunks;

    private Map map;

    private Vector3f lastCameraPos;

    private float debounceTime = 0.8f;

    private float debounce = debounceTime;

    public World(Camera camera) {
        this.camera = camera;
        this.shader = Assets.getShader(Assets.DIR + "/shaders/opaque.glsl");
        this.texture = Assets.getTexture(Assets.DIR + "/images/generated/packedTextures.png");
        this.drawCommands = new DrawCommandBuffer(CHUNK_CAPACITY * 16);
        this.subchunks = new Pool<>(CHUNK_CAPACITY * 16);
        this.map = new Map();
        this.lastCameraPos = camera.getPosition();
    }

    public void start(Scene scene) {
        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        glVertexAttribPointer(0, POS_SIZE, GL_FLOAT, false, STRIDE_BYTES, POS_OFFSET);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, UV_SIZE, GL_FLOAT, false, STRIDE_BYTES, UV_OFFSET);
        glEnableVertexAttribArray(1);

        int numBuffers = subchunks.size();
        int size = numBuffers * (BUFFER_CAPACITY * STRIDE_BYTES);
        int length = size / numBuffers;
        int flags = GL_MAP_PERSISTENT_BIT | GL_MAP_WRITE_BIT | GL_MAP_COHERENT_BIT;
        glBufferStorage(GL_ARRAY_BUFFER, size, flags);
        ByteBuffer buffer = glMapBufferRange(GL_ARRAY_BUFFER, 0, size, flags);
        long base = MemoryUtil.memAddress(buffer);

        for (int offset = 0, i = 0; offset <= size - length && i < subchunks.size(); offset += length, i++) {
            ByteBuffer slice = MemoryUtil.memByteBuffer(base + offset, length);
            VertexBuffer subchunk = new VertexBuffer(slice);
            subchunk.first = (i * BUFFER_CAPACITY);
            subchunk.drawCommandIndex = i;
            subchunks.add(subchunk);
        }

        ibo = glGenBuffers();
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, ibo);
        glBufferData(GL_DRAW_INDIRECT_BUFFER, (long)drawCommands.maxNumCommands() * DrawCommand.SIZE_BYTES, GL_DYNAMIC_DRAW);

        cbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, cbo);
        glBufferData(GL_ARRAY_BUFFER, (long)drawCommands.maxNumCommands() * (2 * Integer.BYTES), GL_DYNAMIC_DRAW);
        
        glVertexAttribIPointer(2, 2, GL_INT, 2 * Integer.BYTES, 0);
        glVertexAttribDivisor(2, 1);
        glEnableVertexAttribArray(2);

        initMap();
    }

    private void initMap() {
        for (int i = 0; i < CHUNK_CAPACITY; i++) {
            Chunk chunk = new Chunk(i, 0, 0, map);
            map.put(chunk.getChunkCoords(), chunk);
            chunk.generateTerrain();
            chunk.generateMesh(drawCommands, subchunks);
        }
    }

    public void update(float dt) {
        debounce -= dt;
        Vector3f delta = JMath.sub(camera.getPosition(), lastCameraPos);
        lastCameraPos = camera.getPosition();

        if (debounce < 0) {
            System.out.printf("x: %.2f, y: %.2f, z: %.2f", delta.x, delta.y, delta.z);
            debounce = debounceTime;
        }
    }

    public void render() {
        if (drawCommands.isDirty()) {
            glBindBuffer(GL_DRAW_INDIRECT_BUFFER, ibo);
            glBufferSubData(GL_DRAW_INDIRECT_BUFFER, 0, drawCommands.data());

            glBindBuffer(GL_ARRAY_BUFFER, cbo);
            glBufferSubData(GL_ARRAY_BUFFER, 0, drawCommands.chunkCoords());

            for (DrawCommand drawCommand : drawCommands) {
                System.out.println(drawCommand.toJson());
            }

            drawCommands.clean();
        }
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
        glMultiDrawArraysIndirect(GL_TRIANGLES, 0, drawCommands.maxNumCommands(), 0);
        texture.unbind();
        shader.detach();
    }

    public void destroy() {
        glDeleteVertexArrays(0);
        glUnmapBuffer(vbo);
        glDeleteBuffers(vbo);
        glDeleteBuffers(ibo);
        glDeleteBuffers(cbo);
    }
}
