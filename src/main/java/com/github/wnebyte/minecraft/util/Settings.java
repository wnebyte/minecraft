package com.github.wnebyte.minecraft.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Settings {

    public static final float RENDER_DISTANCE = 5f;

    // (2r - 3)^2          =
    // (2r - 3) * (2r - 3) =
    // 2r * 2r - 2r * 3 - 3 * 2r + 3 * 3 =
    // (2r)^2 - 2(2r*3) + 3^2
    public static final float AOC = (float)Math.pow(2 * RENDER_DISTANCE, 2) - 2 * (2 * RENDER_DISTANCE * 3) + (3 * 3);

    public static final float MIN_AOC = 9f;

    public static final Gson GSON = new GsonBuilder()
           // .registerTypeAdapter(TextureFormat.class, new TextureFormatAdapter())
            .enableComplexMapKeySerialization()
            .setPrettyPrinting()
            .create();
}
