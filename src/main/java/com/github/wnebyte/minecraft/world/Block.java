package com.github.wnebyte.minecraft.world;

import java.util.Objects;

public class Block {

    public static Block newInstance(byte id) {
        return new Block(id);
    }

    public static Block copy(Block block) {
        return isAir(block) ? new Block(AIR_ID) : new Block(block.id);
    }

    public static boolean isAir(Block block) {
        return (block == null || block.id == AIR_ID);
    }

    public static final byte
            AIR_ID                = 1,
            GRASS_ID              = 2,
            SAND_ID               = 3,
            DIRT_ID               = 4,
            GREEN_CONCRETE_ID     = 5,
            STONE_ID              = 6,
            BEDROCK_ID            = 7,
            OAK_LOG_ID            = 8,
            OAK_LEAVES_ID         = 9,
            OAK_PLANKS_ID         = 10,
            GLOW_STONE_ID         = 11,
            COBBLESTONE_ID        = 12,
            SPRUCE_LOG_ID         = 13,
            SPRUCE_PLANKS_ID      = 14,
            GLASS_ID              = 15,
            SEA_LANTERN_ID        = 16,
            BIRCH_LOG_ID          = 17,
            BLUE_STAINED_CLASS_ID = 18,
            WATER_ID              = 19,
            BIRCH_PLANKS_ID       = 20,
            DIAMOND_BLOCK_ID      = 21,
            OBSIDIAN_ID           = 22,
            CRYING_OBSIDIAN_ID    = 23,
            DARK_OAK_LOG_ID       = 24,
            DARK_OAK_PLANKS_ID    = 25,
            JUNGLE_LOG_ID         = 26,
            JUNGLE_PLANKS_ID      = 27,
            ACACIA_LOG_ID         = 28,
            ACACIA_PLANKS_ID      = 29;

    public static final Block
            AIR                = new Block(AIR_ID),
            GRASS              = new Block(GRASS_ID),
            SAND               = new Block(SAND_ID),
            DIRT               = new Block(DIRT_ID),
            GREEN_CONCRETE     = new Block(GREEN_CONCRETE_ID),
            STONE              = new Block(STONE_ID),
            BEDROCK            = new Block(BEDROCK_ID),
            OAK_LOG            = new Block(OAK_LOG_ID),
            OAK_LEAVES         = new Block(OAK_LEAVES_ID),
            OAK_PLANKS         = new Block(OAK_PLANKS_ID),
            GLOWSTONE          = new Block(GLOW_STONE_ID),
            COBBLESTONE        = new Block(COBBLESTONE_ID),
            SPRUCE_LOG         = new Block(SPRUCE_LOG_ID),
            SPRUCE_PLANKS      = new Block(SPRUCE_PLANKS_ID),
            GLASS              = new Block(GLASS_ID),
            SEA_LATERN         = new Block(SEA_LANTERN_ID),
            BIRCH_LOG          = new Block(BIRCH_LOG_ID),
            BLUE_STAINED_GLASS = new Block(BLUE_STAINED_CLASS_ID),
            WATER              = new Block(WATER_ID),
            BIRCH_PLANKS       = new Block(BIRCH_PLANKS_ID),
            DIAMOND_BLOCKS     = new Block(DIAMOND_BLOCK_ID),
            OBSIDIAN           = new Block(OBSIDIAN_ID),
            CRYING_OBSIDIAN    = new Block(CRYING_OBSIDIAN_ID),
            DARK_OAK_LOG       = new Block(DARK_OAK_LOG_ID),
            DARK_OAK_PLANKS    = new Block(DARK_OAK_PLANKS_ID),
            JUNGLE_LOG         = new Block(JUNGLE_LOG_ID),
            JUNGLE_PLANKS      = new Block(JUNGLE_PLANKS_ID),
            ACACIA_LOG         = new Block(ACACIA_LOG_ID),
            ACACIA_PLANKS      = new Block(ACACIA_PLANKS_ID);

    public final byte id;

    public float x, y, z;

    public Block(int id) {
        this.id = (byte)id;
    }

    public Block(int id, float x, float y, float z) {
        this.id = (byte)id;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public byte getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof Block)) return false;
        Block block = (Block) o;
        return Objects.equals(block.id, this.id);
    }

    @Override
    public int hashCode() {
        int result = 5;
        return 2 *
                result +
                Objects.hashCode(this.id);
    }
}
