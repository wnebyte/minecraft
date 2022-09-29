package com.github.wnebyte.minecraft.renderer;

import org.joml.Vector2i;
import java.nio.ByteBuffer;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.GL_MAP_WRITE_BIT;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL40.GL_DRAW_INDIRECT_BUFFER;
import static org.lwjgl.opengl.GL44.*;
import static org.lwjgl.opengl.GL45.glCreateBuffers;
import static org.lwjgl.opengl.GL45.glCreateVertexArrays;

public class DrawIndirect {

    /*
     typedef struct {
     *     uint count;
     *     uint primCount;
     *     uint first;
     *     uint baseInstance;
     * } DrawArraysIndirectCommand;</code></pre>
     */
    public static class DrawArraysIndirectCommand {
        public int count;
        public int primCount;
        public int first;
        public int baseInstance;

        public int[] toArray() {
            return new int[]{
                    count,
                    primCount,
                    first,
                    baseInstance
            };
        }
    }

    public static class DrawCommand {

        public DrawArraysIndirectCommand command;

        public int distanceToPlayer;

        public int level;

        public DrawCommand(DrawArraysIndirectCommand command, int distanceToPlayer, int level) {
            this.command = command;
            this.distanceToPlayer = distanceToPlayer;
            this.level = level;
        }
    }

    public static class CommandBufferContainer {

        public int maxNumCommands;

        public boolean isTransparent;

        public DrawCommand[] commandBuffer;

        public int numCommands;

        private int[] chunkPosBuffer;

        public CommandBufferContainer(int maxNumCommands, boolean isTransparent) {
            this.maxNumCommands = maxNumCommands;
            this.isTransparent = isTransparent;
            this.numCommands = 0;
            this.commandBuffer = null;
            this.chunkPosBuffer = null;
        }

        public void init() {
            this.commandBuffer = new DrawCommand[maxNumCommands];
            this.chunkPosBuffer = new int[maxNumCommands * 2];
            this.numCommands = 0;
        }

        public void add(DrawArraysIndirectCommand command, Vector2i chunkCoords, int level, Vector2i playerCoords) {
            Vector2i delta = new Vector2i(chunkCoords).sub(new Vector2i(playerCoords));
            int deltaSquared = (delta.x * delta.x) + (delta.y * delta.y);
            commandBuffer[numCommands] = new DrawCommand(command, deltaSquared, level);
            command.baseInstance = numCommands;
            chunkPosBuffer[(numCommands * 2)] = chunkCoords.x;
            chunkPosBuffer[(numCommands * 2) + 1] = chunkCoords.y;
            numCommands++;
        }
    }

    private static final int MAX_VERTS_PER_SUB_CHUNK = 0;

    private static final int STRIDE_BYTES = 12;

    private int chunkPosInstancedBuffer;

    private int vaoID;

    private int vboID;

    private int drawCommandVboID;

    private Shader shader;

    private CommandBufferContainer commandBuffer;

    public void init() {
        commandBuffer.init();
        drawCommandVboID = glCreateBuffers();
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, drawCommandVboID);
        // one draw command per subchunk
        glBufferData(GL_DRAW_INDIRECT_BUFFER, (6 * Integer.BYTES) * 5, GL_DYNAMIC_DRAW);

        // generate a bunch of empty vertex buckets for GPU
        vaoID = glCreateVertexArrays();
        glBindVertexArray(vaoID);

        vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);

        long totalSize = (long)5 * MAX_VERTS_PER_SUB_CHUNK * STRIDE_BYTES;

        // set vertex attributes

        // set up global immutable buffer
        int flags = GL_MAP_PERSISTENT_BIT | GL_MAP_WRITE_BIT | GL_MAP_COHERENT_BIT;
        glBufferStorage(GL_ARRAY_BUFFER, totalSize, flags);
        ByteBuffer basePointer =
                glMapBufferRange(GL_ARRAY_BUFFER, 0, 5 * MAX_VERTS_PER_SUB_CHUNK, flags);


    }
}
