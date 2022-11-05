package com.github.wnebyte.minecraft.world;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.Collections;
import java.util.Map;
import com.google.gson.*;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import com.github.wnebyte.minecraft.core.Application;
import com.github.wnebyte.minecraft.core.Transform;
import com.github.wnebyte.minecraft.renderer.*;
import com.github.wnebyte.minecraft.util.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.GL_R8;
import static org.lwjgl.opengl.GL30C.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL30C.GL_R32F;
import static org.lwjgl.opengl.GL31C.GL_TEXTURE_BUFFER;
import static org.lwjgl.opengl.GL31C.glTexBuffer;
import static org.lwjgl.stb.STBImageWrite.stbi_flip_vertically_on_write;
import static org.lwjgl.stb.STBImageWrite.stbi_write_png;

public class BlockMap {

    private static final Map<Byte, Block> blocks = new HashMap<>();

    private static final Map<Integer, Item> items = new HashMap<>();

    private static final Map<String, TextureFormat> textureFormats = new HashMap<>();

    private static final Map<String, TextureFormat> itemTextureFormats = new HashMap<>();

    private static final Map<String, TextureFormat> blockItemTextureFormats = new HashMap<>();

    private static final Map<String, Integer> nameToId = new HashMap<>();

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
        byte id = (byte)(int)nameToId.get(name.toLowerCase(Locale.ROOT));
        return getBlock(id);
    }

    public static Item getItem(int id) {
        if (items.containsKey(id)) {
            return items.get(id);
        } else {
            assert false : String.format("Error: (BlockMap) Item with id: '%s' does not exist", id);
            return null;
        }
    }

    public static void loadBlocks(String blockConfigPath, String textureConfigPath, String packedTexturePath) {
        String json = Files.read(textureConfigPath);
        TextureFormat[] tfs = Settings.GSON.fromJson(json, TextureFormat[].class);
        Texture texture = Assets.getTexture(packedTexturePath);

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
            nameToId.put(name.toLowerCase(Locale.ROOT), (int)id);
        }
    }

    public static void loadItems(String itemConfigPath, String textureConfigPath, String packedTexturePath) {
        String json = Files.read(textureConfigPath);
        TextureFormat[] tfs = Settings.GSON.fromJson(json, TextureFormat[].class);
        Texture texture = Assets.getTexture(packedTexturePath);

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
            int id = jsonObject.get("id").getAsInt();
            String name = jsonObject.get("name").getAsString();
            int maxStackCount = jsonObject.get("maxStackCount").getAsInt();
            TextureFormat textureFormat = itemTextureFormats.get(name);
            Item item = new Item(id, name, maxStackCount);
            item.setTextureFormat(textureFormat);
            item.setIsBlock(false);
            items.put(id, item);
            nameToId.put(name.toLowerCase(Locale.ROOT), id);
        }
    }

    public static void loadBlockItems(String textureConfigPath, String packedTexturePath) {
        String json = Files.read(textureConfigPath);
        TextureFormat[] tfs = Settings.GSON.fromJson(json, TextureFormat[].class);
        Texture texture = Assets.getTexture(packedTexturePath);

        for (TextureFormat textureFormat : tfs) {
            textureFormat.setTexture(texture);
            blockItemTextureFormats.put(textureFormat.getName(), textureFormat);
            Block block = getBlock(textureFormat.getName());
            Item item = new Item(block.getId(), block.getName(), 64);
            item.setTextureFormat(textureFormat);
            item.setIsBlock(true);
            items.put((int)block.getId(), item);
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
        Vector3f cameraPos = new Vector3f(-1.0f, 1.0f, 1.0f);
        Vector3f cameraOrientation = new Vector3f(-35.0f, -45.0f, 0.0f);
        Vector3f direction = new Vector3f();
        direction.x = (float)Math.cos(Math.toRadians(cameraOrientation.y)) * (float)Math.cos(Math.toRadians(cameraOrientation.x));
        direction.y = (float)Math.sin(Math.toRadians(cameraOrientation.x));
        direction.z = (float)Math.sin(Math.toRadians(cameraOrientation.y)) * (float)Math.cos(Math.toRadians(cameraOrientation.x));
        Vector3f cameraForward = direction.normalize();
        Vector3f cameraRight = JMath.cross(cameraForward, new Vector3f(0.0f, 1.0f, 0.0f));
        Vector3f cameraUp = JMath.cross(cameraRight, cameraForward);
        Matrix4f viewMatrix = new Matrix4f().identity();
        viewMatrix.lookAt(
                cameraPos,
                JMath.add(cameraPos, cameraForward),
                cameraUp
        );
        Matrix4f projectionMatrix = new Matrix4f().identity();
        projectionMatrix.ortho(-1.0f, 1.0f, -1.0f, 1.0f, 0.1f, 2000.0f);

        int width = 32;
        int height = 32;
        Texture colorAttachment = new Texture(new Texture.Configuration.Builder()
                .setTarget(GL_TEXTURE_2D)
                .setSize(width, height)
                .setInternalFormat(GL_RGBA8)
                .setFormat(GL_RGBA)
                .setType(GL_UNSIGNED_INT_8_8_8_8)
                .addParameter(GL_TEXTURE_MIN_FILTER, GL_NEAREST)
                .addParameter(GL_TEXTURE_MAG_FILTER, GL_NEAREST)
                .build());
        Texture depthAttachment = new Texture(new Texture.Configuration.Builder()
                .setTarget(GL_TEXTURE_2D)
                .setSize(width, height)
                .setInternalFormat(GL_DEPTH_COMPONENT)
                .setFormat(GL_DEPTH_COMPONENT)
                .setType(GL_FLOAT)
                .build());
        Framebuffer framebuffer = new Framebuffer(new Framebuffer.Configuration.Builder()
                .setSize(width, height)
                .addColorAttachment(colorAttachment)
                .setDepthAttachment(depthAttachment)
                .build());
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
                    new Vector3f(0f, 0f, 0),
                    new Vector3f(1.5f, 1.5f, 1.5f),
                    new Vector4f(0f, 0f, 0f, 0f)), // 25.f, 1f, 1f, 0f
                    new Vector3f(1f, 1f, 1f),
                    sideSprite,
                    topSprite,
                    bottomSprite));
            renderer.flushCube3DBatches(viewMatrix, projectionMatrix);
            ByteBuffer pixels = framebuffer.readAllPixels(0);
            stbi_flip_vertically_on_write(false);
            stbi_write_png(path, framebuffer.getWidth(), framebuffer.getHeight(), 4, pixels, 4 * framebuffer.getWidth());
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
}
