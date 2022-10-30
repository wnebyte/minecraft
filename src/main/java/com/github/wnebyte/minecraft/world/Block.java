package com.github.wnebyte.minecraft.world;

import java.util.Objects;
import com.github.wnebyte.minecraft.util.TextureFormat;

public class Block {

    /*
    ###########################
    #        UTILITIES        #
    ###########################
    */

    public static boolean isAir(Block block) {
        return (block == null || block.id == 1);
    }

    private byte id;

    private String name;

    private TextureFormat sideTextureFormat;

    private TextureFormat topTextureFormat;

    private TextureFormat bottomTextureFormat;

    private boolean solid;

    private boolean transparent;

    private boolean blendable;

    private boolean colorSideByBiome;

    private boolean colorTopByBiome;

    private boolean colorBottomByBiome;

    public Block(byte id, String name,
                 TextureFormat sideTextureFormat, TextureFormat topTextureFormat, TextureFormat bottomTextureFormat,
                 boolean solid, boolean transparent, boolean blendable,
                 boolean colorSideByBiome, boolean colorTopByBiome, boolean colorBottomByBiome) {
        this.id = id;
        this.name = name;
        this.sideTextureFormat = sideTextureFormat;
        this.topTextureFormat = topTextureFormat;
        this.bottomTextureFormat = bottomTextureFormat;
        this.solid = solid;
        this.transparent = transparent;
        this.blendable = blendable;
        this.colorSideByBiome = colorSideByBiome;
        this.colorTopByBiome = colorTopByBiome;
        this.colorBottomByBiome = colorBottomByBiome;
    }

    public int getTexCoordsIndex(FaceType face) {
        switch (face) {
            case TOP:
                return getTopTextureFormat().getId();
            case BOTTOM:
                return getBottomTextureFormat().getId();
            default:
                return getSideTextureFormat().getId();
        }
    }

    public boolean getColorByBiome(FaceType face) {
        switch (face) {
            case TOP:
                return colorTopByBiome;
            case BOTTOM:
                return colorBottomByBiome;
            default:
                return colorSideByBiome;
        }
    }

    public byte getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public TextureFormat getSideTextureFormat() {
        return sideTextureFormat;
    }

    public TextureFormat getTopTextureFormat() {
        return topTextureFormat;
    }

    public TextureFormat getBottomTextureFormat() {
        return bottomTextureFormat;
    }

    public boolean isSolid() {
        return solid;
    }

    public boolean isTransparent() {
        return transparent;
    }

    public boolean isBlendable() {
        return blendable;
    }

    public boolean isColorSideByBiome() {
        return colorSideByBiome;
    }

    public boolean isColorTopByBiome() {
        return colorTopByBiome;
    }

    public boolean isColorBottomByBiome() {
        return colorBottomByBiome;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof Block)) return false;
        Block block = (Block) o;
        return Objects.equals(this.id, block.id);
    }

    @Override
    public int hashCode() {
        int result = 27;
        return 3 *
                result +
                Objects.hashCode(this.id);
    }

    @Override
    public String toString() {
        return String.format("Block[id: %d, name: %s, side: %s, top: %s, bottom: %s]", id, name,
                sideTextureFormat, topTextureFormat, bottomTextureFormat);
    }

    public static class Builder {

        private byte id;

        private String name;

        private TextureFormat sideTextureFormat, topTextureFormat, bottomTextureFormat;

        private boolean solid, transparent, blendable;

        private boolean colorSideByBiome, colorTopByBiome, colorBottomByBiome;

        public Builder setId(byte id) {
            this.id = id;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setSideTextureFormat(TextureFormat sideTextureFormat) {
            this.sideTextureFormat = sideTextureFormat;
            return this;
        }

        public Builder setTopTextureFormat(TextureFormat topTextureFormat) {
            this.topTextureFormat = topTextureFormat;
            return this;
        }

        public Builder setBottomTextureFormat(TextureFormat bottomTextureFormat) {
            this.bottomTextureFormat = bottomTextureFormat;
            return this;
        }

        public Builder setSolid(boolean value) {
            this.solid = value;
            return this;
        }

        public Builder setTransparent(boolean value) {
            this.transparent = value;
            return this;
        }

        public Builder setBlendable(boolean value) {
            this.blendable = value;
            return this;
        }

        public Builder setColorSideByBiome(boolean value) {
            this.colorSideByBiome = value;
            return this;
        }

        public Builder setColorTopByBiome(boolean value) {
            this.colorTopByBiome = value;
            return this;
        }

        public Builder setColorBottomByBiome(boolean value) {
            this.colorBottomByBiome = value;
            return this;
        }

        public Block build() {
            return new Block(id, name, sideTextureFormat, topTextureFormat, bottomTextureFormat,
                    solid, transparent, blendable, colorSideByBiome, colorTopByBiome, colorBottomByBiome);
        }
    }
}
