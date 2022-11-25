package com.github.wnebyte.minecraft.scenes.test;

import org.joml.Vector2f;
import org.joml.Vector3f;
import com.github.wnebyte.minecraft.ui.*;
import com.github.wnebyte.minecraft.world.BlockMap;
import com.github.wnebyte.minecraft.core.Application;
import com.github.wnebyte.minecraft.core.Scene;
import com.github.wnebyte.minecraft.core.Camera;
import com.github.wnebyte.minecraft.core.MouseListener;
import com.github.wnebyte.minecraft.renderer.Renderer;
import com.github.wnebyte.minecraft.renderer.Sprite;
import com.github.wnebyte.minecraft.renderer.Spritesheet;
import com.github.wnebyte.minecraft.renderer.fonts.JFont;
import com.github.wnebyte.minecraft.util.Assets;
import static com.github.wnebyte.minecraft.core.KeyListener.isKeyBeginPress;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

public class JGuiTestScene {

    private static final float CROSSHAIR_SIZE = 0.08f;

    private static final float CROSSHAIR_HALF_SIZE = CROSSHAIR_SIZE / 2.0f;

    private static final Vector3f CROSSHAIR_COLOR = new Vector3f(177.0f / 255.0f, 199.0f / 255.0f, 179.0f / 255.0f);

    private Camera camera;

    private Renderer renderer;

    private Spritesheet spritesheet;

    private Sprite regSprite;

    private Sprite invSprite;

    private Sprite btnRegSprite;

    private Sprite btnHoverSprite;

    private Sprite btnClickSprite;

    private JImageButton imageButton;

    private JButton button;

    private JFont font;

    private boolean focused = false;

    private JString string = new JString("TXT");

    private JImage image;

    public JGuiTestScene() {
        this.camera = new Camera.Builder()
                .setPosition(0.0f, 0.0f, 3.0f)
                .setMovementSpeed(10f)
                .setZFar(10_000f)
                .build();
        this.camera.lock();
        this.renderer = Renderer.getInstance();
    }

    public void start() {
       // super.loadResources();
        this.spritesheet = Assets.getSpritesheet(Assets.DIR + "/images/spritesheets/hudSprites.png");
        this.btnRegSprite = spritesheet.getSprite(0);
        this.btnHoverSprite = spritesheet.getSprite(1);
        this.btnClickSprite = spritesheet.getSprite(2);
        this.regSprite = spritesheet.getSprite(6);
        this.invSprite = spritesheet.getSprite(7);
        this.font = Assets.getFont(Assets.DIR + "/fonts/Minecraft.ttf", 16);
        this.imageButton = new JImageButton.Builder()
                .setSize(0.9f, 0.2f)
                .setDefaultSprite(btnRegSprite)
                .setHoverSprite(btnHoverSprite)
                .setClickSprite(btnClickSprite)
                .setText("BTN")
                .setTextScale(0.0040f)
                .build();
        this.button = new JButton.Builder()
                .setSize(0.9f, 0.2f)
                .setDefaultColor(0x0000)
                .setHoverColor(0x1111)
                .setClickColor(0x2222)
                .setText("BUTTON")
                .setTextScale(0.0045f)
                .build();
        this.image = new JImage.Builder()
                .setSprite(BlockMap.getTextureFormat("dirt").getAsSprite())
                .build();
    }

    public void processInput(float dt) {
        if (isKeyBeginPress(GLFW_KEY_ESCAPE)) {
            Application.getWindow().setWindowShouldClose(true);
        }
    }

    public void update(float dt) {
        // draw cursor
        float x = MouseListener.getScreenX();
        float y = MouseListener.getScreenY();
        //drawCursor(x, y, CROSSHAIR_HALF_SIZE, new Vector3f(0f, 0f, 0f));
        // draw gui
        JGui.begin(-3.0f, 1.3f, 6.0f, 3.0f / 6f, 0xFFFFFF);
        JGui.centerNextElement();
        JGui.label("SELECT WORLD", 0.0045f, 0x000000);
        JGui.end();
       // JGui.begin(-3.0f, -1.5f + (3.0f / 6.0f), 6.0f, 3.0f / 6.0f, 0xFFFF);
       // JGui.end();
    }

    public void render() {
        renderer.flush(camera);
    }

    public void destroy() {
        renderer.destroy();
    }

    public Camera getCamera() {
        return camera;
    }
}
