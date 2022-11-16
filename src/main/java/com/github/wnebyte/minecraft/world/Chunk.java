package com.github.wnebyte.minecraft.world;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import org.joml.Vector2i;
import org.joml.Vector3i;
import org.joml.Vector3f;
import com.github.wnebyte.minecraft.core.Application;
import com.github.wnebyte.minecraft.renderer.Subchunk;
import com.github.wnebyte.minecraft.renderer.VertexBuffer;
import com.github.wnebyte.minecraft.util.*;

public class Chunk {

    /*
    ###########################
    #        UTILITIES        #
    ###########################
    */

    public enum State {
        UNLOADED,
        LOADED,
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

    public static Vector2i toChunkCoords(Vector3f pos) {
        int x = (int)Math.floor(pos.x / Chunk.WIDTH);
        int z = (int)Math.floor(pos.z / Chunk.DEPTH);
        return new Vector2i(x, z);
    }

    public static Vector3f toWorldCoords(Vector2i chunkCoords) {
        return toWorldCoords(chunkCoords, 0);
    }

    public static Vector3f toWorldCoords(Vector2i chunkCoords, float y) {
        float x = chunkCoords.x * Chunk.WIDTH;
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

    /*
    ###########################
    #      STATIC FIELDS      #
    ###########################
    */

    public static final int WIDTH = 16;

    public static final int HEIGHT = 256;

    public static final int DEPTH = 16;

    public static final int SIZE = WIDTH * HEIGHT * DEPTH;

    private static final float MIN_BIOME_HEIGHT = 55.0f;

    private static final float MAX_BIOME_HEIGHT = 145.0f;

    private static final int OCEAN_LEVEL = 85;

    //   v4 ----------- v5
    //   /|            /|      Axis orientation
    //  / |           / |
    // v0 --------- v1  |      y
    // |  |         |   |      |
    // |  v6 -------|-- v7     +--- x
    // | /          |  /      /
    // |/           | /      z
    // v2 --------- v3

    private static final Formatter<Chunk> FILENAME_FORMATTER = chunk ->
            String.format("chunk-%d-%d", chunk.chunkCoordX, chunk.chunkCoordZ);

    /*
    ###########################
    #          FIELDS         #
    ###########################
    */

    private final byte[] data;

    // Global map position of this chunk
    private final int chunkPosX, chunkPosY, chunkPosZ;

    private final Vector3f chunkPos;

    // Relative position of this chunk
    private final int chunkCoordX, chunkCoordZ;

    private final Vector2i chunkCoords;

    private final Map map;

    private final Pool<Key, Subchunk> subchunks;

    private final AtomicReference<State> state;

    private final String path;

    private final TerrainGenerator generator;

    private final Random rand;

    // References to neighbouring chunks
    private volatile Chunk cXN, cXP, cZN, cZP, cYN, cYP;

    /*
    ###########################
    #       CONSTRUCTORS      #
    ###########################
    */

    public Chunk(int i, int j, int k, Map map, Pool<Key, Subchunk> subchunks) {
        this.chunkPosX = i * Chunk.WIDTH;
        this.chunkPosY = j * Chunk.HEIGHT;
        this.chunkPosZ = k * Chunk.DEPTH;
        this.chunkPos = new Vector3f(chunkPosX, chunkPosY, chunkPosZ);
        this.chunkCoordX = i;
        this.chunkCoordZ = k;
        this.chunkCoords = new Vector2i(i, k);
        this.map = map;
        this.subchunks = subchunks;
        this.state = new AtomicReference<>(State.UNLOADED);
        this.path = Assets.DIR + "/data/world/" + FILENAME_FORMATTER.format(this);
        this.generator = TerrainGenerator.getInstance();
        this.rand = new Random(this.generator.getSeed());
        this.data = new byte[Chunk.SIZE];
    }

    /*
    ###########################
    #         METHODS         #
    ###########################
    */

    public Block getBlock(int index) {
        byte id = data[index];
        return BlockMap.getBlock(id);
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
        return getBlock(index);
    }

    public Block getBlock(Vector3i ivec3) {
        return getBlock(ivec3.x, ivec3.y, ivec3.z);
    }

    public Block getBlock(Vector3f vec3f) {
        Vector3i ivec3 = Chunk.world2Index3D(vec3f, chunkCoords);
        return getBlock(ivec3);
    }

    public void setBlock(Block b, int i, int j, int k) {
        setBlock(b, i, j, k, false);
    }

    public void setBlock(Block b, int i, int j, int k, boolean remesh) {
        int index = toIndex(i, j, k);
        data[index] = b.getId();

        if (remesh) {
            int subchunkLevel = j / 16;
            mesh(subchunkLevel);

            // YN(-)
            if (j % 16 == 0 && subchunkLevel != 0) {
                mesh(subchunkLevel - 1);
            }
            // YP(+)
            else if ((j + 1) % 16 == 0 && subchunkLevel != 15) {
                mesh(subchunkLevel + 1);
            }
            // XN(-)
            if (i == 0 && cXN != null) {
                cXN.mesh(subchunkLevel);
            }
            // XP(+)
            else if (i == Chunk.WIDTH - 1 && cXP != null) {
                cXP.mesh(subchunkLevel);
            }
            // ZN(-)
            if (k == 0 && cZN != null) {
                cZN.mesh(subchunkLevel);
            }
            // ZP(+)
            else if (k == Chunk.DEPTH - 1 && cZP != null) {
                cZP.mesh(subchunkLevel);
            }

        }
    }

    public void generateTerrain() {
        for (int z = 0; z < Chunk.DEPTH; z++) {
            for (int x = 0; x < Chunk.WIDTH; x++) {
                int maxHeight = generator.getHeight(x + chunkPosX, z + chunkPosZ, MIN_BIOME_HEIGHT, MAX_BIOME_HEIGHT);
                int stoneHeight = maxHeight - 3;

                for (int y = 0; y < Chunk.HEIGHT; y++) {
                    if (y == 0) {
                        // 7, aka BEDROCK
                        setBlock(BlockMap.getBlock(7), x, y, z);
                    }
                    else if (y < stoneHeight) {
                        // 6, aka STONE
                        setBlock(BlockMap.getBlock(6), x, y, z);
                    }
                    else if (y < maxHeight) {
                        // 4, aka DIRT
                        setBlock(BlockMap.getBlock(4), x, y, z);
                    }
                    else if (y == maxHeight) {
                        if (maxHeight < OCEAN_LEVEL + 2) {
                            // 3, aka SAND
                            setBlock(BlockMap.getBlock(3), x, y, z);
                        }
                        else {
                            // 2, aka GRASS
                            setBlock(BlockMap.getBlock(2), x, y, z);
                        }
                    }
                    else if (y >= MIN_BIOME_HEIGHT && y < OCEAN_LEVEL) {
                        // 19, aka WATER
                        setBlock(BlockMap.getBlock(19), x, y, z);
                    }
                    else {
                        // 1, aka AIR
                        setBlock(BlockMap.getBlock(1), x, y, z);
                    }
                }
            }
        }
    }

    private void generateDecorations() {
        for (int z = 0; z < Chunk.DEPTH; z++) {
            for (int x = 0; x < Chunk.WIDTH; x++) {
                int num = (rand.nextInt(Short.MAX_VALUE) % 100);
                boolean generateTree = num > 98;

                if (generateTree) {
                    int y = generator.getHeight(x + chunkPosX, z + chunkPosZ, MIN_BIOME_HEIGHT, MAX_BIOME_HEIGHT) + 1;

                    if (y > OCEAN_LEVEL + 2) {
                        // generate a tree
                        int treeHeight = (rand.nextInt(Short.MAX_VALUE) % 3) + 3;
                        int leavesBottomY = JMath.clamp(treeHeight - 3, 3, Chunk.HEIGHT - 1);
                        int leavesTopY = treeHeight + 1;
                        if (generateTree && (y + 1 + leavesTopY < Chunk.HEIGHT)) {
                            for (int treeY = 0; treeY <= treeHeight; treeY++) {
                                // OAK LOG
                                setBlock(BlockMap.getBlock(8), x, treeY + y, z);
                            }

                            int ringLevel = 0;
                            for (int leavesY = leavesBottomY + y; leavesY <= leavesTopY + y; leavesY++) {
                                int leafRadius = (leavesY == leavesTopY) ? 2 : 1;

                                for (int leavesZ = z - leafRadius; leavesZ <= z + leafRadius; leavesZ++) {
                                    for (int leavesX = x - leafRadius; leavesX <= x + leafRadius; leavesX++) {
                                        if (JMath.inRange(leavesX, 0, Chunk.WIDTH) &&
                                                JMath.inRange(leavesZ, 0, Chunk.DEPTH)) {
                                            // OAK LEAVES
                                            setBlock(BlockMap.getBlock(9), leavesX, leavesY, leavesZ);
                                        }
                                        /*
                                        else if (leavesX < 0) {
                                            if (cXN != null) {
                                                cXN.setBlock(BlockMap.getBlock(9), Chunk.WIDTH - leavesX, leavesY, leavesZ);
                                            }
                                        }
                                        else if (leavesX >= Chunk.WIDTH) {
                                            if (cXP != null) {
                                                cXP.setBlock(BlockMap.getBlock(9), leavesX - Chunk.WIDTH, leavesY, leavesZ);
                                            }
                                        }
                                        else if (leavesZ < 0) {
                                            if (cZN != null) {
                                                cZN.setBlock(BlockMap.getBlock(9), leavesX, leavesY, Chunk.DEPTH - leavesZ);
                                            }
                                        }
                                        else if (leavesZ >= Chunk.DEPTH) {
                                            if (cZP != null) {
                                                cZP.setBlock(BlockMap.getBlock(9), leavesX, leavesY, leavesZ - Chunk.DEPTH);
                                            }
                                        }
                                         */
                                    }
                                }
                                ringLevel++;
                            }
                        }
                    }
                }
            }
        }
    }

    private void serialize() {
        Files.compress(path, data);
    }

    private void deserialize() {
        assert Files.exists(path) : "path does not exist";
        byte[] blocks = Files.decompress(path, Chunk.SIZE);
        if (blocks != null) {
            System.arraycopy(blocks, 0, data, 0, blocks.length);
        }
    }

    public void unload() {
        state.set(State.UNLOADED);
        serialize();
        unmesh();
    }

    public void load() {
        if (Files.exists(path)) {
            deserialize();
        } else {
            generateTerrain();
            generateDecorations();
        }
        state.set(State.LOADED);
    }

    public Future<?> unloadAsync() {
        return Application.submit(this::unload);
    }

    public Future<?> loadAsync() {
        return Application.submit(this::load);
    }

    public synchronized void unmesh() {
        for (int j = 0; j < 16; j++) {
            unmesh(j);
        }
    }

    public synchronized void unmesh(int subchunkLevel) {
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

    public synchronized void unmeshAsync() {
        Application.submit(this::unmesh);
    }

    public synchronized void unmeshAsync(int subchunkLevel) {
        Application.submit(() -> this.unmesh(subchunkLevel));
    }

    public synchronized void mesh() {
        updateNeighbourRefs();
        for (int j = 0; j < 16; j++) {
            mesh(j);
        }
    }

    public synchronized void mesh(int subchunkLevel) {
        Vector3i v = new Vector3i(chunkCoordX, subchunkLevel, chunkCoordZ);
        Key opaqueKey = new Key(v, false);
        Key transparentKey = new Key(v, true);
        Subchunk opaqueSubchunk = subchunks.get(opaqueKey);
        Subchunk transparentSubchunk = subchunks.get(transparentKey);
        assert (opaqueSubchunk != null && transparentSubchunk != null) : "buffer is null";
        opaqueSubchunk.setBlendable(false);
        opaqueSubchunk.setChunkCoords(chunkCoords);
        opaqueSubchunk.setSubchunkLevel(subchunkLevel);
        opaqueSubchunk.resetVertexBuffer();
        transparentSubchunk.setBlendable(true);
        transparentSubchunk.setChunkCoords(chunkCoords);
        transparentSubchunk.setSubchunkLevel(subchunkLevel);
        transparentSubchunk.resetVertexBuffer();
        int j = subchunkLevel * 16;
        int jMax = j + 16;
        int access;

        for (; j < jMax; j++) {
            for (int k = 0; k < Chunk.DEPTH; k++) {
                for (int i = 0; i < Chunk.WIDTH; i++) {
                    access = toIndex(i, j, k);
                    byte id = data[access];
                    Block b = BlockMap.getBlock(id);

                    if (Block.isAir(b)) { continue; }
                    createRenderData(b.isBlendable() ? transparentSubchunk.getVertexBuffer() : opaqueSubchunk.getVertexBuffer(),
                            b, i, j, k, access);
                }
            }
        }

        opaqueSubchunk.setState(Subchunk.State.MESHED);
        transparentSubchunk.setState(Subchunk.State.MESHED);
    }

    public synchronized void meshAsync() {
        Application.submit(this::mesh);
    }

    public synchronized void meshAsync(int subchunkLevel) {
        Application.submit(() -> this.mesh(subchunkLevel));
    }

    private void createRenderData(VertexBuffer buffer, Block b, int i, int j, int k, int access) {
        // Left face (X-)
        if (visibleFaceXN(b, i-1, j, k)) {
            appendFace(buffer, b, access, FaceType.LEFT);
        }
        // Right face (X+)
        if (visibleFaceXP(b, i+1, j, k)) {
            appendFace(buffer, b, access, FaceType.RIGHT);
        }
        // Back face (Z-)
        if (visibleFaceZN(b, i, j, k-1)) {
            appendFace(buffer, b, access, FaceType.BACK);
        }
        // Front face (Z+)
        if (visibleFaceZP(b, i, j, k+1)) {
            appendFace(buffer, b, access, FaceType.FRONT);
        }
        // Bottom face (Y-)
        if (visibleFaceYN(b, i, j-1, k)) {
            appendFace(buffer, b, access, FaceType.BOTTOM);
        }
        // Top face (Y+)
        if (visibleFaceYP(b, i, j+1, k)) {
            appendFace(buffer, b, access, FaceType.TOP);
        }
    }

    private void appendFace(VertexBuffer buffer, Block b, int access, FaceType face) {
        buffer.append(access, b.getTexCoordsIndex(face), (byte)face.ordinal(), b.getColorByBiome(face));
    }

    private boolean visibleFaceXN(Block b, int i, int j, int k) {
        if (i < 0) {
            if (cXN == null || cXN.isUnloaded()) {
                return true;
            } else {
                int index = toIndex(Chunk.WIDTH - 1, j, k);
                Block n = cXN.getBlock(index);
                return compare(b, n);
            }
        } else {
            int index = toIndex(i, j, k);
            Block n = getBlock(index);
            return compare(b, n);
        }
    }

    private boolean visibleFaceXP(Block b, int i, int j, int k) {
        if (i >= Chunk.WIDTH) {
            if (cXP == null || cXP.isUnloaded()) {
                return true;
            } else {
                int index = toIndex(0, j, k);
                Block n = cXP.getBlock(index);
                return compare(b, n);
            }
        } else {
            int index = toIndex(i, j, k);
            Block n = getBlock(index);
            return compare(b, n);
        }
    }

    private boolean visibleFaceYN(Block b, int i, int j, int k) {
        if (j < 0) {
            if (cYN == null || cYN.isUnloaded()) {
                return true;
            } else {
                int index = toIndex(i, Chunk.HEIGHT - 1, k);
                Block n = cYN.getBlock(index);
                return compare(b, n);
            }
        } else {
            int index = toIndex(i, j, k);
            Block n = getBlock(index);
            return compare(b, n);
        }
    }

    private boolean visibleFaceYP(Block b, int i, int j, int k) {
        if (j >= Chunk.HEIGHT) {
            if (cYP == null || cYP.isUnloaded()) {
                return true;
            } else {
                int index = toIndex(i, 0, k);
                Block n = cYP.getBlock(index);
                return compare(b, n);
            }
        } else {
            int index = toIndex(i, j, k);
            Block n = getBlock(index);
            return compare(b, n);
        }
    }

    private boolean visibleFaceZN(Block b, int i, int j, int k) {
        if (k < 0) {
            if (cZN == null || cZN.isUnloaded()) {
                return true;
            } else {
                int index = toIndex(i, j, Chunk.DEPTH - 1);
                Block n = cZN.getBlock(index);
                return compare(b, n);
            }
        } else {
            int index = toIndex(i, j, k);
            Block n = getBlock(index);
            return compare(b, n);
        }
    }

    private boolean visibleFaceZP(Block b, int i, int j, int k) {
        if (k >= Chunk.DEPTH) {
            if (cZP == null || cZP.isUnloaded()) {
                return true;
            } else {
                int index = toIndex(i, j, 0);
                Block n = cZP.getBlock(index);
                return compare(b, n);
            }
        } else {
            int index = toIndex(i, j, k);
            Block n = getBlock(index);
            return compare(b, n);
        }
    }

    private boolean compare(Block a, Block b) {
        if (Block.isAir(b)) {
            return true;
        } else if (a.isSolid() && b.isTransparent()) {
            return true;
        } else {
            return false;
        }
    }

    public void updateNeighbourRefs() {
        cXN = map.getChunk(chunkCoordX - 1, chunkCoordZ);
        cXP = map.getChunk(chunkCoordX + 1, chunkCoordZ);
        cZN = map.getChunk(chunkCoordX, chunkCoordZ - 1);
        cZP = map.getChunk(chunkCoordX, chunkCoordZ + 1);
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
        updateNeighbourRefs();
        return new Chunk[]{ cXN, cXP, cZN, cZP };
    }

    public boolean isUnloaded() {
        return (state.get() == State.UNLOADED);
    }

    public boolean isLoaded() {
        return (state.get() == State.LOADED);
    }

    public void setUnloaded() {
        state.set(State.UNLOADED);
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
