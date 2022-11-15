package com.github.wnebyte.minecraft.scenes;

import com.github.wnebyte.minecraft.core.Application;
import com.github.wnebyte.minecraft.renderer.fonts.JFont;
import org.joml.Vector2f;
import com.github.wnebyte.minecraft.core.Scene;
import com.github.wnebyte.minecraft.core.Camera;
import com.github.wnebyte.minecraft.renderer.Renderer;
import com.github.wnebyte.minecraft.renderer.Sprite;
import com.github.wnebyte.minecraft.renderer.Spritesheet;
import com.github.wnebyte.minecraft.ui.JGui;
import com.github.wnebyte.minecraft.util.Assets;

import static com.github.wnebyte.minecraft.core.KeyListener.isKeyBeginPress;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

public class JGuiTestScene extends Scene {

    private Camera camera;

    private Renderer renderer;

    private Spritesheet spritesheet;

    private Sprite regSprite;

    private Sprite sprite;

    private JFont font;

    public JGuiTestScene() {
        this.camera = new Camera.Builder()
                .setPosition(0.0f, 0.0f, 3.0f)
                .setMovementSpeed(10f)
                .setZFar(10_000f)
                .build();
        this.renderer = Renderer.getInstance();
    }

    @Override
    public void start() {
        super.loadResources();
        this.spritesheet = Assets.getSpritesheet(Assets.DIR + "/images/spritesheets/hudSprites.png");
        this.regSprite = spritesheet.getSprite(6);
        this.sprite = spritesheet.getSprite(7);
        this.font = Assets.getFont(Assets.DIR + "/fonts/Minecraft.ttf", 16);
    }

    @Override
    public void processInput(float dt) {
        if (isKeyBeginPress(GLFW_KEY_ESCAPE)) {
            Application.getWindow().setWindowShouldClose(true);
        }
    }

    @Override
    public void update(float dt) {
        JGui.begin(new Vector2f(-2.95f, 1.2f), new Vector2f(2f, 2f));
        JGui.label("LBL", 0.0040f, 0x0000);
        JGui.sameLine();
        JGui.label("LBL", 0.0040f, 0x0000);
        JGui.image(sprite, 0.5f, 0.5f);
        JGui.image(sprite, 0.5f, 0.5f);
        JGui.end();
    }

    @Override
    public void render() {
        renderer.flush(camera);
    }

    @Override
    public Camera getCamera() {
        return camera;
    }
}
