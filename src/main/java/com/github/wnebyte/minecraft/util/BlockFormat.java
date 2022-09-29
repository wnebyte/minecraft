package com.github.wnebyte.minecraft.util;

import java.util.Objects;

public class BlockFormat {

    private int id;

    private String name;

    private TextureFormat sideTextureFormat;

    private TextureFormat topTextureFormat;

    private TextureFormat bottomTextureFormat;

    public BlockFormat(int id, String name,
            TextureFormat sideTextureFormat, TextureFormat topTextureFormat, TextureFormat bottomTextureFormat) {
        this.id = id;
        this.name = name;
        this.sideTextureFormat = sideTextureFormat;
        this.topTextureFormat = topTextureFormat;
        this.bottomTextureFormat = bottomTextureFormat;
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
}
