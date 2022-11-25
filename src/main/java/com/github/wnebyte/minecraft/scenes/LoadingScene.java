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

    private static class ResourceLoader implements Runnable {

        private String blockTexturePath = Assets.DIR + "/images/generated/packedTextures.png";

        private String itemTexturePath = Assets.DIR + "/images/generated/packedItemTextures.png";

        private String blockItemTexturePath = Assets.DIR + "/images/generated/packedBlockItemTextures.png";

        private Texture blockTexture;

        private Texture itemTexture;

        private Texture blockItemTexture;

        public ResourceLoader() {
            blockTexture = new Texture(blockTexturePath, new Texture.Configuration.Builder()
                    .setTarget(GL_TEXTURE_2D)
                    .flip()
                    .addParameter(GL_TEXTURE_MIN_FILTER, GL_NEAREST)
                    .addParameter(GL_TEXTURE_MAG_FILTER, GL_NEAREST)
                    .build());
            itemTexture = new Texture(itemTexturePath, new Texture.Configuration.Builder()
                    .setTarget(GL_TEXTURE_2D)
                    .addParameter(GL_TEXTURE_MIN_FILTER, GL_NEAREST)
                    .addParameter(GL_TEXTURE_MAG_FILTER, GL_NEAREST)
                    .build());
            blockItemTexture = new Texture(blockItemTexturePath, new Texture.Configuration.Builder()
                    .setTarget(GL_TEXTURE_2D)
                    .addParameter(GL_TEXTURE_MIN_FILTER, GL_NEAREST)
                    .addParameter(GL_TEXTURE_MAG_FILTER, GL_NEAREST)
                    .build());
        }

        @Override
        public void run() {
            TexturePacker packer = new TexturePacker(true, true);

            // load blocks
            packer.pack(
                    Assets.DIR + "/images/blocks",
                    Assets.DIR + "/config/textureFormat.json",
                    blockTexturePath, false, 32, 32);
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

    private final ResourceLoader resourceLoader;

    /*
    ###########################
    #       CONSTRUCTORS      #
    ###########################
    */

    public LoadingScene(Camera camera) {
        super(camera);
        this.resourceLoader = new ResourceLoader();
    }

    /*
    ###########################
    #          METHODS        #
    ###########################
    */

    @Override
    public void start() {
        resourceLoader.run();
        gameScene = new GameScene(camera);
        gameScene.start();
    }

    @Override
    public void update(float dt) {
        long count = gameScene.getCounter();
        long percentage = (count / World.CHUNK_SPAWN_AREA) * 100;
        JGui.begin(-3.0f, 1.3f, 6.0f, 3.0f);
        JGui.advanceCursor(0.0f, 1.4f);
        JGui.centerNextElement();
        JGui.label(String.format("%d%s", percentage, "%"), 0.0045f, 0xFFFFFF);
        JGui.end();

        if (count >= World.CHUNK_SPAWN_AREA) {
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
    }

    @Override
    public void destroy() {
        // do nothing
    }
}
