package com.github.wnebyte.minecraft.util;

import java.io.File;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import com.github.wnebyte.minecraft.renderer.fonts.JFont;
import com.github.wnebyte.minecraft.renderer.Shader;
import com.github.wnebyte.minecraft.renderer.Spritesheet;
import com.github.wnebyte.minecraft.renderer.Texture;
import static org.lwjgl.opengl.GL11.*;

public class Assets {

    public static final String DIR = "C:/users/ralle/dev/java/minecraft/assets";

    private static final Map<String, Shader> shaders = new HashMap<>();

    private static final Map<String, Texture> textures = new HashMap<>();

    private static final Map<String, Spritesheet> spritesheets = new HashMap<>();

    private static final Map<String, JFont> fonts = new HashMap<>();

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
        File file = new File(path);
        path = file.getAbsolutePath().toLowerCase(Locale.ROOT);
        assert file.exists() :
                String.format("Error: (Assets) Texture: '%s' does not exist", path);

        if (textures.containsKey(path)) {
            return textures.get(path);
        } else {
            assert false : "Texture with path: " + path + " has not yet been added";
            return null;
        }
    }

    public static void addTexture(Texture texture) {
        File file = new File(texture.getPath());
        String path = file.getAbsolutePath().toLowerCase(Locale.ROOT);
        textures.put(path, texture);
    }

    public static Spritesheet getSpritesheet(String path) {
        File file = new File(path.toLowerCase(Locale.ROOT));
        assert file.exists() :
                String.format("Error: (Assets) Spritesheet: '%s' does not exist", file.getAbsolutePath());

        if (spritesheets.containsKey(file.getAbsolutePath())) {
            return spritesheets.get(file.getAbsolutePath());
        } else {
            assert false :
                    String.format("Error: (Assets) Spritesheet: '%s' has not been added", file.getAbsolutePath());
            return null;
        }
    }

    public static void addSpritesheet(String path, Spritesheet spritesheet) {
        File file = new File(path.toLowerCase(Locale.ROOT));
        assert file.exists() :
                String.format("Error: (Assets) Spritesheet: '%s' does not exist", file.getAbsolutePath());
        if (!spritesheets.containsKey(file.getAbsolutePath())) {
            spritesheets.put(file.getAbsolutePath(), spritesheet);
        }
    }

    public static Spritesheet loadSpritesheet(String path) {
        assert Files.exists(path) : "Path does not exist";
        String json = Files.read(path);
        Spritesheet.Configuration conf = Settings.GSON.fromJson(json, Spritesheet.Configuration.class);
        Texture texture = new Texture(conf.getPath(), new Texture.Configuration.Builder()
                .setTarget(GL_TEXTURE_2D)
                .addParameter(GL_TEXTURE_MAG_FILTER, GL_NEAREST)
                .addParameter(GL_TEXTURE_MIN_FILTER, GL_NEAREST)
                .addParameter(GL_TEXTURE_WRAP_S, GL_NONE)
                .addParameter(GL_TEXTURE_WRAP_T, GL_NONE)
                .build());
        Spritesheet spritesheet = new Spritesheet(texture, conf);
        Assets.addSpritesheet(texture.getPath(), spritesheet);
        return spritesheet;
    }

    public static JFont getFont(String path, int fontSize) {
        File file = new File(path.toLowerCase(Locale.ROOT));
        assert file.exists() :
                String.format("Error: (Assets) Font: '%s' does not exist", file.getAbsolutePath());

        if (fonts.containsKey(file.getAbsolutePath())) {
            return fonts.get(file.getAbsolutePath());
        } else {
            JFont font = new JFont(file.getAbsolutePath(), fontSize);
            font.generateBitmap();
            fonts.put(file.getAbsolutePath(), font);
            return font;
        }
    }

    public static Collection<Shader> getAllShaders() {
        return Collections.unmodifiableCollection(shaders.values());
    }

    public static Collection<Texture> getAllTextures() {
        return Collections.unmodifiableCollection(textures.values());
    }

    public static Collection<JFont> getAllFonts() {
        return Collections.unmodifiableCollection(fonts.values());
    }

    public static Collection<Spritesheet> getAllSpritesheets() {
        return Collections.unmodifiableCollection(spritesheets.values());
    }
}
