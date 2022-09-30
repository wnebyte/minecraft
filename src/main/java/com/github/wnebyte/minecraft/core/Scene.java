package com.github.wnebyte.minecraft.core;

import java.util.Random;

import com.github.wnebyte.minecraft.componenets.*;
import com.github.wnebyte.minecraft.renderer.*;
import com.github.wnebyte.minecraft.util.Assets;
import com.github.wnebyte.minecraft.util.BlockMap;
import com.github.wnebyte.minecraft.util.TexturePacker;
import com.github.wnebyte.minecraft.world.World;

public class Scene {

    private Camera camera;

    private Frustrum frustrum;

    private float debounceTime = 0.2f;

    private float debounce = debounceTime;

    private Renderer renderer;

    private Chunk chunk;

    private Random rand;

    private Texture texture;

    private World world;

    public Scene(Camera camera) {
        this.camera = camera;
        this.frustrum = new Frustrum();
        this.renderer = new Renderer(camera, frustrum);
        this.rand = new Random();
        this.world = new World(camera);
    }

    private void loadResources() {
        TexturePacker.pack(
                Assets.DIR + "/images/blocks",
                Assets.DIR + "/config/textureFormat.json",
                Assets.DIR + "/images/generated/packedTextures.png",
                false, 32, 32);
        BlockMap.load(
                Assets.DIR + "/config/textureFormat.json",
                Assets.DIR + "/config/blockFormat.json");
        texture = Assets.getTexture(Assets.DIR + "/images/generated/packedTextures.png");
    }

    public void start() {
        loadResources();
        world.start(this);
    }

    public void update(float dt) {
        /*
        debounce -= dt;
        Vector3f pos = camera.getPosition();
        String s = String.format("%.0f, %.0f, %.0f", pos.x, pos.y, pos.z);
        Text2D text = new Text2D(s, -3.6f, 1.5f, 0.0075f, 0xFFFF);
        renderer.clearText2D();
        renderer.addText2D(text);

        if (MouseListener.isMouseButtonDown(GLFW_MOUSE_BUTTON_LEFT) && debounce < 0) {
            int x = rand.nextInt(Chunk.WIDTH);
            int y = rand.nextInt(11);
            int z = Chunk.DEPTH - 1;
            chunk.setBlock(Block.AIR, x, y, z);
            chunk.generateMesh();
            debounce = debounceTime;
            System.out.printf("x: %d, y: %d, z: %d%n", x, y, z);
        }
         */
    }

    /*
    public void render() {
        world.render();
        Shader shader = Assets.getShader("C:/users/ralle/dev/java/minecraft/assets/shaders/opaque.glsl");
        shader.use();
        shader.uploadMatrix4f(Shader.U_PROJECTION, camera.getProjectionMatrix());
        shader.uploadMatrix4f(Shader.U_VIEW, camera.getViewMatrix());
        glActiveTexture(GL_TEXTURE0);
        texture.bind();
        shader.uploadTexture(Shader.U_TEXTURE, 0);
        texture.unbind();
        shader.detach();
        renderer.render();
    }
     */

    public void render() {
        world.render();
    }

    public void destroy() {
        world.destroy();
        renderer.destroy();
    }

    public Camera getCamera() {
        return camera;
    }

    public Frustrum getFrustrum() {
        return frustrum;
    }
}
