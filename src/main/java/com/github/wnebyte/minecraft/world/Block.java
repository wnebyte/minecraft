package com.github.wnebyte.minecraft.world;

import java.util.Objects;

public class Block {

    public static boolean isAir(Block block) {
        return (block == null || block.id == Block.AIR.id);
    }

    public static final Block
            AIR                = new Block(1,  false, true,  false),
            GRASS              = new Block(2,  true,  false, false),
            SAND               = new Block(3,  true,  false, false),
            DIRT               = new Block(4,  true,  false, false),
            GREEN_CONCRETE     = new Block(5,  true,  false, false),
            STONE              = new Block(6,  true,  false, false),
            BEDROCK            = new Block(7,  true,  false, false),
            OAK_LOG            = new Block(8,  true,  false, false),
            OAK_LEAVES         = new Block(9,  true,  true,  true),
            OAK_PLANKS         = new Block(10, true,  false, false),
            GLOWSTONE          = new Block(11, true,  false, false),
            COBBLESTONE        = new Block(12, true,  false, false),
            SPRUCE_LOG         = new Block(13, true,  false, false),
            SPRUCE_PLANKS      = new Block(14, true,  false, false),
            GLASS              = new Block(15, true,  true,  true),
            SEA_LATERN         = new Block(16, true,  false, false),
            BIRCH_LOG          = new Block(17, true,  false, false),
            BLUE_STAINED_GLASS = new Block(18, true,  true,  true),
            WATER              = new Block(19, false, true,  true),
            BIRCH_PLANKS       = new Block(20, true,  false, false),
            DIAMOND_BLOCKS     = new Block(21, true,  false, false),
            OBSIDIAN           = new Block(22, true,  false, false),
            CRYING_OBSIDIAN    = new Block(23, true,  false, false),
            DARK_OAK_LOG       = new Block(24, true,  false, false),
            DARK_OAK_PLANKS    = new Block(25, true,  false, false),
            JUNGLE_LOG         = new Block(26, true,  false, false),
            JUNGLE_PLANKS      = new Block(27, true,  false, false),
            ACACIA_LOG         = new Block(28, true,  false, false),
            ACACIA_PLANKS      = new Block(29, true,  false, false);

    public final byte id;

    private boolean isSolid;

    private boolean isTransparent;

    private boolean isBlendable;

    public Block(int id, boolean isSolid, boolean isTransparent, boolean isBlendable) {
        this.id = (byte)id;
        this.isSolid = isSolid;
        this.isTransparent = isTransparent;
        this.isBlendable = isBlendable;
    }

    public byte getId() {
        return id;
    }

    public boolean isSolid() {
        return isSolid;
    }

    public boolean isTransparent() {
        return isTransparent;
    }

    public boolean isBlendable() {
        return isBlendable;
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
