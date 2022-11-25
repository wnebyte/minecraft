package com.github.wnebyte.minecraft.ui;

import java.util.Stack;
import org.joml.Vector2f;
import org.joml.Vector3f;
import com.github.wnebyte.minecraft.core.KeyListener;
import com.github.wnebyte.minecraft.core.MouseListener;
import com.github.wnebyte.minecraft.renderer.Renderer;
import com.github.wnebyte.minecraft.renderer.Sprite;
import com.github.wnebyte.minecraft.renderer.fonts.JFont;
import com.github.wnebyte.minecraft.util.Assets;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

public class JGui {

    private static final Stack<JWindow> stack;

    private static final Vector2f padding;

    private static final float scale;

    private static final Renderer renderer;

    static {
        stack = new Stack<>();
        padding = new Vector2f(0.01f, 0.01f);
        scale = 1.0f;
        renderer = Renderer.getInstance();
    }

    private static WidgetState mouseInAABB(Vector2f position, Vector2f size) {
        float x = MouseListener.getScreenX();
        float y = MouseListener.getScreenY();

        if ((x >= position.x && x <= position.x + size.x) && (y >= position.y - size.y && y <= position.y)) {
            if (MouseListener.isMouseButtonDown(GLFW_MOUSE_BUTTON_LEFT)) {
                return WidgetState.CLICK;
            } else {
                return WidgetState.HOVER;
            }
        } else {
            return WidgetState.DEFAULT;
        }
    }

    private static void drawBackground(JWindow window, JImage image) {
        Vector2f position = new Vector2f(window.getPosition());
        Vector2f size = new Vector2f(window.getSize()).mul(scale);
        renderer.drawTexture2D(position.x, position.y - size.y, -50,
                window.getSize().x, window.getSize().y, image.getSprite(), scale, image.getRGB(), false);
    }

    private static void drawBackground(JWindow window, int rgb) {
        Vector2f position = new Vector2f(window.getPosition());
        Vector2f size = new Vector2f(window.getSize()).mul(scale);
        renderer.drawQuad2D(position.x, position.y - size.y, -50,
                window.getSize().x, window.getSize().y, scale, rgb);
    }

    public static void begin(float x, float y, float width, float height) {
        JWindow window = new JWindow(new Vector2f(x, y), new Vector2f(width, height), new Vector2f(padding));
        stack.push(window);
    }

    public static void begin(float x, float y, float width, float height, JImage image) {
        JWindow window = new JWindow(new Vector2f(x, y), new Vector2f(width, height), new Vector2f(padding));
        stack.push(window);
        drawBackground(window, image);
    }

    public static void begin(float x, float y, float width, float height, int rgb) {
        JWindow window = new JWindow(new Vector2f(x, y), new Vector2f(width, height), new Vector2f(padding));
        stack.push(window);
        drawBackground(window, rgb);
    }

    public static void end() {
        stack.pop();
    }

    public static void advanceCursor(float dx, float dy) {
        JWindow window = stack.peek();
        window.cursor.x += dx;
        window.cursor.y += dy;
    }

    public static void centerNextElement() {
        JWindow window = stack.peek();
        window.setCenterNextElement(true);
    }

    public static void sameLine() {
        JWindow window = stack.peek();
        if (window.lastElementPosition == null) return;
        window.cursor.x = window.lastElementPosition.x + window.lastElementSize.x + padding.x;
        window.cursor.y = window.lastElementPosition.y;
    }


    // returns the position of the next element to be layed out.
    private static Vector2f getNextElementPosition(JWindow window, Vector2f size) {
        if (window.isCenterNextElement()) {
            float width = window.size.x - window.cursor.x;
            window.cursor.x += ((width / 2.0) - (size.x / 2.0f));
            window.setCenterNextElement(false);
        }
        float x = window.position.x + window.cursor.x;
        float y = window.position.y - window.cursor.y;
        return new Vector2f(x, y);
    }

    // advances the cursor to the begining of the next line.
    private static void advanceCursorPastElement(JWindow window, Vector2f size) {
        window.lastElementPosition = new Vector2f(window.cursor);
        window.lastElementSize = new Vector2f(size);
        window.cursor.x = padding.x;
        window.cursor.y += size.y + padding.y;
    }

    public static void image(JImage image) {
        JWindow window = stack.peek();
        Vector2f size = new Vector2f(image.getSize()).mul(scale);
        Vector2f position = getNextElementPosition(window, size);
        renderer.drawTexture2D(position.x, position.y - size.y, image.getZIndex(),
                image.getWidth(), image.getHeight(),
                image.getSprite(), scale, image.getRGB(), false);
        advanceCursorPastElement(window, size);
    }

    public static void label(String text, float scale, int rgb) {
        JWindow window = stack.peek();
        JFont font = Assets.getFont(Assets.DIR + "/fonts/Minecraft.ttf", 16);
        Vector2f size = font.getSize(text).mul(scale);
        Vector2f position = getNextElementPosition(window, size);
        renderer.drawString(text, position.x, position.y - size.y, 0, scale, rgb);
        advanceCursorPastElement(window, size);
    }

    public static boolean button(JButton button) {
        JWindow window = stack.peek();
        Vector2f size = new Vector2f(button.getSize()).mul(scale);
        Vector2f position = getNextElementPosition(window, size);
        WidgetState state = mouseInAABB(position, size);
        boolean res = false;
        int color;
        switch (state) {
            case CLICK:
                color = button.getClickColor();
                res = true;
                break;
            case HOVER:
                color = button.getHoverColor();
                break;
            default:
                color = button.getDefaultColor();
        }
        renderer.drawQuad2D(position.x, position.y - size.y, -1, button.getWidth(), button.getHeight(),
                scale, color);
        // draw string
        JFont font = Assets.getFont(Assets.DIR + "/fonts/Minecraft.ttf", 16);
        Vector2f strSize = font.getSize(button.getText()).mul(button.getTextScale());
        position.x += ((size.x / 2.0f) - (strSize.x / 2.0f));
        position.y -= ((size.y / 2.0f) - (strSize.y / 2.0f));
        renderer.drawString(button.getText(), position.x, position.y - strSize.y, 0,
                button.getTextScale(), 0xFFFF);
        advanceCursorPastElement(window, size);
        return res;
    }

    public static boolean imageButton(JImageButton button) {
        JWindow window = stack.peek();
        Vector2f size = new Vector2f(button.getSize()).mul(scale);
        Vector2f position = getNextElementPosition(window, size);
        WidgetState state = mouseInAABB(position, size);
        boolean res = false;
        Sprite sprite;
        switch (state) {
            case CLICK:
                sprite = button.getClickSprite();
                res = true;
                break;
            case HOVER:
                sprite = button.getHoverSprite();
                break;
            default:
                sprite = button.getDefaultSprite();
        }
        renderer.drawTexture2D(position.x, position.y - size.y, -1, button.getWidth(), button.getHeight(),
                sprite, scale, 0xFFFF, false);
        // draw string
        JFont font = Assets.getFont(Assets.DIR + "/fonts/Minecraft.ttf", 16);
        Vector2f strSize = font.getSize(button.getText()).mul(button.getTextScale());
        position.x += ((size.x / 2.0f) - (strSize.x / 2.0f));
        position.y -= ((size.y / 2.0f) - (strSize.y / 2.0f));
        renderer.drawString(button.getText(), position.x, position.y - strSize.y, 0,
                button.getTextScale(), 0xFFFF);
        advanceCursorPastElement(window, size);
        return res;
    }

    private static int maxCursorBlink = 50;

    private static int cursorBlinkTick = 0;

    public static boolean input(JString string, float scale, float width, float height, boolean focused) {
        JWindow window = stack.peek();
        float boxPadding = 0.02f;
        JFont font = Assets.getFont(Assets.DIR + "/fonts/Minecraft.ttf", 16);
        float fontHeight = font.getFontHeight() * scale;
        Vector2f size = new Vector2f(width, height).mul(JGui.scale);
        Vector2f position = getNextElementPosition(window, size);
        WidgetState state = mouseInAABB(position, size);
        int color;
        switch (state) {
            case CLICK:
                focused = true;
                color = 0x000099;
                break;
            case HOVER:
                color = 0x000099;
                break;
            default:
                if (MouseListener.isMouseButtonDown(GLFW_MOUSE_BUTTON_LEFT)) {
                    focused = false;
                }
                color = 0x000066;
        }
        // draw outline
        renderer.drawQuad2D(position.x, position.y - size.y, -1, width, height, JGui.scale, color);
        // draw string
        Vector2f caretSize = font.getSize("/").mul(scale);
        Vector2f charPosition = new Vector2f(position);
        String text = string.get();
        if (text != null) {
            charPosition.x += boxPadding;
            Vector2f strSize = font.getSize(text).mul(scale);
            renderer.drawString(text, charPosition.x, (charPosition.y - (fontHeight / 2.0f)) - fontHeight, 0, scale, 0xFFFF);
            charPosition.x += strSize.x;
        }
        // draw caret
        if (focused) {
            renderer.drawBox2D(new Vector2f(position.x + (size.x * 0.5f), position.y - (size.y * 0.5f)),
                    new Vector2f(size), -1, 0, new Vector3f(0f, 0f, 0f));
            cursorBlinkTick = (cursorBlinkTick + 1) % maxCursorBlink;
            if (cursorBlinkTick > maxCursorBlink / 2) {
                renderer.drawString("/", charPosition.x, (charPosition.y - (fontHeight / 2.0f)) - fontHeight, 0, scale, 0xFFFF);
            }

            // mutate string
            char c = (char)KeyListener.getLastCharPressed();
            if (c != '\0') {
                string.append(c);
            }

            if (KeyListener.isKeyBeginPress(GLFW_KEY_BACKSPACE)) {
                string.deleteLast();
            }
        }

        advanceCursorPastElement(window, size);
        return focused;
    }
}
