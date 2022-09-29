package com.github.wnebyte.minecraft.componenets;

import com.github.wnebyte.minecraft.renderer.VertexBuffer;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3i;
import org.joml.Vector3f;
import com.github.wnebyte.minecraft.util.BlockMap;
import com.github.wnebyte.minecraft.util.BlockFormat;

public class Chunk {

    /*
    ###########################
    #        UTILITIES        #
    ###########################
    */

    static class Face {

        int id;

        private final float[] tl, tr, bl, br, normals;

        private Face(int id, float[] tl, float[] tr, float[] bl, float[] br, float[] normals) {
            this.id = id;
            this.tl = tl;
            this.tr = tr;
            this.bl = bl;
            this.br = br;
            this.normals = normals;
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
        return (z * SIZE * SIZE) + (y * SIZE) + x;
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

    public static Vector3f toPosition(Vector3i chunkCoords) {
        float x = chunkCoords.x * Chunk.WIDTH;
        float y = chunkCoords.y * Chunk.HEIGHT;
        float z = chunkCoords.z * Chunk.DEPTH;
        return new Vector3f(x, y, z);
    }

    /*
    ###########################
    #      STATIC FIELDS      #
    ###########################
    */

    public static final int WIDTH = 16;

    public static final int HEIGHT = 16;

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
            new Face(0,
                    VERTICES[INDICES[0][1]], VERTICES[INDICES[0][0]], VERTICES[INDICES[0][2]], VERTICES[INDICES[0][3]],
                    null);

    public static final Face RIGHT_FACE =
            new Face(1,
                    VERTICES[INDICES[1][1]], VERTICES[INDICES[1][0]], VERTICES[INDICES[1][2]], VERTICES[INDICES[1][3]],
                    null);

    public static final Face BACK_FACE =
            new Face(2,
                    VERTICES[INDICES[2][1]], VERTICES[INDICES[2][0]], VERTICES[INDICES[2][2]], VERTICES[INDICES[2][3]],
                    null);

    public static final Face LEFT_FACE =
            new Face(3,
                    VERTICES[INDICES[3][1]], VERTICES[INDICES[3][0]], VERTICES[INDICES[3][2]], VERTICES[INDICES[3][3]],
                    null);

    public static final Face TOP_FACE =
            new Face(4,
                    VERTICES[INDICES[4][1]], VERTICES[INDICES[4][0]], VERTICES[INDICES[4][2]], VERTICES[INDICES[4][3]],
                    null);

    public static final Face BOTTOM_FACE =
            new Face(5,
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

    // Relative position of this chunk
    private int chunkCoordX, chunkCoordY, chunkCoordZ;

    private Map map;

    // Stores vertex data and manages buffering to the GPU
    public VertexBuffer vertexBuffer;

    // References to neighbouring chunks
    private Chunk cXN, cXP, cYN, cYP, cZN, cZP;

    private ChunkHelper chunkHelper;

    public Chunk(int x, int y, int z, Map map) {
        this.chunkPosX = x;
        this.chunkPosY = y;
        this.chunkPosZ = z;
        this.chunkCoordX = x / Chunk.WIDTH;
        this.chunkCoordY = y / Chunk.HEIGHT;
        this.chunkCoordZ = z / Chunk.DEPTH;
        this.map = map;
        this.data = new Block[WIDTH * HEIGHT * DEPTH];
        this.chunkHelper = new ChunkHelper();
        this.vertexBuffer = new VertexBuffer();
    }

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
        /*
        int newCapacity = vertexBuffer.isEmpty() ? VertexBuffer.DEFAULT_CAPACITY : vertexBuffer.size() + 1024;
        vertexBuffer.reset(newCapacity);

         */

        int access;
        // Y-axis: start from the bottom and search up
        for (int j = 0; j < HEIGHT; j++) {
            // Z-axis
            for (int k = 0; k < DEPTH; k++) {
                // X-axis
                for (int i = 0; i < WIDTH; i++) {
                    access = toIndex(i, j, k);
                    Block b = data[access];

                    if (b == null || b.id == Block.AIR_ID) {
                        continue;
                    }

                    createCube(b, i, j, k);

                    // Todo: move outside loop
                    // Extend the array if it's nearly full
                    /*
                    if (vertexBuffer.remaining() < 2048) {
                        vertexBuffer.extend(2048);
                    }
                     */
                }
            }
        }
    }

    private void createCube(Block b, int i, int j, int k) {
        BlockFormat format = BlockMap.getBlockFormat(b.id);
        // Left face (X-) CONCRETE
        if (visibleFaceXN(i-1, j, k)) {
            appendFace(format, i, j, k, LEFT_FACE);
        }
        // Right face (X+) DIRT
        if (visibleFaceXP(i+1, j, k)) {
            appendFace(format, i, j, k, RIGHT_FACE);
        }
        // Back face (Z-) SNOW
        if (visibleFaceZN(i, j, k-1)) {
            appendFace(format, i, j, k, BACK_FACE);
        }
        // Front face (Z+) BLUE
        if (visibleFaceZP(i, j, k+1)) {
            appendFace(format, i, j, k, FRONT_FACE);
        }
        // Bottom face (Y-) LIGHT GRAY
        if (visibleFaceYN(i, j-1, k)) {
            appendFace(format, i, j, k, BOTTOM_FACE);
        }
        // Top face (Y+) DARK GRAY
        if (visibleFaceYP(i, j+1, k)) {
            appendFace(format, i, j, k, TOP_FACE);
        }
    }

    private void appendFace(BlockFormat format, int i, int j, int k, Face face) {
        float scale = 1f;
        Vector2f[] uvs = getUvs(format, face);
        vertexBuffer.appendQuad(new Vector3f(i + (face.tl[0] * scale), j + (face.tl[1] * scale), k + (face.tl[2] * scale)), // TL(1)
                                new Vector3f(i + (face.tr[0] * scale), j + (face.tr[1] * scale), k + (face.tr[2] * scale)), // TR(0)
                                new Vector3f(i + (face.bl[0] * scale), j + (face.bl[1] * scale), k + (face.bl[2] * scale)), // BL(2)
                                new Vector3f(i + (face.br[0] * scale), j + (face.br[1] * scale), k + (face.br[2] * scale)), // BR(3)
                                uvs[3], uvs[0], uvs[2], uvs[1]);
    }

    private Vector2f[] getUvs(BlockFormat format, Face face) {
        if (face.id == TOP_FACE.id) {
            return format.getTopTextureFormat().getUvs();
        } else if (face.id == BOTTOM_FACE.id) {
            return format.getBottomTextureFormat().getUvs();
        } else {
            return format.getSideTextureFormat().getUvs();
        }
    }

    private boolean visibleFaceXN(int i, int j, int k) {
        if (i < 0) {
            if (cXN == null) {
                return true;
            }

            int index = toIndex(WIDTH - 1, j, k);
            Block b = cXN.data[index];
            return (b == null || b.id == Block.AIR_ID);
        }

        int index = toIndex(i, j, k);
        Block b = data[index];
        return (b == null || b.id == Block.AIR_ID);
    }

    private boolean visibleFaceXP(int i, int j, int k) {
        if (i >= WIDTH) {
            if (cXP == null) {
                return true;
            }

            int index = toIndex(0, j, k);
            Block b = cXP.data[index];
            return (b == null || b.id == Block.AIR_ID);
        }

        int index = toIndex(i, j, k);
        Block b = data[index];
        return (b == null || b.id == Block.AIR_ID);
    }

    private boolean visibleFaceYN(int i, int j, int k) {
        if (j < 0) {
            if (cYN == null) {
                return true;
            }

            int index = toIndex(i, HEIGHT - 1, k);
            Block b = cYN.data[index];
            return (b == null || b.id == Block.AIR_ID);
        }

        int index = toIndex(i, j, k);
        Block b = data[index];
        return (b == null || b.id == Block.AIR_ID);
    }

    private boolean visibleFaceYP(int i, int j, int k) {
        if (j >= HEIGHT) {
            if (cYP == null) {
                return true;
            }

            int index = toIndex(i, 0, k);
            Block b = cYN.data[index];
            return (b == null || b.id == Block.AIR_ID);
        }

        int index = toIndex(i, j, k);
        Block b = data[index];
        return (b == null || b.id == Block.AIR_ID);
    }

    private boolean visibleFaceZN(int i, int j, int k) {
        if (k < 0) {
            if (cZN == null) {
                return true;
            }

            int index = toIndex(i, j, DEPTH - 1);
            Block b = cYN.data[index];
            return (b == null || b.id == Block.AIR_ID);
        }

        int index = toIndex(i, j, k);
        Block b = data[index];
        return (b == null || b.id == Block.AIR_ID);
    }

    private boolean visibleFaceZP(int i, int j, int k) {
        if (k >= DEPTH) {
            if (cZP == null) {
                return true;
            }

            int index = toIndex(i, j, 0);
            Block b = cYN.data[index];
            return (b == null || b.id == Block.AIR_ID);
        }

        int index = toIndex(i, j, k);
        Block b = data[index];
        return (b == null || b.id == Block.AIR_ID);
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

    public Vector3f getChunkPosition() {
        return new Vector3f(chunkPosX, chunkPosY, chunkPosZ);
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

    public Vector3i getChunkCoordinate() {
        return new Vector3i(chunkCoordX, chunkCoordY, chunkCoordZ);
    }

    public VertexBuffer getVertexBuffer() {
        return vertexBuffer;
    }
}
