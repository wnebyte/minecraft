package com.github.wnebyte.minecraft.core;

import java.util.Queue;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.Future;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import com.github.wnebyte.minecraft.renderer.*;
import com.github.wnebyte.minecraft.util.Assets;
import com.github.wnebyte.minecraft.util.Constants;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public class Application {

    /*
    ###########################
    #         UTLITIES        #
    ###########################
    */

    public static void launch() {
        if (Application.app == null) {
            Application.app = new Application();
            Application.app.run();
        } else {
            throw new IllegalStateException(
                    "Application has already been launched"
            );
        }
    }

    /*
    ###########################
    #      STATIC FIELDS      #
    ###########################
    */

    private static Application app;

    /*
    ###########################
    #          FIELDS         #
    ###########################
    */

    private long thread;

    private ExecutorService threadPool;

    private Queue<Runnable> messageQueue;

    private Window window;

    private Framebuffer framebuffer;

    private float dt = 0.0f;

    private float lastFrame = 0.0f;

    /*
    ###########################
    #       CONSTRUCTORS      #
    ###########################
    */

    private Application() {}

    /*
    ###########################
    #          METHODS        #
    ###########################
    */

    private void run() {
        init();
        loop();
    }

    private void init() {
        thread = Thread.currentThread().getId();
        threadPool = Executors.newFixedThreadPool(7);
        messageQueue = new LinkedList<>();
        window = Window.newInstance("Minecraft");
        window.init();
        window.setScene(new Scene());
        ScreenRenderer.start();
        framebuffer = new Framebuffer(new Framebuffer.Configuration.Builder()
                // opaque
                .addColorAttachment(new Texture(new Texture.Configuration.Builder()
                        .setTarget(GL_TEXTURE_2D)
                        .setSize(window.getWidth(), window.getHeight())
                        .setInternalFormat(GL_RGBA16F)
                        .setFormat(GL_RGBA)
                        .setType(GL_HALF_FLOAT)
                        .addParameter(GL_TEXTURE_MIN_FILTER, GL_LINEAR)
                        .addParameter(GL_TEXTURE_MAG_FILTER, GL_LINEAR)
                        .build()))
                // accum
                .addColorAttachment(new Texture(new Texture.Configuration.Builder()
                        .setTarget(GL_TEXTURE_2D)
                        .setSize(window.getWidth(), window.getHeight())
                        .setInternalFormat(GL_RGBA16F)
                        .setFormat(GL_RGBA)
                        .setType(GL_HALF_FLOAT)
                        .addParameter(GL_TEXTURE_MIN_FILTER, GL_LINEAR)
                        .addParameter(GL_TEXTURE_MAG_FILTER, GL_LINEAR)
                        .build()))
                // reveal
                .addColorAttachment(new Texture(new Texture.Configuration.Builder()
                        .setTarget(GL_TEXTURE_2D)
                        .setSize(window.getWidth(), window.getHeight())
                        .setInternalFormat(GL_R8)
                        .setFormat(GL_RED)
                        .setType(GL_FLOAT)
                        .addParameter(GL_TEXTURE_MIN_FILTER, GL_LINEAR)
                        .addParameter(GL_TEXTURE_MAG_FILTER, GL_LINEAR)
                        .build()))
                .setDepthAttachment(new Texture(new Texture.Configuration.Builder()
                        .setTarget(GL_TEXTURE_2D)
                        .setSize(window.getWidth(), window.getHeight())
                        .setInternalFormat(GL_DEPTH_COMPONENT)
                        .setFormat(GL_DEPTH_COMPONENT)
                        .setType(GL_FLOAT)
                        .build()))
                .build());
    }

    private void loop() {
        Shader shader = Assets.getShader(Assets.DIR + "/shaders/screen.glsl");

        while (!window.shouldClose()) {
            float currentFrame = (float)glfwGetTime();
            dt = currentFrame - lastFrame;
            lastFrame = currentFrame;

            Iterator<Runnable> it = messageQueue.iterator();
            while (it.hasNext()) {
                Runnable task = it.next();
                task.run();
            }

            framebuffer.bind();
            glDrawBuffers(Constants.BUFS_ZERO_NONE_NONE);
            glClearBufferfv(GL_COLOR, 0, Constants.ZERO_FILLER_VEC);
            glClearBufferfv(GL_DEPTH, 0, Constants.ONE_FILLER_VEC);
            window.update(dt);

            // Render pass 4:
            // set backbuffer render states
            glDisable(GL_DEPTH_TEST);
            glDepthMask(true);
            glDisable(GL_BLEND);

            // bind backbuffer
            framebuffer.unbind();
            glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

            // draw final screen quad
            shader.use();
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, framebuffer.getColorAttachment(0).getId());
            shader.uploadTexture(Shader.SCREEN, 0);
            ScreenRenderer.render();
            shader.detach();

            window.swapBuffers();
            window.pollEvents(dt);
            KeyListener.endFrame();
        }

        window.destroy();
    }

    public static Framebuffer getFramebuffer() {
        return Application.app.framebuffer;
    }

    public static Window getWindow() {
        return Application.app.window;
    }

    public static float dt() {
        return Application.app.dt;
    }

    public static Future<?> submit(Runnable task) {
        return Application.app.threadPool.submit(task);
    }

    public static void runLater(Runnable task) {
        Application.app.messageQueue.add(task);
    }

    public static boolean isApplicationThread() {
        return (Thread.currentThread().getId() == Application.app.thread);
    }
}
