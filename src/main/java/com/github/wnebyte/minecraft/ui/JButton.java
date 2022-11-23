package com.github.wnebyte.minecraft.ui;

import java.util.Objects;
import org.joml.Vector2f;

public class JButton {

    public static class Builder {

        private float width;

        private float height;

        private int defaultColor;

        private int clickColor;

        private int hoverColor;

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

        public Builder setDefaultColor(int defaultColor) {
            this.defaultColor = defaultColor;
            return this;
        }

        public Builder setClickColor(int clickColor) {
            this.clickColor = clickColor;
            return this;
        }

        public Builder setHoverColor(int hoverColor) {
            this.hoverColor = hoverColor;
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

        public JButton build() {
            return new JButton(width, height, defaultColor, clickColor, hoverColor, text, textScale);
        }
    }

    private Vector2f size;

    private int defaultColor;

    private int clickColor;

    private int hoverColor;

    private String text;

    private float textScale;

    public JButton(
            float width,
            float height,
            int defaultColor,
            int clickColor,
            int hoverColor,
            String text,
            float textScale)
    {
        this.size = new Vector2f(width, height);
        this.defaultColor = defaultColor;
        this.clickColor = clickColor;
        this.hoverColor = hoverColor;
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

    public int getDefaultColor() {
        return defaultColor;
    }

    public void setDefaultColor(int defaultColor) {
        this.defaultColor = defaultColor;
    }

    public int getClickColor() {
        return clickColor;
    }

    public void setClickColor(int clickColor) {
        this.clickColor = clickColor;
    }

    public int getHoverColor() {
        return hoverColor;
    }

    public void setHoverColor(int hoverColor) {
        this.hoverColor = hoverColor;
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
        if (!(o instanceof JButton)) return false;
        JButton button = (JButton) o;
        return Objects.equals(button.size, this.size) &&
                Objects.equals(button.defaultColor, this.defaultColor) &&
                Objects.equals(button.clickColor, this.clickColor) &&
                Objects.equals(button.hoverColor, this.hoverColor) &&
                Objects.equals(button.text, this.text) &&
                Objects.equals(button.textScale, this.textScale);
    }

    @Override
    public int hashCode() {
        int result = 23;
        return 3 *
                result +
                Objects.hashCode(this.size) +
                Objects.hashCode(this.defaultColor) +
                Objects.hashCode(this.clickColor) +
                Objects.hashCode(this.hoverColor) +
                Objects.hashCode(this.text) +
                Objects.hashCode(this.textScale);
    }

    @Override
    public String toString() {
        return String.format(
                "Image[size: %s, defaultColor: %d, clickColor: %d, hoverColor: %d, text: %s, textScale: %.2f]",
                size, defaultColor, clickColor, hoverColor, text, textScale);
    }
}
