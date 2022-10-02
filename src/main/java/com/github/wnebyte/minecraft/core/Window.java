package com.github.wnebyte.minecraft.core;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWErrorCallback;
import com.github.wnebyte.minecraft.componenets.Cube;
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

    private static Window window;

    private long glfwWindow;

    private String title;

    private int width, height;

    private float lastX = 0.0f;

    private float lastY = 0.0f;

    private boolean firstMouse = true;

    private Camera camera;

    private float dt = 0.0f;

    private float lastFrame = 0.0f;

    private Cube lightSource;

    private Scene scene;

    private Window() {
        this.title = "Window";
        this.camera = new Camera(
                new Vector3f(0.0f, 0.0f, 3.0f),  // position
                new Vector3f(0.0f, 0.0f, -1.0f), // front
                new Vector3f(0.0f, 1.0f, 0.0f),  // up
                Camera.DEFAULT_YAW,
                Camera.DEFAULT_PITCH,
                10f,
                Camera.DEFAULT_MOUSE_SENSITIVITY,
                Camera.DEFAULT_ZOOM);
    }

    public static Window get() {
        if (Window.window == null) {
            Window.window = new Window();
        }
        return Window.window;
    }

    public void run() {
        init();
        loop();
        destroy();
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
       // glfwSetCursorPosCallback(glfwWindow, this::mouseCallback);
       // glfwSetScrollCallback(glfwWindow, this::scrollCallback);
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

        /*
        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
         */

        glEnable(GL_DEPTH_TEST);
       // glEnable(GL_CULL_FACE);

        glViewport(0, 0, width, height);

        this.scene = new Scene(camera);
    }

    public void loop() {
        scene.start();

        while (!glfwWindowShouldClose(glfwWindow)) {
            float currentFrame = (float)glfwGetTime();
            dt = currentFrame - lastFrame;
            lastFrame = currentFrame;

            processInput(glfwWindow);

            // draw scene
            glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            scene.update(dt);
            scene.render();

            // draw skybox
           // glDepthFunc(GL_LEQUAL); // change depth function so depth test passes when values are equal to depth buffer's content
           // skybox.render();
           // glDepthFunc(GL_LESS); // reset depth function

            glfwSwapBuffers(glfwWindow);
            glfwPollEvents();
        }

        scene.destroy();
    }

    private void processInput(long window) {
        if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS) {
            glfwSetWindowShouldClose(window, true);
        }
        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
            camera.handleKeyboard(Camera.Movement.FORWARD, dt);
        }
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
            camera.handleKeyboard(Camera.Movement.BACKWARD, dt);
        }
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
            camera.handleKeyboard(Camera.Movement.LEFT, dt);
        }
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
            camera.handleKeyboard(Camera.Movement.RIGHT, dt);
        }
        if (glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS) {
            camera.handleKeyboard(Camera.Movement.UP, dt);
        }
        if (glfwGetKey(window, GLFW_KEY_LEFT_CONTROL) == GLFW_PRESS) {
            camera.handleKeyboard(Camera.Movement.DOWN, dt);
        }
        if (glfwGetKey(window, GLFW_KEY_COMMA) == GLFW_PRESS) {
            camera.resetZoom();
        }
    }

    private void framebufferSizeCallback(long window, int width, int height) {
        this.width = width;
        this.height = height;
        glViewport(0, 0, width, height);
    }

    private void mouseCallback(long window, double xPos, double yPos) {
        if (firstMouse) {
            lastX = (float)xPos;
            lastY = (float)yPos;
            firstMouse = false;
        }

        float xOffset = (float)xPos - lastX;
        float yOffset = lastY - (float)yPos; // reversed since y-coordinates go from bottom to top

        lastX = (float)xPos;
        lastY = (float)yPos;

        camera.handleMouseMovement(xOffset, yOffset, true);
    }

    private void scrollCallback(long window, double xOffset, double yOffset) {
        camera.handleMouseScroll((float)yOffset);
    }

    public void destroy() {
        // Free the allocated memory
        glfwFreeCallbacks(glfwWindow);
        glfwDestroyWindow(glfwWindow);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null);
    }

    public static int getWidth() {
        return Window.window.width;
    }

    public static int getHeight() {
        return Window.window.height;
    }

    public static Scene getScene() {
        return Window.window.scene;
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
