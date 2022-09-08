package com.github.wnebyte.minecraft.components;

import java.util.Objects;
import org.joml.Vector3f;
import com.github.wnebyte.minecraft.core.Component;

public class Light extends Component {

    private Vector3f position;

    private Vector3f ambient;

    private Vector3f diffuse;

    private Vector3f specular;

    public Light() {}

    public Light(Vector3f position, Vector3f ambient, Vector3f diffuse, Vector3f specular) {
        this.position = position;
        this.ambient = ambient;
        this.diffuse = diffuse;
        this.specular = specular;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public Vector3f getAmbient() {
        return ambient;
    }

    public void setAmbient(Vector3f ambient) {
        this.ambient = ambient;
    }

    public Vector3f getDiffuse() {
        return diffuse;
    }

    public void setDiffuse(Vector3f diffuse) {
        this.diffuse = diffuse;
    }

    public Vector3f getSpecular() {
        return specular;
    }

    public void setSpecular(Vector3f specular) {
        this.specular = specular;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof Light)) return false;
        Light light = (Light) o;
        return Objects.equals(light.position, this.position) &&
                Objects.equals(light.ambient, this.ambient) &&
                Objects.equals(light.diffuse, this.diffuse) &&
                Objects.equals(light.specular, this.specular);
    }

    @Override
    public int hashCode() {
        int result = 4;
        return 3 *
                result +
                Objects.hashCode(this.position) +
                Objects.hashCode(this.ambient) +
                Objects.hashCode(this.diffuse) +
                Objects.hashCode(this.specular);
    }
}
