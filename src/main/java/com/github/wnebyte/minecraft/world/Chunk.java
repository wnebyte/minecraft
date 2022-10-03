package com.github.wnebyte.minecraft.world;

import java.util.Objects;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3i;
import org.joml.Vector3f;
import com.github.wnebyte.minecraft.renderer.VertexBuffer;
import com.github.wnebyte.minecraft.renderer.DrawCommand;
import com.github.wnebyte.minecraft.util.DrawCommandBuffer;
import com.github.wnebyte.minecraft.util.Pool;
import com.github.wnebyte.minecraft.util.BlockMap;
import com.github.wnebyte.minecraft.util.BlockFormat;
import com.github.wnebyte.minecraft.util.ChunkHelper;

public class Chunk {

    /*
    ###########################
    #        UTILITIES        #
    ###########################
    */

    private static class Face {

        private enum Type {
            FRONT,
            RIGHT,
            BACK,
            LEFT,
            TOP,
            BOTTOM;
        }

        private final Face.Type type;

        private final float[] tl, tr, bl, br, normals;

        private Face(Face.Type type, float[] tl, float[] tr, float[] bl, float[] br, float[] normals) {
            this.type = type;
            this.tl = tl;
            this.tr = tr;
            this.bl = bl;
            this.br = br;
            this.normals = normals;
        }

        private boolean isTop() {
            return (type == Face.Type.TOP);
        }

        private boolean isBottom() {
            return (type == Face.Type.BOTTOM);
        }
    }

    public static Vector3i toIndex3D(int index) {
        int z = index / (SIZE * SIZE);
        index -= (z * SIZE * SIZE);
        int y = index / SIZE;
        int x = index % SIZE;
        return new Vector3i(x, y, z);
    }

    public static int toIndex(int x, int y, int z) {
        return (z * Chunk.WIDTH * Chunk.HEIGHT) + (y * Chunk.WIDTH) + x;
    }

    public static Vector2i toChunkCoords2D(Vector3f pos) {
        int x = (int)Math.floor(pos.x / Chunk.WIDTH);
        int z = (int)Math.floor(pos.z / Chunk.DEPTH);
        return new Vector2i(x, z);
    }

    public static Vector3i toChunkCoords3D(Vector3f pos) {
        int x = (int)Math.floor(pos.x / Chunk.WIDTH);
        int y = (int)Math.floor(pos.y / Chunk.HEIGHT);
        int z = (int)Math.floor(pos.z / Chunk.DEPTH);
        return new Vector3i(x, y, z);
    }

    public static Vector3f toPosition(Vector2i chunkCoords){
        float x = chunkCoords.x * Chunk.WIDTH;
        float y = 0;
        float z = chunkCoords.y * Chunk.DEPTH;
        return new Vector3f(x, y, z);
    }

    public static int getUvIndex(BlockFormat blockFormat, Face face) {
        switch (face.type) {
            case TOP:
                return blockFormat.getTopTextureFormat().getId();
            case BOTTOM:
                return blockFormat.getBottomTextureFormat().getId();
            default:
                return blockFormat.getSideTextureFormat().getId();
        }
    }

    /*
    ###########################
    #      STATIC FIELDS      #
    ###########################
    */

    public static final int WIDTH = 16;

    public static final int HEIGHT = 256;

    public static final int DEPTH = 16;

    public static final int SIZE = 16;

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
            { -0.5f,  0.5f,  0.5f  },
            {  0.5f,  0.5f,  0.5f  },
            { -0.5f, -0.5f,  0.5f  },
            {  0.5f, -0.5f,  0.5f  },
            { -0.5f,  0.5f, -0.5f  },
            {  0.5f,  0.5f, -0.5f  },
            { -0.5f, -0.5f, -0.5f  },
            {  0.5f, -0.5f, -0.5f  }
    };

    public static final int[][] INDICES = {
            { 1, 0, 2, 3, 1, 2 }, // Front face  (0)
            { 5, 1, 3, 7, 5, 3 }, // Right face  (1)
            { 7, 6, 4, 5, 7, 4 }, // Back face   (2)
            { 0, 4, 6, 2, 0, 6 }, // Left face   (3)
            { 5, 4, 0, 1, 5, 0 }, // Top face    (4)
            { 3, 2, 6, 7, 3, 6 }  // Bottom face (5)
    };

    public static final float[][] NORMALS = {
            {  0,  0,  1  },
            {  1,  0,  0  },
            {  0,  0, -1  },
            { -1,  0,  0  },
            {  0,  1,  0  },
            {  0, -1,  0  }
    };

    public static final Face FRONT_FACE =
            new Face(Face.Type.FRONT,
                    VERTICES[INDICES[0][1]], VERTICES[INDICES[0][0]], VERTICES[INDICES[0][2]], VERTICES[INDICES[0][3]],
                    null);

    public static final Face RIGHT_FACE =
            new Face(Face.Type.RIGHT,
                    VERTICES[INDICES[1][1]], VERTICES[INDICES[1][0]], VERTICES[INDICES[1][2]], VERTICES[INDICES[1][3]],
                    null);

    public static final Face BACK_FACE =
            new Face(Face.Type.BACK,
                    VERTICES[INDICES[2][1]], VERTICES[INDICES[2][0]], VERTICES[INDICES[2][2]], VERTICES[INDICES[2][3]],
                    null);

    public static final Face LEFT_FACE =
            new Face(Face.Type.LEFT,
                    VERTICES[INDICES[3][1]], VERTICES[INDICES[3][0]], VERTICES[INDICES[3][2]], VERTICES[INDICES[3][3]],
                    null);

    public static final Face TOP_FACE =
            new Face(Face.Type.TOP,
                    VERTICES[INDICES[4][1]], VERTICES[INDICES[4][0]], VERTICES[INDICES[4][2]], VERTICES[INDICES[4][3]],
                    null);

    public static final Face BOTTOM_FACE =
            new Face(Face.Type.BOTTOM,
                    VERTICES[INDICES[5][1]], VERTICES[INDICES[5][0]], VERTICES[INDICES[5][2]], VERTICES[INDICES[5][3]],
                    null);

    /*
    ###########################
    #          FIELDS         #
    ###########################
    */

    private Block[] data;

    // Global map position of this chunk
    private int chunkPosX, chunkPosY, chunkPosZ;

    private Vector3f chunkPos;

    // Relative position of this chunk
    private int chunkCoordX, chunkCoordZ;

    private Vector2i chunkCoords;

    private Map map;

    private DrawCommandBuffer drawCommands;

    private Pool<Vector3i, VertexBuffer> subchunks;

    // References to neighbouring chunks
    private Chunk cXN, cXP, cYN, cYP, cZN, cZP;

    private ChunkHelper chunkHelper;

    /*
    ###########################
    #       CONSTRUCTORS      #
    ###########################
    */

    public Chunk(int i, int j, int k,
                 Map map, DrawCommandBuffer drawCommands, Pool<Vector3i, VertexBuffer> subchunks) {
        this.chunkPosX = i * WIDTH;
        this.chunkPosY = j * HEIGHT;
        this.chunkPosZ = k * DEPTH;
        this.chunkPos = new Vector3f(chunkPosX, chunkPosY, chunkPosZ);
        this.chunkCoordX = i;
        this.chunkCoordZ = k;
        this.chunkCoords = new Vector2i(i, k);
        this.map = map;
        this.drawCommands = drawCommands;
        this.subchunks = subchunks;
        this.data = new Block[WIDTH * HEIGHT * DEPTH];
        this.chunkHelper = new ChunkHelper();
    }

    /*
    ###########################
    #         METHODS         #
    ###########################
    */

    /**
     * Returns the block located at the specified chunk relative position.
     * @param i chunk relative x position.
     * @param j chunk relative y position.
     * @param k chunk relative z position.
     * @return the block.
     */
    public Block getBlock(int i, int j, int k) {
        int index = toIndex(i, j, k);
        return data[index];
    }

    public void setBlock(Block b, int i, int j, int k) {
        int index = toIndex(i, j, k);
        data[index] = b;
    }

    public void patchNeighbours() {
        cXN = map.get(chunkCoordX - 1, chunkCoordZ);
        cXP = map.get(chunkCoordX + 1, chunkCoordZ);
        cZN = map.get(chunkCoordX, chunkCoordZ - 1);
        cZP = map.get(chunkCoordX, chunkCoordZ + 1);
    }

    public void generateTerrain() {
        float minBiomeHeight = 55.0f;
        float maxBiomeHeight = 145.0f;

        for (int z = 0; z < Chunk.DEPTH; z++) {
            int maxHeight = 50;
            int stoneHeight = maxHeight - 3;

            for (int x = 0; x < Chunk.WIDTH; x++) {
                for (int y = 0; y < Chunk.HEIGHT; y++) {
                    if (x == 0 && y == 0 && (z == 0 || z == 1)) {
                        setBlock(Block.GLASS, x, y, z);
                    }
                    else if (y == 0) {
                        setBlock(Block.BEDROCK, x, y, z);
                    }
                    else if (y < stoneHeight) {
                        setBlock(Block.STONE, x, y, z);
                    }
                    else if (y < maxHeight) {
                        setBlock(Block.DIRT, x, y, z);
                    }
                    else if (y == maxHeight) {
                        setBlock(Block.SAND, x, y, z);
                    }
                    else {
                        setBlock(Block.AIR, x, y, z);
                    }
                }
            }
        }
    }

    // simple 32x32x32 chunk consists of:
    // 196,608 vertices
    // 32,768 quads
    // greedy mesh function generates a chunk with:
    // 13,056 vertices
    // 2176 quads
    // face culling mesh function generates a chunk with:
    // 36888 vertices
    // 6148 quads
    public void generateMesh() {
        for (int j = 0; j < 16; j++) {
            generateMesh(j);
        }
    }

    public void generateMesh(int subchunkLevel) {
        VertexBuffer buffer = subchunks.get(new Vector3i(chunkCoordX, subchunkLevel, chunkCoordZ));
        assert (buffer != null) : "buffer is null";
        buffer.reset();
        int j = subchunkLevel * 16;
        int jMax = j + 16;
        int access;

        for (; j < jMax; j++) {
            for (int k = 0; k < Chunk.DEPTH; k++) {
                for (int i = 0; i < Chunk.WIDTH; i++) {
                    access = toIndex(i, j, k);
                    Block b = data[access];

                    if (Block.isAir(b)) { continue; }
                    createCube(buffer, b, i, j, k);
                }
            }
        }

        DrawCommand drawCommand = new DrawCommand();
        drawCommand.vertexCount = buffer.getNumVertices();
        drawCommand.first = buffer.first;
        drawCommand.instanceCount = 1;
        drawCommands.setDrawCommand(buffer.drawCommandIndex, drawCommand, chunkCoords);
    }

    private void createCube(VertexBuffer buffer, Block b, int i, int j, int k) {
        BlockFormat format = BlockMap.getBlockFormat(b.id);
        // Left face (X-)
        if (visibleFaceXN(i-1, j, k)) {
            append(i, j, k, buffer, format, LEFT_FACE);
        }
        // Right face (X+)
        if (visibleFaceXP(i+1, j, k)) {
            append(i, j, k, buffer, format, RIGHT_FACE);
        }
        // Back face (Z-)
        if (visibleFaceZN(i, j, k-1)) {
            append(i, j, k, buffer, format, BACK_FACE);
        }
        // Front face (Z+)
        if (visibleFaceZP(i, j, k+1)) {
            append(i, j, k, buffer, format, FRONT_FACE);
        }
        // Bottom face (Y-)
        if (visibleFaceYN(i, j-1, k)) {
            append(i, j, k, buffer, format, BOTTOM_FACE);
        }
        // Top face (Y+)
        if (visibleFaceYP(i, j+1, k)) {
            append(i, j, k, buffer, format, TOP_FACE);
        }
    }

    private void append(int i, int j, int k, VertexBuffer buffer, BlockFormat blockFormat, Face face) {
        int position = toIndex(i, j, k);
        int uv = getUvIndex(blockFormat, face);
        buffer.append(position, uv, (byte)face.type.ordinal());
    }

    /*
    private void appendFace(VertexBuffer buffer, BlockFormat format, int i, int j, int k, Face face) {
        Vector2f[] uvs = getUvIndex(format, face);
        Vector3f tl = new Vector3f(i + (face.tl[0]), j + (face.tl[1]), k + (face.tl[2]));
        Vector3f tr = new Vector3f(i + (face.tr[0]), j + (face.tr[1]), k + (face.tr[2]));
        Vector3f bl = new Vector3f(i + (face.bl[0]), j + (face.bl[1]), k + (face.bl[2]));
        Vector3f br = new Vector3f(i + (face.br[0]), j + (face.br[1]), k + (face.br[2]));
        Vector2f uv0 = uvs[3];
        Vector2f uv1 = uvs[0];
        Vector2f uv2 = uvs[2];
        Vector2f uv3 = uvs[1];
        buffer.appendQuad(tl, tr, bl, br, uv0, uv1, uv2, uv3);
    }
     */

    private boolean visibleFaceXN(int i, int j, int k) {
        if (i < 0) {
            if (cXN == null) {
                return true;
            }

            int index = toIndex(Chunk.WIDTH - 1, j, k);
            Block b = cXN.data[index];
            return b.isTransparent();
        }

        int index = toIndex(i, j, k);
        Block b = data[index];
        return b.isTransparent();
    }

    private boolean visibleFaceXP(int i, int j, int k) {
        if (i >= WIDTH) {
            if (cXP == null) {
                return true;
            }

            int index = toIndex(0, j, k);
            Block b = cXP.data[index];
            return b.isTransparent();
        }

        int index = toIndex(i, j, k);
        Block b = data[index];
        return b.isTransparent();
    }

    private boolean visibleFaceYN(int i, int j, int k) {
        if (j < 0) {
            if (cYN == null) {
                return true;
            }

            int index = toIndex(i, Chunk.HEIGHT - 1, k);
            Block b = cYN.data[index];
            return b.isTransparent();
        }

        int index = toIndex(i, j, k);
        Block b = data[index];
        return b.isTransparent();
    }

    private boolean visibleFaceYP(int i, int j, int k) {
        if (j >= HEIGHT) {
            if (cYP == null) {
                return true;
            }

            int index = toIndex(i, 0, k);
            Block b = cYN.data[index];
            return b.isTransparent();
        }

        int index = toIndex(i, j, k);
        Block b = data[index];
        return b.isTransparent();
    }

    private boolean visibleFaceZN(int i, int j, int k) {
        if (k < 0) {
            if (cZN == null) {
                return true;
            }

            int index = toIndex(i, j, Chunk.DEPTH - 1);
            Block b = cYN.data[index];
            return b.isTransparent();
        }

        int index = toIndex(i, j, k);
        Block b = data[index];
        return b.isTransparent();
    }

    private boolean visibleFaceZP(int i, int j, int k) {
        if (k >= DEPTH) {
            if (cZP == null) {
                return true;
            }

            int index = toIndex(i, j, 0);
            Block b = cYN.data[index];
            return b.isTransparent();
        }

        int index = toIndex(i, j, k);
        Block b = data[index];
        return b.isTransparent();
    }

    private boolean differentBlock(int access, Block compare) {
        Block b = data[access];
        return b.id != compare.id;
    }

    /*
    private void createRun(Block b, int i, int j, int k, int access) {
        // Precalculate variables
        int i1 = j + 1;
        int j1 = j + 1;
        int k1 = (k + 1) << 12;
        int jS = j1 << 6;
        int jS1 = j1 << 6;

        int chunkAccess;
        int length = 0;

        // Left (X-)
        if (!chunkHelper.visitedXN[access] && visibleFaceXN(i-1, j, k)) {
            // Search upward to determine run length
            for (int q = j; q < CHUNK_HEIGHT; q++) {
                chunkAccess = toIndex(i, q, k);

                // If we reach a different block or an empty block, end the run
                if (differentBlock(chunkAccess, b))
                    break;

                // Store that we have visisted this block
                chunkHelper.visitedXN[chunkAccess] = true;

                length++;
            }

            if (length > 0) {
                vertexBuffer.appendQuad(new Vector3i(i, length + j, k + 1), // TL
                                        new Vector3i(i, length + j,    k),     // TR
                                        new Vector3i(i,  j,            k + 1), // BL
                                        new Vector3i(i,  j,               k),     // BR
                                        b.id);

               // vertexBuffer.appendQuadX(i, jS, length, k1, k);
            }
        }

        length = 0;
        // Right (X+)
        if (!chunkHelper.visitedXP[access] && visibleFaceXP(i+1, j, k)) {
            // Search upward to determine run length
            for (int q = j; q < CHUNK_HEIGHT; q++) {
                // Pre-calculate the array lookup as it's used twice
                chunkAccess = toIndex(i, q, k);

                // If we reach a different block or an empty block, end the run
                if (differentBlock(chunkAccess, b))
                    break;

                // Store that we have visisted this block
                chunkHelper.visitedXP[chunkAccess] = true;

                length++;
            }

            if (length > 0) {
                vertexBuffer.appendQuad(new Vector3i(i + 1, length + j,    k),     // TL
                                        new Vector3i(i + 1, length + j, k + 1), // TR
                                        new Vector3i(i + 1,    j,             k),     // BL
                                        new Vector3i(i + 1,    j,          k + 1), // BR
                                        b.id);

               // vertexBuffer.appendQuadX(i1, jS, length, k, k1);
            }
        }

        length = 0;
        // Back (Z-)
        if (!chunkHelper.visitedZN[access] && visibleFaceZN(i, j, k-1)) {
            // Search upward to determine run length
            for (int q = j; q < CHUNK_HEIGHT; q++) {
                // Pre-calculate the array lookup as it's used twice
                chunkAccess = toIndex(i, q, k);

                // If we reach a different block or an empty block, end the run
                if (differentBlock(chunkAccess, b))
                    break;

                // Store that we have visisted this block
                chunkHelper.visitedZN[chunkAccess] = true;

                length++;
            }

            if (length > 0) {
                vertexBuffer.appendQuad(new Vector3i(i + 1, length + j,  k), // TL
                                        new Vector3i(i,        length + j,  k), // TR
                                        new Vector3i(i + 1,  j,             k), // BL
                                        new Vector3i(i,         j,             k), // BR
                                        b.id);
               // vertexBuffer.appendQuadZ(i1, i, jS, length, k);
            }
        }

        length = 0;
        // Front (Z+)
        if (!chunkHelper.visitedZP[access] && visibleFaceZP(i, j, k+1)) {
            // Search upward to determine run length
            for (int q = j; q < CHUNK_HEIGHT; q++) {
                // Pre-calculate the array lookup as it's used twice
                chunkAccess = toIndex(i, q, k);

                // If we reach a different block or an empty block, end the run
                if (differentBlock(chunkAccess, b))
                    break;

                // Store that we have visisted this block
                chunkHelper.visitedZP[chunkAccess] = true;

                length++;
            }

            if (length > 0) {
                vertexBuffer.appendQuad(new Vector3i(i,        length + j, k + 1), // TL
                                        new Vector3i(i + 1, length + j ,k + 1), // TR
                                        new Vector3i(i,         j,            k + 1), // BL
                                        new Vector3i(i + 1,  j,            k + 1), // BR
                                        b.id);

               // vertexBuffer.appendQuadZ(i, i1, jS, length, k1);
            }
        }

        length = 0;
        // Bottom (Y-)
        if (!chunkHelper.visitedYN[access] && visibleFaceYN(i, j-1, k)) {
            // Search upward to determine run length
            for (int q = j; q < CHUNK_HEIGHT; q++) {
                // Pre-calculate the array lookup as it's used twice
                chunkAccess = toIndex(i, q, k);

                // If we reach a different block or an empty block, end the run
                if (differentBlock(chunkAccess, b))
                    break;

                // Store that we have visisted this block
                chunkHelper.visitedYN[chunkAccess] = true;

                length++;
            }

            if (length > 0) {
                vertexBuffer.appendQuad(new Vector3i(i,       j, k + 1), // TL
                                        new Vector3i(i,       j,    k),     // TR
                                        new Vector3i(length,  j, k + 1), // BL
                                        new Vector3i(length,  j,    k),     // BR
                                        b.id);
               // vertexBuffer.appendQuadY(i, length, jS, k1, k);
            }
        }

        length = 0;
        // Top (Y+)
        if (!chunkHelper.visitedYP[access] && visibleFaceYP(i, j+1, k)) {
            // Search upward to determine run length
            for (int q = j; q < CHUNK_HEIGHT; q++) {
                chunkAccess = toIndex(i, q, k);

                // If we reach a different block or an empty block, end the run
                if (differentBlock(chunkAccess, b))
                    break;

                // Store that we have visisted this block
                chunkHelper.visitedYP[chunkAccess] = true;

                length++;
            }

            if (length > 0) {
                vertexBuffer.appendQuad(new Vector3i(i,       j+1,    k),     // TL
                                        new Vector3i(i,       j+1, k + 1), // TR
                                        new Vector3i(length,  j+1,    k),     // BL
                                        new Vector3i(length,  j+1, k + 1), // BR
                                        b.id);
               // vertexBuffer.appendQuadY(i, length, jS1, k, k1);
            }
        }
    }
     */

    public Vector3f getChunkPos() {
        return chunkPos;
    }

    public float getChunkPosX() {
        return chunkPosX;
    }

    public float getChunkPosY() {
        return chunkPosY;
    }

    public float getChunkPosZ() {
        return chunkPosZ;
    }

    public Vector2i getChunkCoords() {
        return chunkCoords;
    }

    public int getChunkCoordX() {
        return chunkCoordX;
    }

    public int getChunkCoordZ() {
        return chunkCoordZ;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof Chunk)) return false;
        Chunk chunk = (Chunk) o;
        return Objects.equals(chunk.chunkCoordX, this.chunkCoordX) &&
                Objects.equals(chunk.chunkCoordZ, this.chunkCoordZ);
    }

    @Override
    public int hashCode() {
        int result = 15;
        return 2 *
                result +
                Objects.hashCode(this.chunkCoordX) +
                Objects.hashCode(this.chunkCoordZ);
    }

    @Override
    public String toString() {
        return String.format("Chunk[x: %d, z: %d]", this.chunkCoordX, this.chunkCoordZ);
    }
}
