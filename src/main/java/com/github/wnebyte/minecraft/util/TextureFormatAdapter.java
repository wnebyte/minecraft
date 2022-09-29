package com.github.wnebyte.minecraft.util;

import java.lang.reflect.Type;
import com.google.gson.*;

public class TextureFormatAdapter implements JsonSerializer<TextureFormat>, JsonDeserializer<TextureFormat> {

    @Override
    public JsonElement serialize(TextureFormat src, Type typeOfSrc, JsonSerializationContext context) {
        return null;
    }

    @Override
    public TextureFormat deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        int id = jsonObject.get("id").getAsInt();
        String name = jsonObject.get("name").getAsString();
        return null;
    }
}
