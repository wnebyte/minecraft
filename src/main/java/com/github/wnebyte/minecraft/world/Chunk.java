package com.github.wnebyte.minecraft.world;

import java.util.List;
import java.util.Objects;
import org.joml.Vector2i;
import org.joml.Vector3i;
import org.joml.Vector3f;
import com.github.wnebyte.minecraft.renderer.VertexBuffer;
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

    public enum State {
        MESHED,
        UNMESHED
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
        int y = (int)Math.floor(pos.y / 16);
        int z = (int)Math.floor(pos.z / Chunk.DEPTH);
        return new Vector3i(x, y, z);
    }

    public static Vector3f toWorldCoords(Vector2i chunkCoords){
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

    public static Vector3i world2Index3D(Vector3f pos, Vector2i chunkCoords) {
        int i = (int)Math.floor(pos.x - (chunkCoords.x * Chunk.WIDTH));
        int j = (int)Math.floor(pos.y);
        int k = (int)Math.floor(pos.z - (chunkCoords.y * Chunk.DEPTH));
        return new Vector3i(i, j, k);
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

    private static final Formatter<Chunk> FILEPATH_FORMATTER = chunk ->
            String.format("chunk-%d-%d.json", chunk.chunkCoordX, chunk.chunkCoordZ);

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

    private Pool<Key, Subchunk> subchunks;

    // References to neighbouring chunks
    private Chunk cXN, cXP, cZN, cZP, cYN, cYP;

    /*
    ###########################
    #       CONSTRUCTORS      #
    ###########################
    */

    public Chunk(int i, int j, int k, Map map, Pool<Key, Subchunk> subchunks) {
        this.chunkPosX = i * WIDTH;
        this.chunkPosY = j * HEIGHT;
        this.chunkPosZ = k * DEPTH;
        this.chunkPos = new Vector3f(chunkPosX, chunkPosY, chunkPosZ);
        this.chunkCoordX = i;
        this.chunkCoordZ = k;
        this.chunkCoords = new Vector2i(i, k);
        this.map = map;
        this.subchunks = subchunks;
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

    public Block getBlock(Vector3f pos) {
        Vector3i index = Chunk.world2Index3D(pos, chunkCoords);
        return getBlock(index.x, index.y, index.z);
    }

    public void setBlock(Block b, int i, int j, int k) {
        setBlock(b, i, j, k, false);
    }

    public void setBlock(Block b, int i, int j, int k, boolean remesh) {
        int index = toIndex(i, j, k);
        data[index] = b;

        if (remesh) {
            int subchunkLevel = j / 16;
            generateMesh(subchunkLevel);

            // YN(-)
            if (j % 16 == 0 && subchunkLevel != 0) {
                generateMesh(subchunkLevel - 1);
            }
            // YP(+)
            else if ((j + 1) % 16 == 0 && subchunkLevel != 15) {
                generateMesh(subchunkLevel + 1);
            }
            // XN(-)
            if (i == 0 && cXN != null) {
                cXN.generateMesh(subchunkLevel);
            }
            // XP(+)
            else if (i == Chunk.WIDTH - 1 && cXP != null) {
                cXP.generateMesh(subchunkLevel);
            }
            // ZN(-)
            if (k == 0 && cZN != null) {
                cZN.generateMesh(subchunkLevel);
            }
            // ZP(+)
            else if (k == Chunk.DEPTH - 1 && cZP != null) {
                cZP.generateMesh(subchunkLevel);
            }

        }
    }

    public void unload() {
        String path = Assets.DIR + "/data/world/" + FILEPATH_FORMATTER.format(this);
        serialize(path);
        unmesh();
    }

    public void load() {
        String path = Assets.DIR + "/data/world/" + FILEPATH_FORMATTER.format(this);
        if (!deserialize(path)) {
            generateTerrain();
        }
        generateMesh();
    }

    private boolean deserialize(String path) {
        if (!Files.exists(path)) { return false; }
        List<String> lines = Files.readAllLines(path);
        if (lines != null) {
            String json = String.join(System.lineSeparator(), lines);
            Block[] blocks = Settings.GSON.fromJson(json, Block[].class);
            if (blocks != null) {
                System.arraycopy(blocks, 0, data, 0, blocks.length);
            }
        }
        return true;
    }

    private void serialize(String path) {
        String json = Settings.GSON.toJson(data, Block[].class);
        Files.write(path, json);
    }

    public void updateNeighbourRefs() {
        cXN = map.getChunk(chunkCoordX - 1, chunkCoordZ);
        cXP = map.getChunk(chunkCoordX + 1, chunkCoordZ);
        cZN = map.getChunk(chunkCoordX, chunkCoordZ - 1);
        cZP = map.getChunk(chunkCoordX, chunkCoordZ + 1);
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

    public void unmesh() {
        for (int j = 0; j < 16; j++) {
            unmesh(j);
        }
    }

    public void unmesh(int subchunkLevel) {
        Vector3i v = new Vector3i(chunkCoordX, subchunkLevel, chunkCoordZ);
        Key opaqueKey = new Key(v, false);
        Key transparentKey = new Key(v, true);
        Subchunk opaqueSubchunk = subchunks.get(opaqueKey);
        Subchunk transparentSubchunk = subchunks.get(transparentKey);
        opaqueSubchunk.setState(Subchunk.State.UNMESHED);
        transparentSubchunk.setState(Subchunk.State.UNMESHED);
        assert subchunks.free(opaqueKey) : "Free failed";
        assert subchunks.free(transparentKey) : "Free failed";
    }

    public void generateMesh() {
        updateNeighbourRefs();
        for (int j = 0; j < 16; j++) {
            generateMesh(j);
        }
    }

    public void generateMesh(int subchunkLevel) {
        Vector3i v = new Vector3i(chunkCoordX, subchunkLevel, chunkCoordZ);
        Key opaqueKey = new Key(v, false);
        Key transparentKey = new Key(v, true);
        Subchunk opaqueSubchunk = subchunks.get(opaqueKey);
        Subchunk transparentSubchunk = subchunks.get(transparentKey);
        assert (opaqueSubchunk != null && transparentSubchunk != null) : "buffer is null";
        opaqueSubchunk.setBlendable(false);
        opaqueSubchunk.setChunkCoords(chunkCoords);
        opaqueSubchunk.resetVertexBuffer();
        transparentSubchunk.setBlendable(true);
        transparentSubchunk.setChunkCoords(chunkCoords);
        transparentSubchunk.resetVertexBuffer();
        int j = subchunkLevel * 16;
        int jMax = j + 16;
        int access;

        for (; j < jMax; j++) {
            for (int k = 0; k < Chunk.DEPTH; k++) {
                for (int i = 0; i < Chunk.WIDTH; i++) {
                    access = toIndex(i, j, k);
                    Block b = data[access];

                    if (Block.isAir(b)) { continue; }
                    createCube(b.isBlendable() ? transparentSubchunk.getVertexBuffer() : opaqueSubchunk.getVertexBuffer(),
                            b, i, j, k, access);
                }
            }
        }

        opaqueSubchunk.setState(Subchunk.State.MESHED);
        transparentSubchunk.setState(Subchunk.State.MESHED);
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

    private boolean compare(Block a, Block b) {
        if (Block.isAir(b)) {
            return true;
        } if (a.isSolid() && b.isTransparent()) {
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

    public Chunk[] getNeighbours() {
        return new Chunk[]{ cXN, cXP, cZN, cZP };
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
