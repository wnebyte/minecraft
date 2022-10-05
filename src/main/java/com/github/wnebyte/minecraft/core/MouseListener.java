package com.github.wnebyte.minecraft.core;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public class MouseListener {

    private static double scrollX, scrollY;

    private static float xPos, yPos, lastX, lastY;

    private static final boolean[] mouseButtonPressed = new boolean[9];

    private static boolean isDragging;

    private static int mouseButtonDown = 0;

    private static boolean firstMouse;

    public static void cursorPosCallback(long window, double xPos, double yPos) {
        if (mouseButtonDown > 0) {
            isDragging = true;
        }
        if (firstMouse) {
            lastX = (float)xPos;
            lastY = (float)yPos;
            firstMouse = false;
        }

        float xOffset = (float)xPos - lastX;
        float yOffset = lastY - (float)yPos; // reversed since y-coordinates go from bottom to top

        lastX = (float)xPos;
        lastY = (float)yPos;

        Camera camera = Application.getScene().getCamera();
        camera.handleMouseMovement(xOffset, yOffset, true);
    }

    public static void mouseButtonCallback(long window, int button, int action, int mod) {
        if (action == GLFW_PRESS) {
            mouseButtonDown++;
            if (button < mouseButtonPressed.length) {
                mouseButtonPressed[button] = true;
            }
        } else if (action == GLFW_RELEASE) {
            mouseButtonDown--;
            if (button < mouseButtonPressed.length) {
                mouseButtonPressed[button] = false;
                isDragging = false;
            }
        }
    }

    public static void scrollCallback(long window, double xOffset, double yOffset) {
        scrollX = xOffset;
        scrollY = yOffset;
        Camera camera = Application.getScene().getCamera();
        camera.handleMouseScroll((float)yOffset);
    }

    public static boolean isMouseButtonDown(int button) {
        if (button < mouseButtonPressed.length) {
            return mouseButtonPressed[button];
        } else {
            return false;
        }
    }

    public static boolean isDragging() {
        return isDragging;
    }

    public static float getX() {
        return xPos;
    }

    public static float getY() {
        return yPos;
    }
}
