package com.github.wnebyte.minecraft.renderer.fonts;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.awt.*;
import java.awt.image.BufferedImage;
import org.joml.Vector2f;
import com.github.wnebyte.minecraft.renderer.Texture;

public class JFont {

    public static Font registerFont(String path) {
        try {
            GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Font font = Font.createFont(Font.TRUETYPE_FONT, new File(path));
            env.registerFont(font);
            return font;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private final String path;

    private final int fontSize;

    private final Map<Integer, CharInfo> characters;

    private int fontHeight;

    private int width;

    private int height;

    private Texture texture;

    public JFont(String path, int fontSize) {
        this.path = path;
        this.fontSize = fontSize;
        this.characters = new HashMap<>();
    }

    public CharInfo getCharacter(int codepoint) {
        return characters.get(codepoint);
    }

    public Map<Integer, CharInfo> getCharacters() {
        return Collections.unmodifiableMap(characters);
    }

    public void generateBitmap() {
        Font font = registerFont(path).deriveFont(Font.PLAIN, fontSize);
        // create fake image to get font information
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setFont(font);
        FontMetrics fontMetrics = g2d.getFontMetrics();

        float estWidth = (int)Math.sqrt(font.getNumGlyphs()) * font.getSize() + 1;
        this.width = 0;
        this.fontHeight = fontMetrics.getHeight();
        this.height = fontHeight;
        float vSpacing = 1.4f;
        int x = 0;
        int y = (int)(height * vSpacing);

        for (int i = 0; i < font.getNumGlyphs(); i++) {
            if (font.canDisplay(i)) {
                CharInfo info = new CharInfo.Builder()
                        .setSourceX(x)
                        .setSourceY(y)
                        .setDecent(fontMetrics.getDescent())
                        .setWidth(fontMetrics.charWidth(i))
                        .setHeight(fontMetrics.getHeight())
                        .build();
                characters.put(i, info);
                width = Math.max(x + info.getWidth() + 2, width);
                x += info.getWidth() + 2.0f;
                if (x > estWidth) {
                    x = 0;
                    y += info.getHeight() * vSpacing;
                    height += info.getHeight() * vSpacing;
                }
            }
        }
        height += fontMetrics.getHeight() * vSpacing;
        g2d.dispose();
        img.flush();

        // create the real texture
        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        g2d = img.createGraphics();
        g2d.setRenderingHints(getRenderingHints());
        g2d.setFont(font);
        g2d.setColor(Color.WHITE);
        for (int i = 0; i < font.getNumGlyphs(); i++) {
            if (font.canDisplay(i)) {
                CharInfo info = characters.get(i);
                info.calculateTexCoords(width, height);
                g2d.drawString("" + (char)i, info.getSourceX(), info.getSourceY());
            }
        }
        g2d.dispose();
        this.texture = new Texture(img);

        /*
        try {
            ImageIO.write(img, "png", new File("C:/Users/ralle/dev/java/minecraft/font.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
         */
        img.flush();
    }

    private RenderingHints getRenderingHints() {
        Map<RenderingHints.Key, Object> map = new HashMap<>();
        map.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        map.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        map.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        map.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        map.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        RenderingHints rh = new RenderingHints(map);
        return rh;
    }

    public Vector2f getSize(String text) {
        int w = 0;
        int h = 0;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            CharInfo info = getCharacter(c);
            if (info == null) {
                System.err.printf("Warning: (Renderer) Unknown char: '%c'%n", c);
                continue;
            }

            int width = info.getWidth();
            int height = info.getHeight();

            w += width;
            h = Math.max(h, height);
        }

        return new Vector2f(w, h);
    }

    public int getFontHeight() {
        return fontHeight;
    }

    public Texture getTexture() {
        return texture;
    }

    public int getTextureId() {
        return (texture == null) ? -1 : texture.getId();
    }
}
