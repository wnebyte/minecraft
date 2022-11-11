package com.github.wnebyte.minecraft.util;

public class AppData {

    public static final String DIR = "C:/users/ralle/dev/java/minecraft/appdata";

    public static final String SCREENSHOTS_DIR = DIR + "/screenshots";

    static {
        if (!Files.exists(SCREENSHOTS_DIR)) {
            Files.mkdir(SCREENSHOTS_DIR);
        }
    }
}
