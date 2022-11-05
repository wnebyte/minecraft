package com.github.wnebyte.minecraft.renderer;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import org.joml.Vector2f;

public class Spritesheet implements Iterable<Sprite> {

    public static class Configuration {

        private String path;

        private List<Sprite.Configuration> sprites;

        public Configuration(String path, List<Sprite.Configuration> sprites) {
            this.path = path;
            this.sprites = sprites;
        }

        public String getPath() {
            return path;
        }

        public List<Sprite.Configuration> getSprites() {
            return sprites;
        }

        public static class Builder {

            private String path;

            private List<Sprite.Configuration> sprites = new ArrayList<>();

            public Builder setPath(String path) {
                this.path = path;
                return this;
            }

            public Builder setSprites(List<Sprite.Configuration> sprites) {
                this.sprites = sprites;
                return this;
            }

            public Builder addSprite(Sprite.Configuration sprite) {
                this.sprites.add(sprite);
                return this;
            }

            public Configuration build() {
                return new Configuration(path, sprites);
            }
        }
    }

    private final Texture texture;

    private final List<Sprite> sprites;

    public Spritesheet(Texture texture, int spriteWidth, int spriteHeight, int numSprites, int spacing) {
        this.texture = texture;
        this.sprites = new ArrayList<>(numSprites);

        int currentX = 0;
        int currentY = texture.getHeight() - spriteHeight;
        for (int i = 0; i < numSprites; i++) {
            float topY = (currentY + spriteHeight) / (float)texture.getHeight();
            float rightX = (currentX + spriteWidth) / (float)texture.getWidth();
            float leftX = currentX / (float)texture.getWidth();
            float bottomY = currentY / (float)texture.getHeight();

            Vector2f[] texCoords = new Vector2f[] {
                    new Vector2f(rightX, topY),
                    new Vector2f(rightX, bottomY),
                    new Vector2f(leftX, bottomY),
                    new Vector2f(leftX, topY)
            };

            Sprite sprite = new Sprite();
            sprite.setTexture(this.texture);
            sprite.setTexCoords(texCoords);
            sprite.setWidth(spriteWidth);
            sprite.setHeight(spriteHeight);
            sprites.add(sprite);
            currentX += spriteWidth + spacing;
            if (currentX >= texture.getWidth()) {
                currentX = 0;
                currentY -= spriteHeight + spacing;
            }
        }
    }

    public Spritesheet(Texture texture, Configuration conf) {
        this.texture = texture;
        this.sprites = new ArrayList<>(conf.getSprites().size());

        for (Sprite.Configuration spr : conf.getSprites()) {
            float x = spr.getStart().x;
            float y = spr.getStart().y;
            float spriteWidth = spr.getSize().x;
            float spriteHeight = spr.getSize().y;
            float topY = (y + spriteHeight) / (float)texture.getHeight();
            float rightX = (x + spriteWidth) / (float)texture.getWidth();
            float leftX = x / (float)texture.getWidth();
            float bottomY = y / (float)texture.getHeight();

            Vector2f[] texCoords = {
                    new Vector2f(rightX, topY),
                    new Vector2f(rightX, bottomY),
                    new Vector2f(leftX, bottomY),
                    new Vector2f(leftX, topY)
            };

            Sprite sprite = new Sprite();
            sprite.setTexture(texture);
            sprite.setTexCoords(texCoords);
            sprite.setWidth(spriteWidth);
            sprite.setHeight(spriteHeight);
            sprites.add(sprite);
        }
    }

    public Spritesheet(Configuration conf) {
        this(new Texture(conf.getPath(), true), conf);
    }

    public Sprite getSprite(int index) {
        return sprites.get(index);
    }

    public int size() {
        return sprites.size();
    }

    public Texture getTexture() {
        return texture;
    }

    @Override
    public Iterator<Sprite> iterator() {
        return sprites.iterator();
    }
}
