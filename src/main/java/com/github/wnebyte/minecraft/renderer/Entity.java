package com.github.wnebyte.minecraft.renderer;

public interface Entity {

    boolean isDirty();

    void setDirty();

    void setClean();
}
