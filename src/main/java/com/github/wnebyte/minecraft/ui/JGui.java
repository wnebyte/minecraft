package com.github.wnebyte.minecraft.ui;

import java.util.Stack;
import org.joml.Vector2f;
import com.github.wnebyte.minecraft.core.MouseListener;
import com.github.wnebyte.minecraft.renderer.Renderer;
import com.github.wnebyte.minecraft.renderer.Sprite;
import com.github.wnebyte.minecraft.renderer.fonts.JFont;
import com.github.wnebyte.minecraft.util.Assets;
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

    private static WidgetState mouseInAABB(float x, float y, float width, float height) {
        return mouseInAABB(new Vector2f(x, y), new Vector2f(width, height));
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
        }

        return WidgetState.DEFAULT;
    }

    private static void drawOverlay(JWindow window) {
        float width = window.getSize().x * scale;
        float height = window.getSize().y * scale;
        float x0 = window.getPosition().x;
        float x1 = x0 + width;
        float y1 = window.getPosition().y;
        float y0 = y1 - height;
        renderer.drawQuad2D(x0, y0, -5, width, height, scale, 0xFFFF);
    }

    public static void begin(float x, float y, float width, float height) {
        JWindow window = new JWindow(new Vector2f(x, y), new Vector2f(width, height), new Vector2f(padding));
        drawOverlay(window); // Todo: remove this eventually
        stack.push(window);
    }

    public static void end() {
        stack.pop();
    }

    public static void centerNextElement() {
        JWindow window = stack.peek();
        window.setCenterNextElement(true);
    }

    public static void sameLine() {
        JWindow window = stack.peek();
        if (window.lastElementPosition == null) return;
        float x = window.lastElementPosition.x + window.lastElementSize.x + padding.x;
        float y = window.lastElementPosition.y;
        window.cursor.x = x;
        window.cursor.y = y;
    }

    public static void image(Sprite sprite, float width, float height) {
        JWindow window = stack.peek();
        Vector2f size = new Vector2f(width, height).mul(scale);
        Vector2f position = getNextElementPosition(window, size);
        renderer.drawTexture2D(position.x, position.y - size.y, 0, width, height,
                sprite, scale, 0xFFFF, false);
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

    public static boolean button(Button button) {
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

    public static boolean imageButton(ImageButton button) {
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
        renderer.drawTexture2D(position.x, position.y - size.y, -1, button.getSize().x, button.getSize().y,
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

    public static String input() { return null; }

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

    // advances the cursor to the begining of the next row.
    private static void advanceCursorPastElement(JWindow window, Vector2f size) {
        window.lastElementPosition = new Vector2f(window.cursor);
        window.lastElementSize = new Vector2f(size);
        window.cursor.x = padding.x;
        window.cursor.y += size.y + padding.y;
    }
}
