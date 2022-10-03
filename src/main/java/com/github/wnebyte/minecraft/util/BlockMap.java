package com.github.wnebyte.minecraft.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import com.google.gson.*;
import org.joml.Vector2f;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30C.GL_R32F;
import static org.lwjgl.opengl.GL31C.GL_TEXTURE_BUFFER;
import static org.lwjgl.opengl.GL31C.glTexBuffer;

public class BlockMap {

    private static final Map<Integer, BlockFormat> blockFormats = new HashMap<>();

    private static final Map<String, TextureFormat> textureFormats = new HashMap<>();

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

    public static BlockFormat getBlockFormat(int id) {
        if (blockFormats.containsKey(id)) {
            return blockFormats.get(id);
        } else {
            assert false : String.format("Error: (BlockMap) BlockFormat with id: '%s' does not exist", id);
            return null;
        }
    }

    public static BlockFormat getBlockFormat(String name) {
        int id = nameToId.get(name);
        return getBlockFormat(id);
    }

    public static void load(String textureConfigPath, String blockConfigPath) {
        String json = String.join(System.lineSeparator(), Files.readAllLines(textureConfigPath));
        TextureFormat[] tfs = Settings.GSON.fromJson(json, TextureFormat[].class);
        for (TextureFormat textureFormat : tfs) {
            textureFormats.put(textureFormat.getName(), textureFormat);
        }

        json = String.join(System.lineSeparator(), Files.readAllLines(blockConfigPath));
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
            boolean isSolid = jsonObject.get("isSolid").getAsBoolean();
            boolean isTransparent = jsonObject.get("isTransparent").getAsBoolean();
            boolean isBlendable = jsonObject.get("isBlendable").getAsBoolean();
            String side = jsonObject.get("side").getAsString();
            String top = jsonObject.get("top").getAsString();
            String bottom = jsonObject.get("bottom").getAsString();
            TextureFormat sideTextureFormat = textureFormats.get(side);
            TextureFormat topTextureFormat = textureFormats.get(top);
            TextureFormat bottomTextureForamt = textureFormats.get(bottom);
            BlockFormat blockFormat = new BlockFormat.Builder()
                    .setId(id)
                    .setName(name)
                    .setSideTextureFormat(sideTextureFormat)
                    .setTopTextureFormat(topTextureFormat)
                    .setBottomTextureFormat(bottomTextureForamt)
                    .setIsSolid(isSolid)
                    .setIsTransparent(isTransparent)
                    .setIsBlendable(isBlendable)
                    .build();
            blockFormats.put(id, blockFormat);
            nameToId.put(name, id);
        }
    }

    public static void bufferTexCoords() {
        int numTextures = textureFormats.size();
        float[] data = new float[8 * numTextures];

        for (Map.Entry<String, TextureFormat> entry : textureFormats.entrySet()) {
            TextureFormat textureFormat = entry.getValue();
            Vector2f[] uvs = textureFormat.getUvs();
            int index = textureFormat.getId() * 8;
            assert ((index + 7) < (8 * numTextures)) : "Invalid texture location";
            // TR
            data[index + 0] = uvs[0].x;
            data[index + 1] = uvs[0].y;
            // BR
            data[index + 2] = uvs[1].x;
            data[index + 3] = uvs[1].y;
            // BL
            data[index + 4] = uvs[2].x;
            data[index + 5] = uvs[2].y;
            // TL
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

    public static int getTexCoordsTextureId() {
        return texCoordsTextureId;
    }

    public static int getTexCoordsBufferId() {
        return texCoordsBufferId;
    }

    public static Collection<BlockFormat> getAllBlockFormats() {
        return Collections.unmodifiableCollection(blockFormats.values());
    }

    public static Collection<TextureFormat> getAllTextureFormats() {
        return Collections.unmodifiableCollection(textureFormats.values());
    }
}
