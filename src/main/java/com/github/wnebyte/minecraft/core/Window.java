package com.github.wnebyte.minecraft.core;

import java.util.List;
import java.util.Arrays;
import java.util.Random;

import com.github.wnebyte.minecraft.renderer.Renderer;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWErrorCallback;
import com.github.wnebyte.minecraft.renderer.Texture;
import com.github.wnebyte.minecraft.components.Block;
import com.github.wnebyte.minecraft.util.Assets;
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

    private Block lightSource;

    private Vector3f lightSourcePos;

    private Window() {
        this.title = "Window";
        this.camera = new Camera(
                new Vector3f(0.0f, 0.0f, 3.0f),  // position
                new Vector3f(0.0f, 0.0f, -1.0f), // front
                new Vector3f(0.0f, 1.0f, 0.0f)); // up
        this.lightSourcePos = new Vector3f(1.2f, 1.0f, 2.0f);
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
        glfwSetCursorPosCallback(glfwWindow, this::mouseCallback);
        glfwSetScrollCallback(glfwWindow, this::scrollCallback);
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
    }

    public void loop() {
        Renderer renderer = new Renderer(2, camera);
        createLightSource();

        List<Texture> textures = Arrays.asList(
                Assets.getTexture("C:/users/ralle/dev/java/minecraft/assets/images/bricks.png"),
                Assets.getTexture("C:/users/ralle/dev/java/minecraft/assets/images/chiseled_quartz_block.png"),
                Assets.getTexture("C:/users/ralle/dev/java/minecraft/assets/images/chiseled_sandstone.png"),
                Assets.getTexture("C:/users/ralle/dev/java/minecraft/assets/images/chiseled_stone_bricks.png"));
        Block block = new Block(new Transform(
                new Vector3f(0.0f, 0.0f, 0.0f),
                new Vector3f(1.0f, 1.0f, 1.0f),
                0.0f
        ), null, new Vector4f(1.0f, 0.5f, 0.31f, 1.0f));

        renderer.addLs(lightSource);
        renderer.add(block);
        renderer.start();

        while (!glfwWindowShouldClose(glfwWindow)) {
            float currentFrame = (float)glfwGetTime();
            dt = currentFrame - lastFrame;
            lastFrame = currentFrame;

            processInput(glfwWindow);

           // glClearColor(39.0f / 255.0f, 40.0f / 255.0f, 34.0f / 255.0f, 1.0f);
            glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            if (dt >= 0) {
                renderer.render();
            }

            glfwSwapBuffers(glfwWindow);
            glfwPollEvents();
        }

        renderer.destroy();
    }

    private void createLightSource() {
        this.lightSource = new Block(new Transform(
                new Vector3f(lightSourcePos),
                new Vector3f(0.2f, 0.2f, 0.2f),
                0.0f));
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
        if (glfwGetKey(window, GLFW_KEY_DOWN) == GLFW_PRESS) {
            float movementSpeed = 2.5f;
            float velocity = movementSpeed * dt;
            lightSource.transform.position.z += velocity;
            lightSource.setDirty();
        }
        if (glfwGetKey(window, GLFW_KEY_UP) == GLFW_PRESS) {
            float movementSpeed = 2.5f;
            float velocity = movementSpeed * dt;
            lightSource.transform.position.z -= velocity;
            lightSource.setDirty();
        }
        if (glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS) {
            float movementSpeed = 2.5f;
            float velocity = movementSpeed * dt;
            lightSource.transform.position.x += velocity;
            lightSource.setDirty();
        }
        if (glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS) {
            float movementSpeed = 2.5f;
            float velocity = movementSpeed * dt;
            lightSource.transform.position.x -= velocity;
            lightSource.setDirty();
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

        camera.handleMouseMovement(xOffset, yOffset, false);
    }

    private void scrollCallback(long window, double xOffset, double yOffset) {
        camera.handleMouseScroll((float)yOffset);
    }

    /*
    public void loop() {
        float beginTime = (float)glfwGetTime();
        float endTime;
        float dt = -1.0f;

        Vector3f position = new Vector3f(0.0f, 0.0f, 0.0f);
        float aspect = (float)(width / height);
        float fov = 70.0f;
        float zNear = 0.1f;
        float zFar = 10_000.0f;
        MyCamera camera = new MyCamera(position, fov, aspect, zNear, zFar);

        Renderer renderer = new Renderer(100, null);
        renderer.start();
        createBlocks(renderer);

        Vector3f center = new Vector3f(0.0f, 0.0f, 0.0f);
        Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
        float eyeRotation = 45.0f;

        while (!glfwWindowShouldClose(glfwWindow)) {
            glfwPollEvents();
            glClearColor(39.0f/255.0f, 40.0f/255.0f, 34.0f/255.0f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            if (dt >= 0) {
                if (isKeyPressed(GLFW_KEY_SPACE)) {
                    eyeRotation += 30.0f * dt;
                }
                Vector3f eye =
                        new Vector3f(
                                (float)Math.sin(Math.toRadians(eyeRotation)) * 7.0f,
                                5.0f,
                                (float)Math.cos(Math.toRadians(eyeRotation)) * 7.0f);
                camera.lookAt(eye, center, up);
                renderer.render();
            }

            glfwSwapBuffers(glfwWindow);
            KeyListener.endFrame();
            endTime = (float)glfwGetTime();
            dt = endTime - beginTime;
            beginTime = endTime;
        }

        renderer.destroy();
    }
     */

    public void destroy() {
        // Free the allocated memory
        glfwFreeCallbacks(glfwWindow);
        glfwDestroyWindow(glfwWindow);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null);
    }

    private void createBlocks(Renderer renderer) {
        List<Texture> textures = Arrays.asList(
                Assets.getTexture("C:/users/ralle/dev/java/minecraft/assets/images/bricks.png"),
                Assets.getTexture("C:/users/ralle/dev/java/minecraft/assets/images/chiseled_quartz_block.png"),
                Assets.getTexture("C:/users/ralle/dev/java/minecraft/assets/images/chiseled_sandstone.png"),
                Assets.getTexture("C:/users/ralle/dev/java/minecraft/assets/images/chiseled_stone_bricks.png"));
        Random rand = new Random();

        for (int x = 0; x < 10; x++) {
            for (int z = 0; z < 10; z++) {
                Block block = new Block(new Transform(
                        new Vector3f(x - 5, 0, z - 5),
                        new Vector3f(1.0f, 1.0f, 1.0f),
                        0.0f),
                        textures.get(rand.nextInt(textures.size())));
                renderer.add(block);
            }
        }
    }

    public static int getWidth() {
        return Window.window.width;
    }

    public static int getHeight() {
        return Window.window.height;
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
