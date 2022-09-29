package com.github.wnebyte.minecraft.util;

import java.util.Arrays;
import java.util.Objects;
import org.joml.Vector2f;
import com.github.wnebyte.minecraft.renderer.Texture;

public class TextureFormat {

    private int id;

    private String name;

    private Vector2f[] uvs;

    private transient Texture texture;

    public TextureFormat(int id, String name, Vector2f[] uvs) {
        this.id = id;
        this.name = name;
        this.uvs = uvs;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Vector2f[] getUvs() {
        return uvs;
    }

    public Texture getTexture() {
        return texture;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof TextureFormat)) return false;
        TextureFormat textureFormat = (TextureFormat) o;
        return Objects.equals(this.id, textureFormat.id) &&
                Objects.equals(this.name, textureFormat.name) &&
                Arrays.equals(this.uvs, textureFormat.uvs);
    }

    @Override
    public int hashCode() {
        int result = 73;
        return 2 *
                result +
                Objects.hashCode(this.id) +
                Objects.hashCode(this.name) +
                Arrays.hashCode(this.uvs);
    }

    @Override
    public String toString() {
        return String.format("TextureFormat[id: %d, name: %s, uvs: %s]", id, name, Arrays.toString(uvs));
    }

    public String toJson() {
        return Settings.GSON.toJson(this);
    }
}
