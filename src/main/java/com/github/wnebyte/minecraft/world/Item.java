package com.github.wnebyte.minecraft.world;

import com.github.wnebyte.minecraft.util.TextureFormat;

import java.util.Objects;

public class Item {

    private int id;

    private String name;

    private int maxStackCount;

    private transient int stackCount;

    private transient TextureFormat textureFormat;

    private transient boolean isBlock;

    public Item(int id, String name, int maxStackCount) {
        this.id = id;
        this.name = name;
        this.maxStackCount = maxStackCount;
        this.stackCount = 1;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getMaxStackCount() {
        return maxStackCount;
    }

    public TextureFormat getTextureFormat() {
        return textureFormat;
    }

    public void setTextureFormat(TextureFormat textureFormat) {
        this.textureFormat = textureFormat;
    }

    public boolean isBlock() {
        return isBlock;
    }

    public void setIsBlock(boolean block) {
        this.isBlock = block;
    }

    public int getStackCount() {
        return stackCount;
    }

    public void setStackCount(int stackCount) {
        this.stackCount = stackCount;
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
