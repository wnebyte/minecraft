package com.github.wnebyte.minecraft.components;

import org.joml.Vector4f;
import com.github.wnebyte.minecraft.core.Component;

public class CubeRenderer extends Component {

    public void setDirty() {}

    public boolean isDirty() { return false; }

    public void setClean() {}

    public Vector4f getColor() { return null; }
}
