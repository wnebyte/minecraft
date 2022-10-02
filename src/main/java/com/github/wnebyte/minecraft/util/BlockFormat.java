package com.github.wnebyte.minecraft.util;

import java.util.Objects;

public class BlockFormat {

    private int id;

    private String name;

    private TextureFormat sideTextureFormat;

    private TextureFormat topTextureFormat;

    private TextureFormat bottomTextureFormat;

    private boolean isSolid;

    private boolean isTransparent;

    private boolean isBlendable;

    public BlockFormat(int id, String name,
            TextureFormat sideTextureFormat, TextureFormat topTextureFormat, TextureFormat bottomTextureFormat,
                       boolean isSolid, boolean isTransparent, boolean isBlendable) {
        this.id = id;
        this.name = name;
        this.sideTextureFormat = sideTextureFormat;
        this.topTextureFormat = topTextureFormat;
        this.bottomTextureFormat = bottomTextureFormat;
        this.isSolid = isSolid;
        this.isTransparent = isTransparent;
        this.isBlendable = isBlendable;
    }

    public int getId() {
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
        if (!(o instanceof BlockFormat)) return false;
        BlockFormat blockFormat = (BlockFormat) o;
        return Objects.equals(this.id, blockFormat.id) &&
                Objects.equals(this.name, blockFormat.name) &&
                Objects.equals(this.sideTextureFormat, blockFormat.sideTextureFormat) &&
                Objects.equals(this.topTextureFormat, blockFormat.topTextureFormat) &&
                Objects.equals(this.bottomTextureFormat, blockFormat.bottomTextureFormat);
    }

    @Override
    public int hashCode() {
        int result = 27;
        return 3 *
                result +
                Objects.hashCode(this.id) +
                Objects.hashCode(this.name) +
                Objects.hashCode(this.sideTextureFormat) +
                Objects.hashCode(this.topTextureFormat) +
                Objects.hashCode(this.bottomTextureFormat);
    }

    @Override
    public String toString() {
        return String.format("BlockFormat[id: %d, name: %s, side: %s, top: %s, bottom: %s]", id, name,
                sideTextureFormat, topTextureFormat, bottomTextureFormat);
    }

    public static class Builder {

        private int id;

        private String name;

        private TextureFormat sideTextureFormat, topTextureFormat, bottomTextureFormat;

        private boolean isSolid, isTransparent, isBlendable;

        public Builder setId(int id) {
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

        public Builder setIsSolid(boolean isSolid) {
            this.isSolid = isSolid;
            return this;
        }

        public Builder setIsTransparent(boolean isTransparent) {
            this.isTransparent = isTransparent;
            return this;
        }

        public Builder setIsBlendable(boolean isBlendable) {
            this.isBlendable = isBlendable;
            return this;
        }

        public BlockFormat build() {
            return new BlockFormat(id, name, sideTextureFormat, topTextureFormat, bottomTextureFormat,
                    isSolid, isTransparent, isBlendable);
        }
    }
}
