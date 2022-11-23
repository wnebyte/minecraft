package com.github.wnebyte.minecraft.core;

import java.util.Arrays;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public class KeyListener {

    private static final boolean[] keyPressed = new boolean[350];

    private static final boolean[] keyBeginPress = new boolean[350];

    private static int lastCharPressed = '\0';

    public static void endFrame() {
        Arrays.fill(keyBeginPress, false);
        lastCharPressed = '\0';
    }

    public static void keyCallback(long window, int key, int scanCode, int action, int mods) {
        if (action == GLFW_PRESS) {
            keyPressed[key] = true;
            keyBeginPress[key] = true;
        } else if (action == GLFW_RELEASE) {
            keyPressed[key] = false;
            keyBeginPress[key] = false;
        }
    }

    public static void charCallback(long window, int codepoint) {
        lastCharPressed = codepoint;
    }

    public static boolean isKeyPressed(int keyCode) {
        return keyPressed[keyCode];
    }

    public static boolean isKeyBeginPress(int keyCode) {
        return keyBeginPress[keyCode];
    }

    public static int getLastCharPressed() {
        return lastCharPressed;
    }
}
