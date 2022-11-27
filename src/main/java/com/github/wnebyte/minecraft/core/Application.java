package com.github.wnebyte.minecraft.core;

import java.util.Queue;
import java.util.concurrent.Future;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;
import java.util.Calendar;
import java.nio.ByteBuffer;
import org.lwjgl.BufferUtils;
import com.github.wnebyte.minecraft.renderer.*;
import com.github.wnebyte.minecraft.util.Assets;
import com.github.wnebyte.minecraft.util.AppData;
import com.github.wnebyte.minecraft.util.ImageIO;
import com.github.wnebyte.minecraft.util.Constants;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public class Application {

    /*
    ###########################
    #        UTILITIES        #
    ###########################
    */

    protected static class Configuration {

        private String title;

        private int width;

        private int height;

        private Supplier<Scene> scene;

        public void setTitle(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getWidth() {
            return width;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getHeight() {
            return height;
        }

        public void setScene(Supplier<Scene> scene) {
            this.scene = scene;
        }

        public Supplier<Scene> getScene() {
            return scene;
        }
    }

    public static void launch(Application app) {
        if (Application.app == null) {
            Application.app = app;
            Configuration conf = new Configuration();
            app.configure(conf);
            app.run(conf);
        } else {
            throw new IllegalStateException(
                    "Application has already been launched."
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

    private boolean takeScreenshot;

    private String screenshotName;

    /*
    ###########################
    #       CONSTRUCTORS      #
    ###########################
    */

    protected Application() {}

    /*
    ###########################
    #          METHODS        #
    ###########################
    */

    protected void configure(Configuration conf) {}

    private void run(Configuration conf) {
        init(conf);
        loop();
    }

    private void init(Configuration conf) {
        thread = Thread.currentThread().getId();
        threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);
        messageQueue = new ConcurrentLinkedQueue<>();
        window = Window.newInstance(conf.getTitle());
        window.setScene(conf.getScene().get());
        ScreenRenderer.start();
        framebuffer = new Framebuffer.Builder()
                .setSize(window.getWidth(), window.getHeight())
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
                // depth
                .setDepthAttachment(new Texture(new Texture.Configuration.Builder()
                        .setTarget(GL_TEXTURE_2D)
                        .setSize(window.getWidth(), window.getHeight())
                        .setInternalFormat(GL_DEPTH_COMPONENT)
                        .setFormat(GL_DEPTH_COMPONENT)
                        .setType(GL_FLOAT)
                        .build()))
                .build();
    }

    private void loop() {
        Shader shader = Assets.getShader(Assets.DIR + "/shaders/screen.glsl");

        while (!window.shouldClose()) {
            float currentFrame = (float)glfwGetTime();
            dt = currentFrame - lastFrame;
            lastFrame = currentFrame;

            Runnable msg;
            while ((msg = messageQueue.poll()) != null) {
                msg.run();
                currentFrame = (float)glfwGetTime();
                float dt = currentFrame - lastFrame;
                if (dt >= (1f / 60f) * 0.15f) {
                    break;
                }
            }

            window.pollEvents(dt);
            // Render pass 1:
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

            // reset render states
            glEnable(GL_DEPTH_TEST);

            window.swapBuffers();
            KeyListener.endFrame();
            MouseListener.endFrame();

            if (takeScreenshot) {
                int width = framebuffer.getWidth();
                int height = framebuffer.getHeight();
                Calendar calendar = Calendar.getInstance();
                String time = String.format("%d-%d-%d",
                        calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
                String path = AppData.SCREENSHOTS_DIR + "/" + screenshotName + "-" + time + ".png";
                ByteBuffer pixels = BufferUtils.createByteBuffer(width * height * 4);
                glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
                ImageIO.write(path, width, height, 4, pixels, true);
                System.out.printf("Screenshot: '%s' was saved!%n", path);
                takeScreenshot = false;
            }
        }

        window.destroy();
    }

    public static void takeScreenshot(String name) {
        Application.app.screenshotName = name;
        Application.app.takeScreenshot = true;
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

    public static <T> Future<T> submit(Runnable task, T result) {
        return Application.app.threadPool.submit(task, result);
    }

    public static void runLater(Runnable task) {
        Application.app.messageQueue.add(task);
    }

    public static boolean isApplicationThread() {
        return (Thread.currentThread().getId() == Application.app.thread);
    }
}
