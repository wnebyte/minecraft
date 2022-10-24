package com.github.wnebyte.minecraft.core;

import org.joml.Vector3f;
import com.github.wnebyte.minecraft.renderer.*;
import com.github.wnebyte.minecraft.util.Assets;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public class Application {

    public static Application newInstance() {
        if (Application.app == null) {
            Application.app = new Application();
            return Application.app;
        } else {
            throw new IllegalStateException(
                    "Application has already been initialized"
            );
        }
    }

    public static final float[] ZERO_FILLER_VEC = { 0.0f, 0.0f, 0.0f, 0.0f };

    public static final float[] ONE_FILLER_VEC  = { 1.0f, 1.0f, 1.0f, 1.0f };

    public static final int[] BUFS_0NN   = { GL_COLOR_ATTACHMENT0, GL_NONE, GL_NONE };

    public static final int[] BUFS_N12 = { GL_NONE, GL_COLOR_ATTACHMENT1, GL_COLOR_ATTACHMENT2 };

    private static Application app;

    private Window window;

    private Framebuffer framebuffer;

    private Scene scene;

    private Camera camera;

    private Shader shader;

    private float dt = 0.0f;

    private float lastFrame = 0.0f;

    private Application() {
        this.camera = new Camera(
                new Vector3f(0.0f, 0.0f, 3.0f),  // position
                new Vector3f(0.0f, 0.0f, -1.0f), // forward
                new Vector3f(0.0f, 1.0f, 0.0f),  // up
                Camera.DEFAULT_YAW,
                Camera.DEFAULT_PITCH,
                10f,
                Camera.DEFAULT_MOUSE_SENSITIVITY,
                Camera.DEFAULT_ZOOM,
                Camera.DEFAULT_Z_NEAR,
                10_000f);
    }

    public void run() {
        init();
        loop();
    }

    private void init() {
        window = Window.newInstance("Title");
        ScreenRenderer.start();
        framebuffer = new Framebuffer(new Framebuffer.Specification.Builder()
                // opaque
                .addColorAttachment(new Texture(new Texture.Specification.Builder()
                        .setTarget(GL_TEXTURE_2D)
                        .setSize(window.getWidth(), window.getHeight())
                        .setInternalFormat(GL_RGBA16F)
                        .setFormat(GL_RGBA)
                        .setType(GL_HALF_FLOAT)
                        .addParameter(GL_TEXTURE_MIN_FILTER, GL_LINEAR)
                        .addParameter(GL_TEXTURE_MAG_FILTER, GL_LINEAR)
                        .build()))
                // accum
                .addColorAttachment(new Texture(new Texture.Specification.Builder()
                        .setTarget(GL_TEXTURE_2D)
                        .setSize(window.getWidth(), window.getHeight())
                        .setInternalFormat(GL_RGBA16F)
                        .setFormat(GL_RGBA)
                        .setType(GL_HALF_FLOAT)
                        .addParameter(GL_TEXTURE_MIN_FILTER, GL_LINEAR)
                        .addParameter(GL_TEXTURE_MAG_FILTER, GL_LINEAR)
                        .build()))
                // reveal
                .addColorAttachment(new Texture(new Texture.Specification.Builder()
                        .setTarget(GL_TEXTURE_2D)
                        .setSize(window.getWidth(), window.getHeight())
                        .setInternalFormat(GL_R8)
                        .setFormat(GL_RED)
                        .setType(GL_FLOAT)
                        .addParameter(GL_TEXTURE_MIN_FILTER, GL_LINEAR)
                        .addParameter(GL_TEXTURE_MAG_FILTER, GL_LINEAR)
                        .build()))
                .setDepthAttachment(new Texture(new Texture.Specification.Builder()
                        .setTarget(GL_TEXTURE_2D)
                        .setSize(window.getWidth(), window.getHeight())
                        .setInternalFormat(GL_DEPTH_COMPONENT)
                        .setFormat(GL_DEPTH_COMPONENT)
                        .setType(GL_FLOAT)
                        .build()))
                .build());
        scene = new Scene(camera);
    }

    private void loop() {
        scene.start();
        shader = Assets.getShader(Assets.DIR + "/shaders/screen.glsl");

        while (!window.shouldClose()) {
            float currentFrame = (float)glfwGetTime();
            dt = currentFrame - lastFrame;
            lastFrame = currentFrame;

            framebuffer.bind();
            glDrawBuffers(BUFS_0NN);
            glClearBufferfv(GL_COLOR, 0, ZERO_FILLER_VEC);
            glClearBufferfv(GL_DEPTH, 0, ONE_FILLER_VEC);
            scene.update(dt);
            scene.render(dt);

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
            window.pollEvents();
            window.processInput(camera, dt);
        }

        scene.destroy();
        window.destroy();
    }

    public static Framebuffer getFramebuffer() {
        return Application.app.framebuffer;
    }

    public static Window getWindow() {
        return Application.app.window;
    }

    public static Scene getScene() {
        return Application.app.scene;
    }

    public static float dt() {
        return Application.app.dt;
    }
}
