package com.github.wnebyte.minecraft.world;

import java.util.Objects;
import java.util.Random;

import org.joml.Vector2i;
import org.joml.Vector3i;
import org.joml.Vector3f;
import com.github.wnebyte.minecraft.renderer.VertexBuffer;
import com.github.wnebyte.minecraft.renderer.DrawCommand;
import com.github.wnebyte.minecraft.util.*;

public class Chunk {

    /*
    ###########################
    #        UTILITIES        #
    ###########################
    */

    public enum FaceType {
        FRONT,
        RIGHT,
        BACK,
        LEFT,
        TOP,
        BOTTOM;
    }

    public static Vector3i toIndex3D(int index) {
        int z = index / (Chunk.WIDTH * Chunk.HEIGHT);
        index -= (z * Chunk.WIDTH * Chunk.HEIGHT);
        int y = index / Chunk.WIDTH;
        int x = index % Chunk.WIDTH;
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

    public static Vector3f toWorld(Vector2i chunkCoords){
        float x = chunkCoords.x * Chunk.WIDTH;
        float y = 0;
        float z = chunkCoords.y * Chunk.DEPTH;
        return new Vector3f(x, y, z);
    }

    public static Vector3f index2World(int index, Vector2i chunkCoords) {
        Vector3f index3D = JMath.toVector3f(Chunk.toIndex3D(index));
        return new Vector3f(
                index3D.x + (chunkCoords.x * Chunk.WIDTH),
                index3D.y,
                index3D.z + (chunkCoords.y * Chunk.DEPTH));
    }

    public static int getUvIndex(BlockFormat blockFormat, FaceType face) {
        switch (face) {
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

    //   v4 ----------- v5
    //   /|            /|      Axis orientation
    //  / |           / |
    // v0 --------- v1  |      y
    // |  |         |   |      |
    // |  v6 -------|-- v7     +--- x
    // | /          |  /      /
    // |/           | /      z
    // v2 --------- v3

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

    private DrawCommandBuffer transparentDrawCommands;

    private Pool<Key, Subchunk> subchunks;

    // References to neighbouring chunks
    private Chunk cXN, cXP, cZN, cZP, cYN, cYP;

    private Random rand;

    /*
    ###########################
    #       CONSTRUCTORS      #
    ###########################
    */

    public Chunk(int i, int j, int k,
                 Map map, DrawCommandBuffer drawCommands, DrawCommandBuffer transparentDrawCommands,
                 Pool<Key, Subchunk> subchunks) {
        this.chunkPosX = i * WIDTH;
        this.chunkPosY = j * HEIGHT;
        this.chunkPosZ = k * DEPTH;
        this.chunkPos = new Vector3f(chunkPosX, chunkPosY, chunkPosZ);
        this.chunkCoordX = i;
        this.chunkCoordZ = k;
        this.chunkCoords = new Vector2i(i, k);
        this.map = map;
        this.drawCommands = drawCommands;
        this.transparentDrawCommands = transparentDrawCommands;
        this.subchunks = subchunks;
        this.rand = new Random();
        this.data = new Block[WIDTH * HEIGHT * DEPTH];
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

    public void updateNeighbourRefs() {
        cXN = map.get(chunkCoordX - 1, chunkCoordZ);
        cXP = map.get(chunkCoordX + 1, chunkCoordZ);
        cZN = map.get(chunkCoordX, chunkCoordZ - 1);
        cZP = map.get(chunkCoordX, chunkCoordZ + 1);
    }

    private final float minBiomeHeight = 55.0f;

    private final float maxBiomeHeight = 145.0f;

    private final int oceanLevel = 85;

    public void generateTerrain() {
        for (int z = 0; z < Chunk.DEPTH; z++) {
            int maxHeight = 50;
            int stoneHeight = maxHeight - 3;

            for (int x = 0; x < Chunk.WIDTH; x++) {
                for (int y = 0; y < Chunk.HEIGHT; y++) {
                    if (y == 0) {
                        setBlock(Block.BEDROCK, x, y, z);
                    }
                    else if (y < stoneHeight) {
                        setBlock(Block.STONE, x, y, z);
                    }
                    else if (y < maxHeight) {
                        setBlock(Block.DIRT, x, y, z);
                    }
                    else if (y == maxHeight) {
                        setBlock(Block.GRASS, x, y, z);
                    }
                    else {
                        setBlock(Block.AIR, x, y, z);
                    }
                }
            }
        }
    }

    public void generateDecorations() {
        for (int z = 0; z < Chunk.DEPTH; z++) {
            for (int x = 0; x < Chunk.WIDTH; x++) {

            }
        }
    }

    public void generateMesh() {
        for (int j = 0; j < 16; j++) {
            generateMesh(j);
        }
    }

    public void generateMesh(int subchunkLevel) {
        Vector3i v = new Vector3i(chunkCoordX, subchunkLevel, chunkCoordZ);
        Subchunk opaque = subchunks.get(new Key(v, false));
        Subchunk transparent = subchunks.get(new Key(v, true));
        assert (opaque != null && transparent != null) : "buffer is null";
        opaque.chunkCoords = chunkCoords;
        transparent.chunkCoords = chunkCoords;
        opaque.data.reset();
        transparent.data.reset();
        int j = subchunkLevel * 16;
        int jMax = j + 16;
        int access;

        for (; j < jMax; j++) {
            for (int k = 0; k < Chunk.DEPTH; k++) {
                for (int i = 0; i < Chunk.WIDTH; i++) {
                    access = toIndex(i, j, k);
                    Block b = data[access];

                    if (Block.isAir(b)) { continue; }
                    createCube(b.isBlendable() ? transparent.data : opaque.data, b, i, j, k, access);
                }
            }
        }

        if (opaque.data.size() > 0) {
            DrawCommand drawCommand = new DrawCommand();
            drawCommand.vertexCount = opaque.data.getNumVertices();
            drawCommand.first = opaque.first;
            drawCommand.instanceCount = 1;
            drawCommands.putCommand(drawCommand, chunkCoords);
        }

        if (transparent.data.size() > 0) {
            DrawCommand drawCommand = new DrawCommand();
            drawCommand.vertexCount = transparent.data.getNumVertices();
            drawCommand.first = transparent.first;
            drawCommand.instanceCount = 1;
            transparentDrawCommands.putCommand(drawCommand, chunkCoords);
        }
    }

    private void createCube(VertexBuffer buffer, Block b, int i, int j, int k, int access) {
        BlockFormat blockFormat = BlockMap.getBlockFormat(b.id);
        // Left face (X-)
        if (visibleFaceXN(b, i-1, j, k)) {
            appendFace(buffer, blockFormat, access, FaceType.LEFT);
        }
        // Right face (X+)
        if (visibleFaceXP(b, i+1, j, k)) {
            appendFace(buffer, blockFormat, access, FaceType.RIGHT);
        }
        // Back face (Z-)
        if (visibleFaceZN(b, i, j, k-1)) {
            appendFace(buffer, blockFormat, access, FaceType.BACK);
        }
        // Front face (Z+)
        if (visibleFaceZP(b, i, j, k+1)) {
            appendFace(buffer, blockFormat, access, FaceType.FRONT);
        }
        // Bottom face (Y-)
        if (visibleFaceYN(b, i, j-1, k)) {
            appendFace(buffer, blockFormat, access, FaceType.BOTTOM);
        }
        // Top face (Y+)
        if (visibleFaceYP(b, i, j+1, k)) {
            appendFace(buffer, blockFormat, access, FaceType.TOP);
        }
    }

    private void appendFace(VertexBuffer buffer, BlockFormat blockFormat, int access, FaceType face) {
        int uv = getUvIndex(blockFormat, face);
        buffer.append(access, uv, (byte)face.ordinal());
    }

    private boolean visibleFaceXN(Block b, int i, int j, int k) {
        if (i < 0) {
            if (cXN == null) {
                return true;
            } else {
                int index = toIndex(Chunk.WIDTH - 1, j, k);
                Block n = cXN.data[index];
                return compare(b, n);
            }
        }
        int index = toIndex(i, j, k);
        Block n = data[index];
        return compare(b, n);
    }

    private boolean visibleFaceXP(Block b, int i, int j, int k) {
        if (i >= Chunk.WIDTH) {
            if (cXP == null) {
                return true;
            } else {
                int index = toIndex(0, j, k);
                Block n = cXP.data[index];
                return compare(b, n);
            }
        }
        int index = toIndex(i, j, k);
        Block n = data[index];
        return compare(b, n);
    }

    private boolean visibleFaceYN(Block b, int i, int j, int k) {
        if (j < 0) {
            if (cYN == null) {
                return true;
            } else {
                int index = toIndex(i, Chunk.HEIGHT - 1, k);
                Block n = cYN.data[index];
                return compare(b, n);
            }
        }
        int index = toIndex(i, j, k);
        Block n = data[index];
        return compare(b, n);
    }

    private boolean visibleFaceYP(Block b, int i, int j, int k) {
        if (j >= Chunk.HEIGHT) {
            if (cYP == null) {
                return true;
            } else {
                int index = toIndex(i, 0, k);
                Block n = cYP.data[index];
                return compare(b, n);
            }
        }
        int index = toIndex(i, j, k);
        Block n = data[index];
        return compare(b, n);
    }

    private boolean visibleFaceZN(Block b, int i, int j, int k) {
        if (k < 0) {
            if (cZN == null) {
                return true;
            } else {
                int index = toIndex(i, j, Chunk.DEPTH - 1);
                Block n = cZN.data[index];
                return compare(b, n);
            }
        }
        int index = toIndex(i, j, k);
        Block n = data[index];
        return compare(b, n);
    }

    private boolean visibleFaceZP(Block b, int i, int j, int k) {
        if (k >= Chunk.DEPTH) {
            if (cZP == null) {
                return true;
            } else {
                int index = toIndex(i, j, 0);
                Block n = cZP.data[index];
                return compare(b, n);
            }
        }
        int index = toIndex(i, j, k);
        Block n = data[index];
        return compare(b, n);
    }

    private boolean differentBlock(int access, Block compare) {
        Block b = data[access];
        return b.id != compare.id;
    }

    private boolean compare(Block b, Block neighbour) {
        if (Block.isAir(neighbour)) {
            return true;
        }
        if (b.isSolid() && neighbour.isTransparent()) {
            return true;
        }
        return false;
    }

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
