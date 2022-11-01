package com.github.wnebyte.minecraft.fonts;

import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.awt.*;
import java.awt.image.BufferedImage;
import com.github.wnebyte.minecraft.renderer.Texture;

public class MFont {

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

    private int width, height;

    private int textureId = -1;

    private Texture texture = null;

    public MFont(String path, int fontSize) {
        this.path = path;
        this.fontSize = fontSize;
        this.characters = new HashMap<>();
    }

    public CharInfo getCharacter(int codepoint) {
        return characters.getOrDefault(codepoint,
                new CharInfo(0, 0, 0,0, 0));
    }

    public void generateBitmap() {
        Font font = registerFont(path);
        font = new Font(font.getName(), Font.PLAIN, fontSize);
        // create fake image to get font information
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setFont(font);
        FontMetrics fontMetrics = g2d.getFontMetrics();

        int estWidth = (int)Math.sqrt(font.getNumGlyphs()) * font.getSize() + 1;
        width = 0;
        height = fontMetrics.getHeight();
        float f = 1.4f;
        int x = 0;
        int y = (int)(height * f);

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
                width = Math.max(x + fontMetrics.charWidth(i), width);

                x += info.getWidth();
                if (x > estWidth) {
                    x = 0;
                    y += fontMetrics.getHeight() * f;
                    height += fontMetrics.getHeight() * f;
                }
            }
        }
        height += fontMetrics.getHeight() * f;
        g2d.dispose();

        // create the real texture
        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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
        this.textureId = texture.getId();

        /*
        try {
            ImageIO.write(img, "png", new File("C:/Users/ralle/dev/java/engine/font.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
         */
    }

    public Texture getTexture() {
        return texture;
    }

    public int getTextureId() {
        return textureId;
    }
}
