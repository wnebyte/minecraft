package com.github.wnebyte.minecraft.scenes;

import com.github.wnebyte.minecraft.core.Application;
import com.github.wnebyte.minecraft.core.Scene;
import com.github.wnebyte.minecraft.core.Camera;
import com.github.wnebyte.minecraft.core.MouseListener;
import com.github.wnebyte.minecraft.renderer.Sprite;
import com.github.wnebyte.minecraft.renderer.Spritesheet;
import com.github.wnebyte.minecraft.ui.JGui;
import com.github.wnebyte.minecraft.ui.JImageButton;
import com.github.wnebyte.minecraft.util.Assets;

public class MainMenuScene extends Scene {

    /*
    ###########################
    #          FIELDS         #
    ###########################
    */

    private final JImageButton loadButton;

    private final JImageButton newButton;

    private final JImageButton exitButton;

    /*
    ###########################
    #       CONSTRUCTORS      #
    ###########################
    */

    public MainMenuScene() {
        this(DEFAULT_CAMERA);
    }

    public MainMenuScene(Camera camera) {
        super(camera);
        Spritesheet spritesheet = Assets.loadSpritesheet(Assets.DIR + "/config/hudSprites.json");
        Sprite btnRegSprite   = spritesheet.getSprite(0);
        Sprite btnHoverSprite = spritesheet.getSprite(1);
        Sprite btnClickSprite = spritesheet.getSprite(2);
        this.loadButton = new JImageButton.Builder()
                .setSize(0.9f, 0.2f)
                .setDefaultSprite(btnRegSprite)
                .setHoverSprite(btnHoverSprite)
                .setClickSprite(btnClickSprite)
                .setText("LOAD")
                .setTextScale(0.0045f)
                .build();
        this.newButton = new JImageButton.Builder()
                .setSize(0.9f, 0.2f)
                .setDefaultSprite(btnRegSprite)
                .setHoverSprite(btnHoverSprite)
                .setClickSprite(btnClickSprite)
                .setText("NEW")
                .setTextScale(0.0045f)
                .build();
        this.exitButton = new JImageButton.Builder()
                .setSize(0.9f, 0.2f)
                .setDefaultSprite(btnRegSprite)
                .setHoverSprite(btnHoverSprite)
                .setClickSprite(btnClickSprite)
                .setText("EXIT")
                .setTextScale(0.0045f)
                .build();
    }

    /*
    ###########################
    #          METHODS        #
    ###########################
    */

    @Override
    public void start() {
       camera.lock();
    }

    @Override
    public void update(float dt) {
        // draw cursor
        float x = MouseListener.getScreenX();
        float y = MouseListener.getScreenY();
        drawCursor(x, y, CROSSHAIR_HALF_SIZE, CROSSHAIR_COLOR);
        // draw gui
        JGui.begin(-3.0f, 1.3f, 6.0f, 3.0f);
        JGui.advanceCursor(0.0f, 4.5f * (0.2f + 0.02f));
        JGui.centerNextElement();
        if (JGui.imageButton(newButton)) {
            System.out.println("new");
        }
        JGui.centerNextElement();
        if (JGui.imageButton(loadButton)) {
            Application.getWindow().setScene(new LoadingScene(camera));
        }
        JGui.centerNextElement();
        if (JGui.imageButton(exitButton)) {
            Application.getWindow().setWindowShouldClose(true);
        }
        JGui.end();
    }

    @Override
    public void processInput(float dt) {
        // do nothing
    }

    @Override
    public void render() {
        renderer.flush(camera);
    }

    @Override
    public void destroy() {
        // do nothing
    }
}
