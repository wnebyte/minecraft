package com.github.wnebyte.minecraft.scenes;

import com.github.wnebyte.minecraft.core.Application;
import com.github.wnebyte.minecraft.core.Scene;
import com.github.wnebyte.minecraft.core.Camera;
import com.github.wnebyte.minecraft.world.World;
import com.github.wnebyte.minecraft.world.BlockMap;
import com.github.wnebyte.minecraft.renderer.Texture;
import com.github.wnebyte.minecraft.ui.JGui;
import com.github.wnebyte.minecraft.util.Assets;
import com.github.wnebyte.minecraft.util.TerrainGenerator;
import com.github.wnebyte.minecraft.util.TexturePacker;
import static org.lwjgl.opengl.GL11.*;

public class LoadingScene extends Scene {

    /*
    ###########################
    #        UTILITIES        #
    ###########################
    */

    public static class ResourceLoader implements Runnable {

        private final String blockTexturePath = Assets.DIR + "/images/generated/packedTextures.png";

        private final String itemTexturePath = Assets.DIR + "/images/generated/packedItemTextures.png";

        private final String blockItemTexturePath = Assets.DIR + "/images/generated/packedBlockItemTextures.png";

        public ResourceLoader() {}

        @Override
        public void run() {
            TexturePacker packer = new TexturePacker(true, true);

            // load blocks
            packer.pack(
                    Assets.DIR + "/images/blocks",
                    Assets.DIR + "/config/textureFormat.json",
                    blockTexturePath, false, 32, 32);
            Texture blockTexture = new Texture(blockTexturePath, new Texture.Configuration.Builder()
                    .setTarget(GL_TEXTURE_2D)
                    .flip()
                    .addParameter(GL_TEXTURE_MIN_FILTER, GL_NEAREST)
                    .addParameter(GL_TEXTURE_MAG_FILTER, GL_NEAREST)
                    .build());
            Assets.addTexture(blockTexture);
            BlockMap.loadBlocks(
                    Assets.DIR + "/config/blockFormat.json",
                    Assets.DIR + "/config/textureFormat.json",
                    blockTexturePath);
            BlockMap.bufferTexCoords();

            // load items
            packer = new TexturePacker();
            packer.pack(
                    Assets.DIR + "/images/items",
                    Assets.DIR + "/config/itemTextureFormat.json",
                    itemTexturePath, false, 32, 32);
            Texture itemTexture = new Texture(itemTexturePath, new Texture.Configuration.Builder()
                    .setTarget(GL_TEXTURE_2D)
                    .addParameter(GL_TEXTURE_MIN_FILTER, GL_NEAREST)
                    .addParameter(GL_TEXTURE_MAG_FILTER, GL_NEAREST)
                    .build());
            Assets.addTexture(itemTexture);
            BlockMap.loadItems(
                    Assets.DIR + "/config/itemFormat.json",
                    Assets.DIR + "/config/itemTextureFormat.json",
                    itemTexturePath);

            // generate and load block items
            BlockMap.generateBlockItemImages(
                    Assets.DIR + "/config/blockFormat.json",
                    Assets.DIR + "/images/generated/blockItems");
            packer.pack(
                    Assets.DIR + "/images/generated/blockItems",
                    Assets.DIR + "/config/blockItemTextureFormat.json",
                    blockItemTexturePath, false, 32, 32);
            Texture blockItemTexture = new Texture(blockItemTexturePath, new Texture.Configuration.Builder()
                    .setTarget(GL_TEXTURE_2D)
                    .addParameter(GL_TEXTURE_MIN_FILTER, GL_NEAREST)
                    .addParameter(GL_TEXTURE_MAG_FILTER, GL_NEAREST)
                    .build());
            Assets.addTexture(blockItemTexture);
            BlockMap.loadBlockItems(
                    Assets.DIR + "/config/blockItemTextureFormat.json",
                    blockItemTexturePath);

            // load remaining assets
            TerrainGenerator.load(
                    Assets.DIR + "/config/terrainNoise.json",
                    (int)System.currentTimeMillis());
        }
    }

    /*
    ###########################
    #          FIELDS         #
    ###########################
    */

    private GameScene gameScene;

    private int frames;

    private final ResourceLoader resourceLoader;

    /*
    ###########################
    #       CONSTRUCTORS      #
    ###########################
    */

    public LoadingScene(Camera camera) {
        super(camera);
        this.resourceLoader = new ResourceLoader();
        this.frames = 0;
    }

    /*
    ###########################
    #          METHODS        #
    ###########################
    */

    @Override
    public void start() {
        // do nothing
    }

    @Override
    public void update(float dt) {
        long count = (gameScene == null) ? 0L : gameScene.getCounter();
        long percentage = (count / World.CHUNK_SPAWN_AREA) * 100;
        JGui.begin(-3.0f, 1.3f, 6.0f, 3.0f);
        JGui.advanceCursor(0.0f, 1.4f);
        JGui.centerNextElement();
        JGui.label(String.format("%d%s", percentage, "%"), 0.0045f, 0xFFFFFF);
        JGui.end();

        if (frames == -1 && count >= World.CHUNK_SPAWN_AREA) {
            camera.unlock();
            Application.getWindow().setScene(gameScene);
        }
    }

    @Override
    public void processInput(float dt) {
        // do nothing
    }

    @Override
    public void render() {
        renderer.flush(camera);
        if (frames == 2) {
            resourceLoader.run();
            gameScene = new GameScene(camera);
            gameScene.start();
            frames = -1;
        } else if (frames >= 0) {
            frames++;
        }
    }

    @Override
    public void destroy() {
        // do nothing
    }
}
