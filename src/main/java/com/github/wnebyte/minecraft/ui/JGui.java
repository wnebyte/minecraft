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

    private static final float scale = 1.0f;

    private static final Renderer renderer;

    static {
        stack = new Stack<>();
        padding = new Vector2f(0.01f, 0.01f);
        renderer = Renderer.getInstance();
    }

    private static WidgetState mouseInAABB(float x, float y, float width, float height) {
        return mouseInAABB(new Vector2f(x, y), new Vector2f(width, height));
    }

    private static WidgetState mouseInAABB(Vector2f position, Vector2f size) {
        float x = MouseListener.getScreenX();
        float y = MouseListener.getScreenY();

        if ((x >= position.x && x <= position.x + size.x) && (y >= position.y && y <= position.y + size.y)) {
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
        begin(new Vector2f(x, y), new Vector2f(width, height));
    }

    public static void begin(Vector2f position, Vector2f size) {
        JWindow window = new JWindow(position, size, new Vector2f(padding));
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
        float x0 = position.x;
        float x1 = x0 + size.x;
        float y1 = position.y;
        float y0 = y1 - size.y;
        renderer.drawTexture2D(x0, y0, 0, width, height, sprite, scale, 0xFFFF, false);
        advanceCursorPastElement(window, size);
    }

    public static void label(String text, float scale, int rgb) {
        JWindow window = stack.peek();
        JFont font = Assets.getFont(Assets.DIR + "/fonts/Minecraft.ttf", 16);
        Vector2f size = font.getSize(text).mul(scale);
        Vector2f position = getNextElementPosition(window, size);
        float x0 = position.x;
        float x1 = x0 + size.x;
        float y1 = position.y;
        float y0 = y1 - size.y;
        renderer.drawString(text, x0, y0, 0, scale, rgb);
        advanceCursorPastElement(window, size);
    }

    public static String input() { return null; }

    public static boolean button() { return false; }

    public static boolean texturedButton() { return false; }

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

    // places the cursor at the begining of the next row.
    private static void advanceCursorPastElement(JWindow window, Vector2f size) {
        window.lastElementPosition = new Vector2f(window.cursor);
        window.lastElementSize = new Vector2f(size);
        window.cursor.x = padding.x;
        window.cursor.y += size.y + padding.y;
    }
}
