package com.github.wnebyte.minecraft.core;

import java.util.Arrays;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public class KeyListener {

    private static KeyListener instance;

    private final boolean[] keyPressed = new boolean[350];

    private final boolean[] keyBeginPress = new boolean[350];

    public static KeyListener get() {
        if (instance == null) {
            instance = new KeyListener();
        }
        return instance;
    }

    public static void endFrame() {
        Arrays.fill(get().keyBeginPress, false);
    }

    public static void keyCallback(long window, int key, int scanCode, int action, int mods) {
        if (action == GLFW_PRESS) {
            get().keyPressed[key] = true;
            get().keyBeginPress[key] = true;
        } else if (action == GLFW_RELEASE) {
            get().keyPressed[key] = false;
            get().keyBeginPress[key] = false;
        }
    }

    public static boolean isKeyPressed(int keyCode) {
        return get().keyPressed[keyCode];
    }

    public static boolean isKeyBeginPress(int keyCode) {
        return get().keyBeginPress[keyCode];
    }
}
