package com.github.wnebyte.minecraft.util;

public class JsonIO {

    public static boolean write(String path, Object src) {
        String json = Settings.GSON.toJson(src);
        return Files.write(path, json);
    }

    public static <T> T read(String path, Class<T> cls) {
        if (Files.exists(path)) {
            T obj = Settings.GSON.fromJson(path, cls);
            return obj;
        } else {
            return null;
        }
    }
}
