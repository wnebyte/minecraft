package com.github.wnebyte.minecraft.core;

import org.joml.Vector2i;
import org.lwjgl.opengl.GL;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWErrorCallback;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {

    /*
    ###########################
    #         UTILITIES       #
    ###########################
    */

    public static Window newInstance(String title) {
        if (Window.window == null) {
            Window.window = new Window(title);
            Window.window.init();
            return Window.window;
        } else {
            throw new IllegalStateException(
                    "Window has already been instantiated."
            );
        }
    }

    public static Vector2i getResolution() {
        return getResolution(glfwGetPrimaryMonitor());
    }

    public static Vector2i getResolution(long monitor) {
        int width = 0, height = 0, size = 0;
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

        return found ? new Vector2i(width, height) : new Vector2i(1920, 1080);
    }

    /*
    ###########################
    #      STATIC FIELDS      #
    ###########################
    */

    private static Window window;

    /*
    ###########################
    #          FIELDS         #
    ###########################
    */

    private long glfwWindow;

    private String title;

    private int width, height;

    private Scene scene;

    /*
    ###########################
    #       CONSTRUCTORS      #
    ###########################
    */

    private Window(String title) {
        this.title = title;
    }

    /*
    ###########################
    #          METHODS        #
    ###########################
    */

    private void init() {
        // Setup an error callback
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW
        if (!glfwInit()) {
            throw new IllegalStateException(
                    "Unable to initialize GLFW."
            );
        }

        Vector2i res = Window.getResolution(glfwGetPrimaryMonitor());
        width = res.x;
        height = res.y;

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
        glfwSetCharCallback(glfwWindow, KeyListener::charCallback);
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

        viewport();
    }

    public void update(float dt) {
        if (scene != null) {
            scene.update(dt);
            scene.render();
        }
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(glfwWindow);
    }

    public void swapBuffers() {
        glfwSwapBuffers(glfwWindow);
    }

    public void pollEvents(float dt) {
        glfwPollEvents();
        if (scene != null) {
            scene.processInput(dt);
        }
    }

    public void viewport() {
        glViewport(0, 0, width, height);
    }

    public void setShouldClose(boolean value) {
        glfwSetWindowShouldClose(glfwWindow, value);
    }

    public void setCursorMode(int mode) {
        glfwSetInputMode(glfwWindow, GLFW_CURSOR, mode);
    }

    public void setCursorPos(double xPos, double yPos) {
        glfwSetCursorPos(glfwWindow, xPos, yPos);
    }

    private void framebufferSizeCallback(long window, int width, int height) {
        this.width = width;
        this.height = height;
        glViewport(0, 0, width, height);
    }

    public void destroy() {
        if (scene != null) {
            scene.destroy();
        }
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

    public Vector2i getSize() {
        return new Vector2i(width, height);
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
        glfwSetWindowSize(glfwWindow, width, height);
    }

    public float getAspectRatio() {
        return (float)width / (float)height;
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene newScene) {
        if (scene != null) {
            scene.destroy();
        }
        this.scene = newScene;
        if (scene != null) {
            scene.start();
        }
    }
}
