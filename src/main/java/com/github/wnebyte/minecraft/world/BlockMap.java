package com.github.wnebyte.minecraft.world;

import java.util.*;
import java.util.Map;
import java.util.Collections;
import java.io.File;
import java.nio.ByteBuffer;
import com.google.gson.*;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import com.github.wnebyte.minecraft.core.Application;
import com.github.wnebyte.minecraft.core.Transform;
import com.github.wnebyte.minecraft.core.Camera;
import com.github.wnebyte.minecraft.renderer.*;
import com.github.wnebyte.minecraft.util.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30C.GL_R32F;
import static org.lwjgl.opengl.GL31C.GL_TEXTURE_BUFFER;
import static org.lwjgl.opengl.GL31C.glTexBuffer;

public class BlockMap {

    private static final Map<Byte, Block> blocks = new HashMap<>();

    private static final Map<Short, Item> items = new HashMap<>();

    private static final Map<String, TextureFormat> textureFormats = new HashMap<>();

    private static final Map<String, TextureFormat> itemTextureFormats = new HashMap<>();

    private static final Map<String, TextureFormat> blockItemTextureFormats = new HashMap<>();

    private static final Map<String, Short> dict = new HashMap<>();

    private static int texCoordsTextureId;

    private static int texCoordsBufferId;

    public static TextureFormat getTextureFormat(String name) {
        if (textureFormats.containsKey(name)) {
            return textureFormats.get(name);
        } else {
            assert false : String.format("Error: (BlockMap) TextureFormat with name: '%s' does not exist", name);
            return null;
        }
    }

    public static TextureFormat getItemTextureFormat(String name) {
        if (itemTextureFormats.containsKey(name)) {
            return itemTextureFormats.get(name);
        } else {
            assert false : String.format("Error: (BlockMap) TextureFormat with name: '%s' does not exist", name);
            return null;
        }
    }

    public static TextureFormat getBlockItemTextureFormat(String name) {
        if (blockItemTextureFormats.containsKey(name)) {
            return blockItemTextureFormats.get(name);
        } else {
            assert false : String.format("Error: (BlockMap) TextureFormat with name: '%s' does not exist", name);
            return null;
        }
    }

    public static Block getBlock(int id) {
        if (blocks.containsKey((byte)id)) {
            return blocks.get((byte)id);
        } else {
            assert false : String.format("Error: (BlockMap) Block with id: '%s' does not exist", id);
            return null;
        }
    }

    public static Block getBlock(String name) {
        byte id = (byte)(short)dict.get(name.toLowerCase());
        return getBlock(id);
    }

    public static Item getItem(short id) {
        if (items.containsKey(id)) {
            return items.get(id);
        } else {
            assert false : String.format("Error: (BlockMap) Item with id: '%s' does not exist", id);
            return null;
        }
    }

    public static void loadBlocks(String blockConfigPath, String textureConfigPath, Texture texture) {
        String json = Files.read(textureConfigPath);
        TextureFormat[] tfs = Settings.GSON.fromJson(json, TextureFormat[].class);
        for (TextureFormat textureFormat : tfs) {
            textureFormat.setTexture(texture);
            textureFormats.put(textureFormat.getName(), textureFormat);
        }

        json = Files.read(blockConfigPath);
        JsonElement jsonElement = JsonParser.parseString(json);
        if (!jsonElement.isJsonArray()) {
            return;
        }
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        for (JsonElement e : jsonArray) {
            if (!e.isJsonObject()) {
                continue;
            }
            JsonObject jsonObject = e.getAsJsonObject();
            byte id = jsonObject.get("id").getAsByte();
            String name = jsonObject.get("name").getAsString();
            boolean solid = jsonObject.get("solid").getAsBoolean();
            boolean transparent = jsonObject.get("transparent").getAsBoolean();
            boolean blendable = jsonObject.get("blendable").getAsBoolean();
            boolean colorSideByBiome = jsonObject.get("colorSideByBiome").getAsBoolean();
            boolean colorTopByBiome = jsonObject.get("colorTopByBiome").getAsBoolean();
            boolean colorBottomByBiome = jsonObject.get("colorBottomByBiome").getAsBoolean();
            String side = jsonObject.get("side").getAsString();
            String top = jsonObject.get("top").getAsString();
            String bottom = jsonObject.get("bottom").getAsString();
            TextureFormat sideTextureFormat = textureFormats.get(side);
            TextureFormat topTextureFormat = textureFormats.get(top);
            TextureFormat bottomTextureForamt = textureFormats.get(bottom);
            Block block = new Block.Builder()
                    .setId(id)
                    .setName(name)
                    .setSideTextureFormat(sideTextureFormat)
                    .setTopTextureFormat(topTextureFormat)
                    .setBottomTextureFormat(bottomTextureForamt)
                    .setSolid(solid)
                    .setTransparent(transparent)
                    .setBlendable(blendable)
                    .setColorSideByBiome(colorSideByBiome)
                    .setColorTopByBiome(colorTopByBiome)
                    .setColorBottomByBiome(colorBottomByBiome)
                    .build();
            blocks.put(id, block);
            dict.put(name, (short)id);
        }
    }

    public static void loadItems(String itemConfigPath, String textureConfigPath, Texture texture) {
        String json = Files.read(textureConfigPath);
        TextureFormat[] tfs = Settings.GSON.fromJson(json, TextureFormat[].class);

        for (TextureFormat textureFormat : tfs) {
            textureFormat.setTexture(texture);
            itemTextureFormats.put(textureFormat.getName(), textureFormat);
        }

        json = Files.read(itemConfigPath);
        JsonElement jsonElement = JsonParser.parseString(json);
        if (!jsonElement.isJsonArray()) {
            return;
        }
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        for (JsonElement e : jsonArray) {
            if (!e.isJsonObject()) {
                continue;
            }
            JsonObject jsonObject = e.getAsJsonObject();
            short id = jsonObject.get("id").getAsShort();
            String name = jsonObject.get("name").getAsString();
            short maxStackCount = jsonObject.get("maxStackCount").getAsShort();
            TextureFormat textureFormat = itemTextureFormats.get(name);
            Item item = new Item.Builder()
                    .setId(id)
                    .setName(name)
                    .setMaxStackCount(maxStackCount)
                    .setTextureFormat(textureFormat)
                    .setIsBlock(false)
                    .build();
            items.put(id, item);
            dict.put(name, id);
        }
    }

    public static void loadBlockItems(String textureConfigPath, Texture texture) {
        String json = Files.read(textureConfigPath);
        TextureFormat[] tfs = Settings.GSON.fromJson(json, TextureFormat[].class);

        for (TextureFormat textureFormat : tfs) {
            Block block = getBlock(textureFormat.getName());
            if (Block.isAir(block)) {
                continue;
            }
            textureFormat.setTexture(texture);
            blockItemTextureFormats.put(textureFormat.getName(), textureFormat);
            Item item = new Item.Builder()
                    .setId(block.getId())
                    .setName(block.getName())
                    .setTextureFormat(textureFormat)
                    .setMaxStackCount((short)64)
                    .setIsBlock(true)
                    .build();
            items.put((short)block.getId(), item);
        }
    }

    public static void bufferTexCoords() {
        int size = textureFormats.size();
        float[] data = new float[8 * size];

        for (TextureFormat textureFormat : textureFormats.values()) {
            Vector2f[] uvs = textureFormat.getUvs();
            int index = textureFormat.getId() * 8;
            assert ((index + 7) < (8 * size)) : "Invalid texture location";
            data[index + 0] = uvs[0].x;
            data[index + 1] = uvs[0].y;
            data[index + 2] = uvs[1].x;
            data[index + 3] = uvs[1].y;
            data[index + 4] = uvs[2].x;
            data[index + 5] = uvs[2].y;
            data[index + 6] = uvs[3].x;
            data[index + 7] = uvs[3].y;
        }

        texCoordsBufferId = glGenBuffers();
        glBindBuffer(GL_TEXTURE_BUFFER, texCoordsBufferId);
        glBufferData(GL_TEXTURE_BUFFER, data, GL_STATIC_DRAW);

        texCoordsTextureId = glGenTextures();
        glBindTexture(GL_TEXTURE_BUFFER, texCoordsTextureId);
        glTexBuffer(GL_TEXTURE_BUFFER, GL_R32F, texCoordsBufferId);

        glBindBuffer(GL_TEXTURE_BUFFER, 0);
        glBindTexture(GL_TEXTURE_BUFFER, 0);
    }

    public static void generateBlockItemImages(String blockConfigPath, String outputPath) {
        File outputFile = new File(outputPath);
        File blockConfigFile = new File(blockConfigPath);

        if (outputFile.isDirectory()) {
            if (outputFile.lastModified() > blockConfigFile.lastModified()) {
                return;
            }
        }

        Files.mkdir(outputFile);
        float yaw = -63.70f;
        float pitch = -44.34f;
        float fov = 41.0f;
        Vector3f position = new Vector3f(-2.25f, 2.36f, 3.52f);
        float scale = 2.75f;
        Vector4f[] rotations = {
                new Vector4f(7.0f,  1.0f, 0.0f, 0.0f),
                new Vector4f(19.5f, 0.0f, 1.0f, 0.0f),
                new Vector4f(0.5f,  0.0f, 0.0f, 1.0f)
        };
        Camera camera = new Camera(
                new Vector3f(position),
                new Vector3f(0.0f, 0.0f, -1.0f),
                new Vector3f(0.0f, 1.0f, 0.0f),
                yaw,
                pitch,
                10f,
                Camera.DEFAULT_MOUSE_SENSITIVITY,
                fov + 2,
                Camera.DEFAULT_Z_NEAR,
                10_000f);
        int width = 32;
        int height = 32;
        Framebuffer framebuffer = new Framebuffer.Builder()
                .setSize(width, height)
                .addColorAttachment(new Texture(new Texture.Configuration.Builder()
                        .setTarget(GL_TEXTURE_2D)
                        .setSize(width, height)
                        .setInternalFormat(GL_RGBA8)
                        .setFormat(GL_RGBA)
                        .setType(GL_UNSIGNED_INT_8_8_8_8)
                        .addParameter(GL_TEXTURE_MIN_FILTER, GL_NEAREST)
                        .addParameter(GL_TEXTURE_MAG_FILTER, GL_NEAREST)
                        .build()))
                .setDepthAttachment(new Texture(new Texture.Configuration.Builder()
                        .setTarget(GL_TEXTURE_2D)
                        .setSize(width, height)
                        .setInternalFormat(GL_DEPTH_COMPONENT)
                        .setFormat(GL_DEPTH_COMPONENT)
                        .setType(GL_FLOAT)
                        .build()))
                .build();
        framebuffer.bind();
        glViewport(0, 0, framebuffer.getWidth(), framebuffer.getHeight());
        Renderer renderer = Renderer.getInstance();

        for (Block block : blocks.values()) {
            if (Block.isAir(block)) continue;
            String path = outputPath + "/" + block.getName() + ".png";
            framebuffer.bind();
            Sprite sideSprite = block.getSideTextureFormat().getAsSprite();
            Sprite topSprite = block.getTopTextureFormat().getAsSprite();
            Sprite bottomSprite = block.getBottomTextureFormat().getAsSprite();
            glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            renderer.drawCube3D(new Cube3D(new Transform(
                    new Vector3f(0f, 0f, -1),
                    new Vector3f(scale, scale, scale),
                    rotations),
                    new Vector3f(1f, 1f, 1f),
                    sideSprite,
                    topSprite,
                    bottomSprite));
            renderer.flushCube3DBatches(camera.getViewMatrix(), camera.getProjectionMatrix(), true);
            ByteBuffer pixels = framebuffer.readAllPixels(0);
            ImageIO.write(path, width, height, 4, pixels);
        }

        framebuffer.unbind();
        Application.getWindow().viewport();
    }

    public static int getTexCoordsTextureId() {
        return texCoordsTextureId;
    }

    public static int getTexCoordsBufferId() {
        return texCoordsBufferId;
    }

    public static Collection<Block> getAllBlocks() {
        return Collections.unmodifiableCollection(blocks.values());
    }

    public static Collection<Item> getAllItems() {
        return Collections.unmodifiableCollection(items.values());
    }

    public static Collection<TextureFormat> getAllTextureFormats() {
        return Collections.unmodifiableCollection(textureFormats.values());
    }

    public static Collection<TextureFormat> getAllItemTextureFormats() {
        return Collections.unmodifiableCollection(itemTextureFormats.values());
    }

    public static Collection<TextureFormat> getAllBlockItemTextureFormats() {
        return Collections.unmodifiableCollection(blockItemTextureFormats.values());
    }
}
