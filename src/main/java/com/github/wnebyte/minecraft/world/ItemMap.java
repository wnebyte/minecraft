package com.github.wnebyte.minecraft.world;

import java.util.Map;
import java.util.HashMap;
import com.github.wnebyte.minecraft.util.TextureFormat;

public class ItemMap {

    private static final Map<Integer, Item> items = new HashMap<>();

    private static final Map<String, Integer> dict = new HashMap<>();

    private static final Map<String, TextureFormat> textureFormats = new HashMap<>();

    public static TextureFormat getTextureFormat(String name) {
        return null;
    }

    public static Item newItem(int id) {
        return null;
    }

    public static Item newItem(String name) {
        if (dict.containsKey(name)) {
            int id = dict.get(name);
            return newItem(id);
        } else {
            return null;
        }
    }

    public static void loadItems() {}

    public static void loadBlockItems() {}
}
