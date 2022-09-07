package com.github.wnebyte.minecraft.util;

import java.io.File;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import com.github.wnebyte.minecraft.renderer.Shader;
import com.github.wnebyte.minecraft.renderer.Texture;

public class Assets {

    private static final Map<String, Shader> shaders = new HashMap<>();

    private static final Map<String, Texture> textures = new HashMap<>();

    /**
     * Lazily initializes (if necessary) and returns the <code>Shader</code> located at
     * the specified <code>path</code>.
     */
    public static Shader getShader(String path) {
        File file = new File(path.toLowerCase(Locale.ROOT));
        assert file.exists() :
                String.format("Error: (Assets) Shader: '%s' does not exist", file.getAbsolutePath());

        if (shaders.containsKey(file.getAbsolutePath())) {
            return shaders.get(file.getAbsolutePath());
        } else {
            Shader shader = new Shader(file.getAbsolutePath());
            shader.compile();
            shaders.put(file.getAbsolutePath(), shader);
            return shader;
        }
    }

    /**
     * Lazily initializes (if necessary) and returns the <code>Texture</code> located at
     * the specified <code>path</code>.
     */
    public static Texture getTexture(String path) {
        File file = new File(path.toLowerCase(Locale.ROOT));
        assert file.exists() :
                String.format("Error: (Assets) Texture: '%s' does not exist", file.getAbsolutePath());

        if (textures.containsKey(file.getAbsolutePath())) {
            return textures.get(file.getAbsolutePath());
        } else {
            Texture texture = new Texture(path, true);
           // texture.init(file.getAbsolutePath());
            textures.put(file.getAbsolutePath(), texture);
            return texture;
        }
    }

    public static Collection<Shader> getShaders() {
        return shaders.values();
    }

    public static Collection<Texture> getTextures() {
        return textures.values();
    }
}
