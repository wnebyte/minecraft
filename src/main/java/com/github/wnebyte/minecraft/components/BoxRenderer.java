package com.github.wnebyte.minecraft.components;

import java.util.Objects;
import org.joml.Vector4f;
import com.github.wnebyte.minecraft.core.Scene;
import com.github.wnebyte.minecraft.core.Component;
import com.github.wnebyte.minecraft.core.Transform;

public class BoxRenderer extends Component {

    private Vector4f color = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);

    private transient Transform transform;

    private transient boolean dirty = true;

    @Override
    public void start(Scene scene) {
        transform = Transform.copy(gameObject.transform);
    }

    @Override
    public void update(float dt) {
        if (!transform.equals(gameObject.transform)) {
            transform = Transform.copy(gameObject.transform);
            dirty = true;
        }
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

    public Vector4f getColor() {
        return color;
    }

    public void setColor(Vector4f value) {
        if (!color.equals(value)) {
            color.set(value);
            dirty = true;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof BoxRenderer)) return false;
        BoxRenderer c = (BoxRenderer) o;
        return Objects.equals(c.transform, this.transform) &&
                Objects.equals(c.color, this.color) &&
                Objects.equals(c.dirty, this.dirty) &&
                super.equals(c);
    }

    @Override
    public int hashCode() {
        int result = 5;
        return 2 *
                result +
                Objects.hashCode(this.transform) +
                Objects.hashCode(this.color) +
                Objects.hashCode(this.dirty) +
                super.hashCode();
    }
}
