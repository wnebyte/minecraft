package com.github.wnebyte.minecraft.util;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import com.google.gson.*;

public class BlockMap {

    private static final Map<Integer, BlockFormat> blockFormats = new HashMap<>();

    private static final Map<String, TextureFormat> textureFormats = new HashMap<>();

    private static final Map<String, Integer> nameToId = new HashMap<>();

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
            String side = jsonObject.get("side").getAsString();
            String top = jsonObject.get("top").getAsString();
            String bottom = jsonObject.get("bottom").getAsString();
            TextureFormat sideTextureFormat = textureFormats.get(side);
            TextureFormat topTextureFormat = textureFormats.get(top);
            TextureFormat bottomTextureForamt = textureFormats.get(bottom);
            BlockFormat blockFormat = new BlockFormat(id, name,
                    sideTextureFormat, topTextureFormat, bottomTextureForamt);
            blockFormats.put(id, blockFormat);
            nameToId.put(name, id);
        }
    }

    public static Collection<BlockFormat> getAllBlockFormats() {
        return Collections.unmodifiableCollection(blockFormats.values());
    }

    public static Collection<TextureFormat> getAllTextureFormats() {
        return Collections.unmodifiableCollection(textureFormats.values());
    }
}
