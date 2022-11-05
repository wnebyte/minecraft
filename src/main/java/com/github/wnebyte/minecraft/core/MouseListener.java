package com.github.wnebyte.minecraft.core;

import org.joml.Vector2f;
import org.joml.Vector4f;
import org.joml.Matrix4f;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public class MouseListener {

    private static double xScroll, yScroll;

    private static float xPos, yPos, lastX, lastY;

    private static float xScreenPos, yScreenPos;

    private static final boolean[] mouseButtonPressed = new boolean[9];

    private static boolean isDragging;

    private static int mouseButtonDown = 0;

    private static boolean firstMouse;

    private static Vector2f windowSize;

    public static void cursorPosCallback(long window, double x, double y) {
        xPos = (float)x;
        yPos = (float)y;

        if (mouseButtonDown > 0) {
            isDragging = true;
        }
        if (firstMouse) {
            lastX = (float)x;
            lastY = (float)y;
            firstMouse = false;
        }

        float xOffset = (float)x - lastX;
        float yOffset = lastY - (float)y; // reversed since y-coordinates go from bottom to top

        lastX = (float)x;
        lastY = (float)y;

        Scene scene = Application.getWindow().getScene();
        if (scene != null) {
            Camera camera = scene.getCamera();
            if (camera != null) {
                if (camera.isLocked()) {
                    Vector4f tmp = new Vector4f(
                            (xPos / windowSize.x) * 2.0f - 1.0f,
                            -((yPos / windowSize.y) * 2.0f - 1.0f),
                            0, 1.0f);
                    Matrix4f inverseProjection = new Matrix4f(camera.getInverseProjectionHUD());
                    Vector4f projectedScreen = tmp.mul(inverseProjection);
                    xScreenPos = projectedScreen.x;
                    yScreenPos = projectedScreen.y;
                } else {
                    xScreenPos = 0.0f;
                    yScreenPos = 0.0f;
                    camera.handleMouseMovement(xOffset, yOffset, true);
                }
            }
        }
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
        xScroll = xOffset;
        yScroll = yOffset;
        Scene scene = Application.getWindow().getScene();
        if (scene != null) {
            Camera camera = scene.getCamera();
            if (camera != null) {
                camera.handleMouseScroll((float)yOffset);
            }
        }
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

    public static float getScreenY() {
        return yScreenPos;
    }

    public static float getScreenX() {
        return xScreenPos;
    }

    public static double getScrollX() {
        return xScroll;
    }

    public static double getScrollY() {
        return yScroll;
    }

    public static void resetScreenPos() {
        xScreenPos = 0.0f;
        yScreenPos = 0.0f;
    }

    public static void setWindowSize(Vector2f windowSize) {
        MouseListener.windowSize = windowSize;
    }
}
