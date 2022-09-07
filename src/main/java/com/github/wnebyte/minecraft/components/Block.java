package com.github.wnebyte.minecraft.components;

import java.util.Objects;
import com.github.wnebyte.minecraft.core.Component;
import com.github.wnebyte.minecraft.core.Transform;
import com.github.wnebyte.minecraft.renderer.Sprite;

// 8 x 8 cube
// The 8 vertices will look like this:
//   v4 ----------- v5
//   /|            /|      Axis orientation
//  / |           / |
// v0 --------- v1  |      y
// |  |         |   |      |
// |  v6 -------|-- v7     +--- x
// | /          |  /      /
// |/           | /      z
// v2 --------- v3
public class Block extends Component {

    private Transform transform;

    private Sprite mesh;

    private Sprite normal;

    private boolean dirty;

    public Block() {
        this(null, null, null);
    }

    public Block(Transform transform) {
        this(transform, null, null);
    }

    public Block(Transform transform, Sprite mesh) {
        this(transform, mesh, null);
    }

    public Block(Transform transform, Sprite mesh, Sprite normal) {
        this.transform = transform;
        this.mesh = mesh;
        this.normal = normal;
        this.dirty = true;
    }

    public Transform getTransform() {
        return transform;
    }

    public void setTransform(Transform transform) {
        this.transform = transform;
        setDirty();
    }

    public Sprite getMesh() {
        return mesh;
    }

    public void setMesh(Sprite mesh) {
        this.mesh = mesh;
    }

    public Sprite getNormal() {
        return normal;
    }

    public void setNormal(Sprite normal) {
        this.normal = normal;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty() {
        this.dirty = true;
    }

    public void setClean() {
        this.dirty = false;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof Block)) return false;
        Block block = (Block) o;
        return Objects.equals(block.transform, this.transform) &&
                Objects.equals(block.mesh, this.mesh) &&
                Objects.equals(block.normal, this.normal) &&
                Objects.equals(block.dirty, this.dirty) &&
                super.equals(block);
    }

    @Override
    public int hashCode() {
        int result = 5;
        return 2 *
                result +
                Objects.hashCode(this.transform) +
                Objects.hashCode(this.mesh) +
                Objects.hashCode(this.normal) +
                Objects.hashCode(this.dirty) +
                super.hashCode();
    }
}
