package com.github.wnebyte.minecraft.physics.components;

import org.joml.Vector3f;
import com.github.wnebyte.minecraft.core.Component;

public class BoxCollider extends Component {

    private Vector3f size;

    private Vector3f offset;

    public BoxCollider() {
        this(new Vector3f(), new Vector3f());
    }

    public BoxCollider(Vector3f size) {
        this(size, new Vector3f());
    }

    public BoxCollider(Vector3f size, Vector3f offset) {
        this.size = size;
        this.offset = offset;
    }

    public Vector3f getOffset() {
        return offset;
    }

    public void setOffset(Vector3f offset) {
        this.offset = offset;
    }

    public Vector3f getSize() {
        return size;
    }

    public void setSize(Vector3f size) {
        this.size = size;
    }
}
