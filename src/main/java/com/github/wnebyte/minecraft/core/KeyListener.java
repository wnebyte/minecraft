package com.github.wnebyte.minecraft.core;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import com.github.wnebyte.minecraft.event.KeyEvent;
import com.github.wnebyte.minecraft.event.EventHandler;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public class KeyListener {

    private static final boolean[] keyPressed = new boolean[350];

    private static final boolean[] keyBeginPress = new boolean[350];

    private static final List<EventHandler<KeyEvent>> eventHandlers = new ArrayList<>();

    public static void endFrame() {
        Arrays.fill(keyBeginPress, false);
    }

    public static void keyCallback(long window, int key, int scanCode, int action, int mods) {
        if (action == GLFW_PRESS) {
            keyPressed[key] = true;
            keyBeginPress[key] = true;
        } else if (action == GLFW_RELEASE) {
            keyPressed[key] = false;
            keyBeginPress[key] = false;
        }
        handle(new KeyEvent(key, action));
    }

    public static boolean isKeyPressed(int keyCode) {
        return keyPressed[keyCode];
    }

    public static boolean isKeyBeginPress(int keyCode) {
        return keyBeginPress[keyCode];
    }

    public static void addEventHandler(EventHandler<KeyEvent> eventHandler) {
        eventHandlers.add(eventHandler);
    }

    private static void handle(KeyEvent event) {
        for (EventHandler<KeyEvent> handler : eventHandlers) {
            handler.handle(event);
        }
    }
}
