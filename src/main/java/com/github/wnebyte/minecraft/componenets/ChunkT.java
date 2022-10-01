package com.github.wnebyte.minecraft.componenets;

import com.github.wnebyte.minecraft.renderer.VertexBuffer;
import com.github.wnebyte.minecraft.util.ChunkHelper;
import com.github.wnebyte.minecraft.world.Block;
import com.github.wnebyte.minecraft.world.Map;

public class ChunkT {

    private static final int EMPTY = 0;
    private static final int CHUNK_SIZE = 32;
    private static final int CHUNK_SIZE_SQUARED = 1024;
    private static final int CHUNK_SIZE_CUBED = 32768;
    private static final int CHUNK_SIZE_MINUS_ONE = 31;
    private static final int CHUNK_SIZE_SHIFTED = 32 << 6;

    public Block[] data = new Block[CHUNK_SIZE_CUBED];

    public VertexBuffer vertexBuffer;

    // Parent reference to access blocks in other chunks
    public Map m;

    // The position of this chunk in the chunk grid.
    // Maps are usually 16 chunks wide, 16 chunks long and 6 chunks tall
    public int chunkPosX, chunkPosY, chunkPosZ;

    // Height maps
    public byte[] MinY = new byte[CHUNK_SIZE_SQUARED];
    public byte[] MaxY = new byte[CHUNK_SIZE_SQUARED];

    ChunkHelper chunkHelper;
    ChunkT cXN, cXP, cYN, cYP, cZN, cZP;

    public ChunkT(Map map, int x, int y, int z)
    {
        m = map;
        chunkPosX = x;
        chunkPosY = y;
        chunkPosZ = z;

        // Set min defaults to 32
        for (int i = CHUNK_SIZE_SQUARED - 1; i >= 0; i--)
            MinY[i] = (byte)CHUNK_SIZE;

        chunkHelper = new ChunkHelper();
      //  vertexBuffer = new VertexBuffer();
    }

    public void addBlock(int i, int j, int k, Block b) {
        data[j + i * CHUNK_SIZE + k * CHUNK_SIZE_SQUARED] = b;
    }

    public void generateMesh()
    {
        // Default 4096, else use the lase size + 1024
        int newSize = vertexBuffer.size() == 0 ? 4096 : vertexBuffer.size() + 1024;
       // vertexBuffer.reset(newSize);

        // Negative X side
       // cXN = m.get(chunkPosX - 1, chunkPosY, chunkPosZ);

        // Positive X side
       // cXP = m.get(chunkPosX + 1, chunkPosY, chunkPosZ);

        // Negative Y side
       // cYN = m.get(chunkPosX, chunkPosY - 1, chunkPosZ);

        // Positive Y side
       // cYP = m.get(chunkPosX, chunkPosY + 1, chunkPosZ);

        // Negative Z neighbour
       // cZN = m.get(chunkPosX, chunkPosY, chunkPosZ - 1);

        // Positive Z side
       // cZP = m.get(chunkPosX, chunkPosY, chunkPosZ + 1);

        // Precalculate the map-relative Y position of the chunk in the map
        int chunkY = chunkPosY * CHUNK_SIZE;

        // Allocate variables on the stack
        int access, heightMapAccess, iCS, kCS2, i1, k1, j, topJ;
        boolean minXEdge, maxXEdge, minZEdge, maxZEdge;

        k1 = 1;

        for (int k = 0; k < CHUNK_SIZE; k++, k1++)
        {
            // Calculate this once, rather than multiple times in the inner loop
            kCS2 = k * CHUNK_SIZE_SQUARED;

            i1 = 1;
            heightMapAccess = k * CHUNK_SIZE;

            // Is the current run on the Z- or Z+ edge of the chunk
            minZEdge = k == 0;
            maxZEdge = k == CHUNK_SIZE_MINUS_ONE;

            for (int i = 0; i < CHUNK_SIZE; i++, i1++)
            {
                // Determine where to start the innermost loop
                j = MinY[heightMapAccess];
                topJ = MaxY[heightMapAccess];
                heightMapAccess++;

                // Calculate this once, rather than multiple times in the inner loop
                iCS = i * CHUNK_SIZE;

                // Calculate access here and increment it each time in the innermost loop
                access = kCS2 + iCS + j;

                // Is the current run on the X- or X+ edge of the chunk
                boolean minX = i == 0;
                boolean maxX = i == CHUNK_SIZE_MINUS_ONE;

                // X and Z runs search upwards to create runs, so start at the bottom.
                for (; j < topJ; j++, access++)
                {
                    Block b = data[access];

                    if (b.id != EMPTY)
                    {
                        createRun(b, i, j, k << 12, i1, k1 << 12, j + chunkY, access,
                                minX, maxX, j == 0, j == CHUNK_SIZE_MINUS_ONE, minZEdge, maxZEdge, iCS, kCS2);
                    }
                }

                // Extend the array if it is nearly full
                /*
                if (vertexBuffer.remaining() < 2048)
                    vertexBuffer.extend(2048);
                 */
            }
        }
    }

    void createRun(Block b, int i, int j, int k, int i1, int k1, int y, int access,
                   boolean minX, boolean maxX, boolean minY, boolean maxY, boolean minZ, boolean maxZ, int iCS, int kCS2)
    {
        int textureHealth16 = 0;
        int runFinish;
        int accessIncremented = access + 1;
        int chunkAccess;
        int j1 = j + 1;
        int jS = j << 6;
        int jS1 = j1 << 6;
        int length;

        // Left (X-)
        if (!chunkHelper.visitedXN[access] && drawFaceXN(j, access, minX, kCS2))
        {
            chunkHelper.visitedXN[access] = true;
            chunkAccess = accessIncremented;

            for (length = jS1; length < CHUNK_SIZE_SHIFTED; length += (1 << 6))
            {
                if (differentBlock(chunkAccess, b))
                    break;

                chunkHelper.visitedXN[chunkAccess++] = true;
            }

            // k1 and k are already shifted
            BlockVertex.AppendQuadX(vertexBuffer, i, jS, length, k1, k, (int)0, textureHealth16);
        }

        // Right (X+)
        if (!chunkHelper.visitedXP[access] && drawFaceXP(j, access, maxX, kCS2))
        {
            chunkHelper.visitedXP[access] = true;

            chunkAccess = accessIncremented;

            for (length = jS1; length < CHUNK_SIZE_SHIFTED; length += (1 << 6))
            {
                if (differentBlock(chunkAccess, b))
                    break;

                chunkHelper.visitedXP[chunkAccess++] = true;
            }

            BlockVertex.AppendQuadX(vertexBuffer, i1, jS, length, k, k1, (int)0, textureHealth16);
        }

        // Back (Z-)
        if (!chunkHelper.visitedZN[access] && drawFaceZN(j, access, minZ, iCS))
        {
            chunkHelper.visitedZN[access] = true;

            chunkAccess = accessIncremented;

            for (length = jS1; length < CHUNK_SIZE_SHIFTED; length += (1 << 6))
            {
                if (differentBlock(chunkAccess, b))
                    break;

                chunkHelper.visitedZN[chunkAccess++] = true;
            }

            BlockVertex.AppendQuadZ(vertexBuffer, i1, i, jS, length, k, (int)0, textureHealth16);
        }

        // Front (Z+)
        if (!chunkHelper.visitedZP[access] && drawFaceZP(j, access, maxZ, iCS))
        {
            chunkHelper.visitedZP[access] = true;

            chunkAccess = accessIncremented;

            for (length = jS1; length < CHUNK_SIZE_SHIFTED; length += (1 << 6))
            {
                if (differentBlock(chunkAccess, b))
                    break;

                chunkHelper.visitedZP[chunkAccess++] = true;
            }

            BlockVertex.AppendQuadZ(vertexBuffer, i, i1, jS, length, k1, (int)0, textureHealth16);
        }

        // Bottom (Y-)
        if (y > 0 && !chunkHelper.visitedYN[access] && drawFaceYN(access, minY, iCS, kCS2))
        {
            chunkHelper.visitedYN[access] = true;

            chunkAccess = access + CHUNK_SIZE;

            for (length = i1; length < CHUNK_SIZE; length++)
            {
                if (differentBlock(chunkAccess, b))
                    break;

                chunkHelper.visitedYN[chunkAccess] = true;

                chunkAccess += CHUNK_SIZE;
            }

            BlockVertex.AppendQuadY(vertexBuffer, i, length, jS, k1, k, (int)0, textureHealth16);
        }

        // Top (Y+)
        if (!chunkHelper.visitedYP[access] && drawFaceYP(access, maxY, iCS, kCS2))
        {
            chunkHelper.visitedYP[access] = true;

            chunkAccess = access + CHUNK_SIZE;

            for (length = i1; length < CHUNK_SIZE; length++)
            {
                if (differentBlock(chunkAccess, b))
                    break;

                chunkHelper.visitedYP[chunkAccess] = true;

                chunkAccess += CHUNK_SIZE;
            }

            BlockVertex.AppendQuadY(vertexBuffer, i, length, jS1, k, k1, (int)0, textureHealth16);
        }
    }

    protected boolean drawFaceXN(int j, int access, boolean min, int kCS2)
    {
        if (min)
        {
            if (chunkPosX == 0)
                return false;

            if (cXN == null)
                return true;

            // If it is outside this chunk, get the block from the neighbouring chunk
            return cXN.data[31 * CHUNK_SIZE + j + kCS2].id == 0;
        }

        return data[access - CHUNK_SIZE].id == 0;
    }

    protected boolean drawFaceXP(int j, int access, boolean max, int kCS2)
    {
        if (max)
        {
            if (chunkPosX == 32 - 1)
                return false;

            if (cXP == null)
                return true;

            // If it is outside this chunk, get the block from the neighbouring chunk
            return cXP.data[j + kCS2].id == 0;
        }

        return data[access + CHUNK_SIZE].id == 0;
    }

    protected boolean drawFaceYN(int access, boolean min, int iCS, int kCS2)
    {
        if (min)
        {
            if (chunkPosY == 0)
                return false;

            if (cYN == null)
                return true;

            // If it is outside this chunk, get the block from the neighbouring chunk
            return cYN.data[iCS + 31 + kCS2].id == 0;
        }

        return data[access - 1].id == 0;
    }

    protected boolean drawFaceYP(int access, boolean max, int iCS, int kCS2)
    {
        if (max)
        {
            // Don't check chunkYPos here as players can move above the map

            if (cYP == null)
                return true;

            return cYP.data[iCS + kCS2].id == 0;
        }

        return data[access + 1].id == 0;
    }

    protected boolean drawFaceZN(int j, int access, boolean min, int iCS)
    {
        if (min)
        {
            if (chunkPosZ == 0)
                return false;

            if (cZN == null)
                return true;

            return cZN.data[iCS + j + 31 * CHUNK_SIZE_SQUARED].id == 0;
        }

        return data[access - CHUNK_SIZE_SQUARED].id == 0;
    }

    protected boolean drawFaceZP(int j, int access, boolean max, int iCS)
    {
        if (max)
        {
            if (chunkPosZ == 32 - 1)
                return false;

            if (cZP == null)
                return true;

            return cZP.data[iCS + j].id == 0;
        }

        return data[access + CHUNK_SIZE_SQUARED].id == 0;
    }

    protected boolean differentBlock(int chunkAccess, Block compare)
    {
        Block b = data[chunkAccess];
        return b.id != compare.id;
    }
}
