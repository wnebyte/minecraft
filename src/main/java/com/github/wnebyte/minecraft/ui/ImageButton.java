package com.github.wnebyte.minecraft.ui;

import java.util.Objects;
import org.joml.Vector2f;
import com.github.wnebyte.minecraft.renderer.Sprite;

public class ImageButton {

    public static class Builder {

        private float width;

        private float height;

        private Sprite defaultSprite;

        private Sprite clickSprite;

        private Sprite hoverSprite;

        private String text;

        private float textScale;

        public Builder setWidth(float width) {
            this.width = width;
            return this;
        }

        public Builder setHeight(float height) {
            this.height = height;
            return this;
        }

        public Builder setSize(float width, float height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder setDefaultSprite(Sprite defaultSprite) {
            this.defaultSprite = defaultSprite;
            return this;
        }

        public Builder setClickSprite(Sprite clickSprite) {
            this.clickSprite = clickSprite;
            return this;
        }

        public Builder setHoverSprite(Sprite hoverSprite) {
            this.hoverSprite = hoverSprite;
            return this;
        }

        public Builder setText(String text) {
            this.text = text;
            return this;
        }

        public Builder setTextScale(float textScale) {
            this.textScale = textScale;
            return this;
        }

        public ImageButton build() {
            return new ImageButton(width, height, defaultSprite, clickSprite, hoverSprite, text, textScale);
        }
    }

    private Vector2f size;

    private Sprite defaultSprite;

    private Sprite clickSprite;

    private Sprite hoverSprite;

    private String text;

    private float textScale;

    public ImageButton(
            float width,
            float height,
            Sprite defaultSprite,
            Sprite clickSprite,
            Sprite hoverSprite,
            String text,
            float textScale)
    {
        this.size = new Vector2f(width, height);
        this.defaultSprite = defaultSprite;
        this.clickSprite = clickSprite;
        this.hoverSprite = hoverSprite;
        this.text = text;
        this.textScale = textScale;
    }

    public Vector2f getSize() {
        return size;
    }

    public void setSize(Vector2f size) {
        this.size = size;
    }

    public float getWidth() {
        return size.x;
    }

    public float getHeight() {
        return size.y;
    }

    public Sprite getDefaultSprite() {
        return defaultSprite;
    }

    public void setDefaultSprite(Sprite defaultSprite) {
        this.defaultSprite = defaultSprite;
    }

    public Sprite getClickSprite() {
        return clickSprite;
    }

    public void setClickSprite(Sprite clickSprite) {
        this.clickSprite = clickSprite;
    }

    public Sprite getHoverSprite() {
        return hoverSprite;
    }

    public void setHoverSprite(Sprite hoverSprite) {
        this.hoverSprite = hoverSprite;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public float getTextScale() {
        return textScale;
    }

    public void setTextScale(float textScale) {
        this.textScale = textScale;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof ImageButton)) return false;
        ImageButton button = (ImageButton) o;
        return Objects.equals(button.size, this.size) &&
                Objects.equals(button.defaultSprite, this.defaultSprite) &&
                Objects.equals(button.clickSprite, this.clickSprite) &&
                Objects.equals(button.hoverSprite, this.hoverSprite) &&
                Objects.equals(button.text, this.text) &&
                Objects.equals(button.textScale, this.textScale);
    }

    @Override
    public int hashCode() {
        int result = 55;
        return 2 *
                result +
                Objects.hashCode(this.size) +
                Objects.hashCode(this.defaultSprite) +
                Objects.hashCode(this.clickSprite) +
                Objects.hashCode(this.hoverSprite) +
                Objects.hashCode(this.text) +
                Objects.hashCode(this.textScale);
    }

    @Override
    public String toString() {
        return String.format(
                "ImageButton[size: %s, defaultSprite: %s, clickSprite: %s, hoverSprite: %s, text: %s, textScale: %.2f]",
                size, defaultSprite, clickSprite, hoverSprite, text, textScale);
    }
}
