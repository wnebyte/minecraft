package com.github.wnebyte.minecraft.core;

import org.lwjgl.opengl.GL;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWErrorCallback;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {

    public static class Resolution {

        public final int width, height;

        public Resolution(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }

    public static Window newInstance(String title) {
        if (Window.window == null) {
            Window.window = new Window(title);
            Window.window.init();
            return Window.window;
        } else {
            throw new IllegalStateException(
                    "Window has already been initialized"
            );
        }
    }

    private static Window window;

    private long glfwWindow;

    private String title;

    private int width, height;

    private Scene scene;

    private Window(String title) {
        this.title = title;
    }

    public void init() {
        // Setup an error callback
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW
        if (!glfwInit()) {
            throw new IllegalStateException(
                    "Unable to initialize GLFW."
            );
        }

        Resolution res = Window.getResolution(glfwGetPrimaryMonitor());
        width = res.width;
        height = res.height;

        // Configure GLFW
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 6);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);

        // Create the window
        glfwWindow = glfwCreateWindow(width, height, title, NULL, NULL);
        if (glfwWindow == NULL) {
            throw new IllegalStateException(
                    "Failed to create GLFW window."
            );
        }

        // Make OpenGL context current
        glfwMakeContextCurrent(glfwWindow);
       // glfwSetFramebufferSizeCallback(glfwWindow, this::framebufferSizeCallback);
        glfwSetCursorPosCallback(glfwWindow, MouseListener::cursorPosCallback);
        glfwSetMouseButtonCallback(glfwWindow, MouseListener::mouseButtonCallback);
        glfwSetScrollCallback(glfwWindow, MouseListener::scrollCallback);
        glfwSetKeyCallback(glfwWindow, KeyListener::keyCallback);
        glfwSetInputMode(glfwWindow, GLFW_CURSOR, GLFW_CURSOR_DISABLED);

        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(glfwWindow);

        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        glViewport(0, 0, width, height);
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(glfwWindow);
    }

    public void swapBuffers() {
        glfwSwapBuffers(glfwWindow);
    }

    public void pollEvents() {
        glfwPollEvents();
    }

    public void setViewport() {
        glViewport(0, 0, width, height);
    }

    public void processInput(Camera camera, float dt) {
        if (glfwGetKey(glfwWindow, GLFW_KEY_ESCAPE) == GLFW_PRESS) {
            glfwSetWindowShouldClose(glfwWindow, true);
        }
        if (glfwGetKey(glfwWindow, GLFW_KEY_W) == GLFW_PRESS) {
            camera.handleKeyboard(Camera.Movement.FORWARD, dt);
        }
        if (glfwGetKey(glfwWindow, GLFW_KEY_S) == GLFW_PRESS) {
            camera.handleKeyboard(Camera.Movement.BACKWARD, dt);
        }
        if (glfwGetKey(glfwWindow, GLFW_KEY_A) == GLFW_PRESS) {
            camera.handleKeyboard(Camera.Movement.LEFT, dt);
        }
        if (glfwGetKey(glfwWindow, GLFW_KEY_D) == GLFW_PRESS) {
            camera.handleKeyboard(Camera.Movement.RIGHT, dt);
        }
        if (glfwGetKey(glfwWindow, GLFW_KEY_SPACE) == GLFW_PRESS) {
            camera.handleKeyboard(Camera.Movement.UP, dt);
        }
        if (glfwGetKey(glfwWindow, GLFW_KEY_LEFT_CONTROL) == GLFW_PRESS) {
            camera.handleKeyboard(Camera.Movement.DOWN, dt);
        }
        if (glfwGetKey(glfwWindow, GLFW_KEY_COMMA) == GLFW_PRESS) {
            camera.resetZoom();
        }
    }

    private void framebufferSizeCallback(long window, int width, int height) {
        this.width = width;
        this.height = height;
        glViewport(0, 0, width, height);
    }

    public void destroy() {
        // Free the allocated memory
        glfwFreeCallbacks(glfwWindow);
        glfwDestroyWindow(glfwWindow);
        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Scene getScene() {
        return scene;
    }

    public static Resolution getResolution(long monitor) {
        int width = 0;
        int height = 0;
        int size = 0;
        boolean found = false;

        GLFWVidMode.Buffer buffer = glfwGetVideoModes(monitor);
        if (buffer != null) {
            for (GLFWVidMode mode : buffer) {
                int tmpWidth = mode.width();
                int tmpHeight = mode.height();
                int tmpSize = tmpWidth * tmpHeight;
                if (tmpSize > size) {
                    width = tmpWidth;
                    height = tmpHeight;
                    size = tmpSize;
                    found = true;
                }
            }
        }

        return found ? new Resolution(width, height) : new Resolution(1920, 1080);
    }
}
