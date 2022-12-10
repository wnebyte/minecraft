package world;

import org.junit.Test;
import org.junit.Assert;
import org.joml.Vector2i;
import org.joml.Vector3f;
import com.github.wnebyte.minecraft.world.Chunk;

public class ChunkTest {

    @Test
    public void toPositiveChunkCoordsTest00() {
        // 0, 0, 0
        Vector3f pos = new Vector3f(0.9f, 0, 0.9f);
        Vector2i cc = Chunk.toChunkCoords(pos);
        Assert.assertEquals(0, cc.x);
        Assert.assertEquals(0, cc.y);
        // 15, 0, 15
        pos = new Vector3f(15.9f, 0, 15.9f);
        cc = Chunk.toChunkCoords(pos);
        Assert.assertEquals(0, cc.x);
        Assert.assertEquals(0, cc.y);
        // 30, 0, 0
        pos = new Vector3f(30.5f, 0, 0);
        cc = Chunk.toChunkCoords(pos);
        Assert.assertEquals(1, cc.x);
        Assert.assertEquals(0, cc.y);
    }

    @Test
    public void toNegativeChunkCoordsTest00() {
        // -1, 0, -1
        Vector3f pos = new Vector3f(-1, 0, -1);
        Vector2i cc = Chunk.toChunkCoords(pos);
        Assert.assertEquals(-1, cc.x);
        Assert.assertEquals(-1, cc.y);

        // -45, 0, 0
        pos = new Vector3f(-45, 0, 0);
        cc = Chunk.toChunkCoords(pos);
        Assert.assertEquals(-3, cc.x);
        Assert.assertEquals(0,  cc.y);
    }

    @Test
    public void toPositiveWorldCoordsTest00() {
        // 0, 0
        Vector2i cc = new Vector2i(0, 0);
        Vector3f pos = Chunk.toWorldCoords(cc);
        Assert.assertEquals(0f, pos.x, 0f);
        Assert.assertEquals(0f, pos.z, 0f);
        // 2, 2
        cc = new Vector2i(2, 2);
        pos = Chunk.toWorldCoords(cc);
        Assert.assertEquals(32.0f, pos.x, 0f);
        Assert.assertEquals(32.0f, pos.z, 0f);
    }

    @Test
    public void index2WorldTest00() {
        // cc: 0, index: 0
        Vector2i cc = new Vector2i(0, 0);
        int index = 0;
        Vector3f pos = Chunk.index2World(index, cc);
        Assert.assertEquals(0.0f, pos.x, 0.0f);
        Assert.assertEquals(0.0f, pos.y, 0.0f);
        Assert.assertEquals(0.0f, pos.z, 0.0f);
        // cc: 0, index: 1
        index = 1;
        pos = Chunk.index2World(index, cc);
        Assert.assertEquals(1.0f, pos.x, 0.0f);
        Assert.assertEquals(0.0f, pos.y, 0.0f);
        Assert.assertEquals(0.0f, pos.z, 0.0f);
        // cc: 0, index: 15
        index = 15;
        pos = Chunk.index2World(index, cc);
        Assert.assertEquals(15.0f, pos.x, 0.0f);
        Assert.assertEquals(0.0f,  pos.y, 0.0f);
        Assert.assertEquals(0.0f,  pos.z, 0.0f);
        // cc: 0, index: 16
        index = 16;
        pos = Chunk.index2World(index, cc);
        Assert.assertEquals(0.0f, pos.x, 0.0f);
        Assert.assertEquals(1.0f, pos.y, 0.0f);
        Assert.assertEquals(0.0f, pos.z, 0.0f);
    }
}
