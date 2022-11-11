package com.github.wnebyte.minecraft.world;

import java.util.Objects;
import com.github.wnebyte.minecraft.renderer.TextureFormat;

/**
 * This class represents an unmodifiable item type.
 */
public class Item {

    public static class Builder {

        private short id;

        private String name;

        private short maxStackCount;

        private TextureFormat textureFormat;

        private boolean isBlock;

        public Builder setId(short id) {
            this.id = id;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setMaxStackCount(short maxStackCount) {
            this.maxStackCount = maxStackCount;
            return this;
        }

        public Builder setTextureFormat(TextureFormat textureFormat) {
            this.textureFormat = textureFormat;
            return this;
        }

        public Builder setIsBlock(boolean value) {
            this.isBlock = value;
            return this;
        }

        public Item build() {
            return new Item(id, name, maxStackCount, textureFormat, isBlock);
        }
    }

    private final short id;

    private final String name;

    private final short maxStackCount;

    private final TextureFormat textureFormat;

    private final boolean isBlock;

    public Item(short id, String name, short maxStackCount, TextureFormat textureFormat, boolean isBlock) {
        this.id = id;
        this.name = name;
        this.maxStackCount = maxStackCount;
        this.textureFormat = textureFormat;
        this.isBlock = isBlock;
    }

    public short getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public short getMaxStackCount() {
        return maxStackCount;
    }

    public TextureFormat getTextureFormat() {
        return textureFormat;
    }

    public boolean isBlock() {
        return isBlock;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof Item)) return false;
        Item item = (Item) o;
        return Objects.equals(item.id, this.id);
    }

    @Override
    public int hashCode() {
        int result = 13;
        return 2 * result + Objects.hashCode(this.id);

    }
}
