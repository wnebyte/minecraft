package com.github.wnebyte.minecraft.components;

import java.util.Objects;
import org.joml.Vector2f;
import com.github.wnebyte.minecraft.core.Component;
import com.github.wnebyte.minecraft.core.Transform;
import com.github.wnebyte.minecraft.renderer.Material;

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

    public Transform transform;

    private Material material;

    private boolean dirty;

    private Vector2f[] texCoords = new Vector2f[] {
            new Vector2f(1, 1), // TR
            new Vector2f(1, 0), // BR
            new Vector2f(0, 0), // BL
            new Vector2f(0, 1)  // TL
    };

    public Block(Transform transform) {
        this(transform, null);
    }

    public Block(Transform transform, Material material) {
        this.transform = transform;
        this.material = material;
        this.dirty = true;
    }

    public Vector2f getTexCoords(int index) {
        Vector2f[] uvx = new Vector2f[] {
                texCoords[0], // TR
                texCoords[3], // TL
                texCoords[2], // BL
                texCoords[1], // BR
                texCoords[0], // TR
                texCoords[2]  // BL
        };
        return uvx[index];
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
        setDirty();
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
                Objects.equals(block.material, this.material) &&
                Objects.equals(block.dirty, this.dirty) &&
                super.equals(block);
    }

    @Override
    public int hashCode() {
        int result = 5;
        return 2 *
                result +
                Objects.hashCode(this.transform) +
                Objects.hashCode(this.material) +
                Objects.hashCode(this.dirty) +
                super.hashCode();
    }
}
