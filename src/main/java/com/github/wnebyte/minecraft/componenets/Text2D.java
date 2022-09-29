package com.github.wnebyte.minecraft.componenets;

import java.util.Objects;
import com.github.wnebyte.minecraft.fonts.SFont;

public class Text2D {

    private String text;

    private int rgb;

    private SFont font;

    private float x, y;

    private float scale;

    private transient boolean dirty = true;

    public Text2D(String text, float x, float y, float scale, int rgb) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.scale = scale;
        this.rgb = rgb;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public int getRGB() {
        return rgb;
    }

    public void setRGB(int rgb) {
        this.rgb = rgb;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setFont(SFont font) {
        this.font = font;
    }

    public SFont getFont() {
        return font;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof Text2D)) return false;
        Text2D tr = (Text2D) o;
        return Objects.equals(tr.text, this.text) &&
                Objects.equals(tr.x, this.x) &&
                Objects.equals(tr.y, this.y) &&
                Objects.equals(tr.scale, this.scale) &&
                Objects.equals(tr.rgb, this.rgb) &&
                super.equals(tr);
    }

    @Override
    public int hashCode() {
        int result = 25;
        return 2 *
                result +
                Objects.hashCode(this.text) +
                Objects.hashCode(this.x) +
                Objects.hashCode(this.y) +
                Objects.hashCode(this.scale) +
                Objects.hashCode(this.rgb);
    }
}
