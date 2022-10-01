package com.github.wnebyte.minecraft.world;

import java.nio.ByteBuffer;
import java.util.Random;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.system.MemoryUtil;
import com.github.wnebyte.minecraft.core.Camera;
import com.github.wnebyte.minecraft.core.MouseListener;
import com.github.wnebyte.minecraft.renderer.*;
import com.github.wnebyte.minecraft.util.Assets;
import com.github.wnebyte.minecraft.util.Pool;
import com.github.wnebyte.minecraft.util.DrawCommandBuffer;
import static com.github.wnebyte.minecraft.renderer.VertexBuffer.*;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
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

    public static final int CHUNK_CAPACITY = 80;

    public static final int SPAWN_CHUNK_SIZE = 5 * 5;

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

    private Random rand;

    private float debounceTime = 0.2f;

    private float debounce = debounceTime;

    public World(Camera camera) {
        this.camera = camera;
        this.shader = Assets.getShader(Assets.DIR + "/shaders/opaque.glsl");
        this.texture = Assets.getTexture(Assets.DIR + "/images/generated/packedTextures.png");
        this.drawCommands = new DrawCommandBuffer(CHUNK_CAPACITY * 16);
        this.subchunks = new Pool<>(CHUNK_CAPACITY * 16);
        this.map = new Map();
        this.rand = new Random();
    }

    public void start() {
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
        int x = (int)(Math.sqrt(SPAWN_CHUNK_SIZE) * 16) / 2;
        int y = 51;
        int z = x;
       // camera.setPosition(new Vector3f(x, y, z));
        camera.setPosition(new Vector3f(0, 0, 0));
    }

    private void initMap() {
        long startTime = System.nanoTime();
        int sqrt = (int)Math.sqrt(SPAWN_CHUNK_SIZE);
        for (int x = 0; x < sqrt; x++) {
            for (int z = 0; z < sqrt; z++) {
                Chunk chunk = new Chunk(x, 0, z, map, drawCommands, subchunks);
                map.put(chunk.getChunkCoords(), chunk);
                chunk.generateTerrain();
                chunk.generateMesh();
            }
        }
        double time = (System.nanoTime() - startTime) * 1E-9;
        System.out.printf("init: %.2fs%n", time);
    }

    public void update(float dt) {
        debounce -= dt;

        if (MouseListener.isMouseButtonDown(GLFW_MOUSE_BUTTON_LEFT) && debounce < 0) {
            Vector3f pos = new Vector3f(camera.getPosition());
            Vector2i chunkPos = Chunk.toChunkCoords2D(new Vector3f(0, 0, 0));
            Chunk chunk = map.get(chunkPos.x, chunkPos.y);
            if (chunk != null) {
                int x = rand.nextInt(Chunk.WIDTH);
                int y = rand.nextInt(20);
                int z = 0;
                int subchunkLevel = y / 16;
                chunk.setBlock(Block.AIR, x, y, z);
                chunk.generateMesh(subchunkLevel);
                System.out.printf("x: %d, y: %d, z: %d%n", x, y, z);
            }

            debounce = debounceTime;
        }
    }

    public void render() {
        glBindVertexArray(vao);
        if (drawCommands.isDirty()) {
            glBindBuffer(GL_DRAW_INDIRECT_BUFFER, ibo);
            glBufferSubData(GL_DRAW_INDIRECT_BUFFER, 0, drawCommands.data());

            glBindBuffer(GL_ARRAY_BUFFER, cbo);
            glBufferSubData(GL_ARRAY_BUFFER, 0, drawCommands.chunkCoords());

            long vertexCount = 0;
            for (DrawCommand drawCommand : drawCommands) {
                vertexCount += drawCommand.vertexCount;
            }
            System.out.println("vertexCount: " + vertexCount);

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
        glBindVertexArray(0);
    }

    public void destroy() {
        glDeleteVertexArrays(0);
        glUnmapBuffer(vbo);
        glDeleteBuffers(vbo);
        glDeleteBuffers(ibo);
        glDeleteBuffers(cbo);
    }
}