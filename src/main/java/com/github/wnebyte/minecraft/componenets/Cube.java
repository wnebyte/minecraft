package com.github.wnebyte.minecraft.componenets;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.joml.Vector2f;
import com.github.wnebyte.minecraft.core.Component;
import com.github.wnebyte.minecraft.core.Transform;
import com.github.wnebyte.minecraft.renderer.Texture;

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
public class Cube extends Component {

    public Transform transform;

    private Texture side, top, bottom;

    private List<Texture> textures;

    private boolean dirty;

    private boolean isAir;

    private Vector2f[] texCoords = new Vector2f[] {
            new Vector2f(1, 1), // TR
            new Vector2f(1, 0), // BR
            new Vector2f(0, 0), // BL
            new Vector2f(0, 1)  // TL
    };

    public Cube(Transform transform, Texture side, Texture top, Texture bottom) {
        this.transform = transform;
        this.side = side;
        this.top = top;
        this.bottom = bottom;
        this.dirty = true;
        this.textures = new ArrayList<>(3);
        this.textures.add(side);
        this.textures.add(top);
        this.textures.add(bottom);
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

    public Texture getSideTexture() {
        return side;
    }

    public Texture getTopTexture() {
        return top;
    }

    public Texture getBottomTexture() {
        return bottom;
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

    public boolean isAir() {
        return isAir;
    }

    public void setIsAir() {
        this.isAir = true;
    }

    public List<Texture> getTextures() {
        return textures;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof Cube)) return false;
        Cube block = (Cube) o;
        return Objects.equals(block.transform, this.transform) &&
                Objects.equals(block.dirty, this.dirty) &&
                super.equals(block);
    }

    @Override
    public int hashCode() {
        int result = 5;
        return 2 *
                result +
                Objects.hashCode(this.transform) +
                Objects.hashCode(this.dirty) +
                super.hashCode();
    }
}
