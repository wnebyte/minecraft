package com.github.wnebyte.minecraft.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

public class Files {

    public static boolean write(String path, CharSequence line) {
        return write(path, Collections.singletonList(line));
    }

    public static boolean write(String path, Iterable<? extends CharSequence> lines) {
        try {
            java.nio.file.Files.write(Paths.get(path), lines, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<String> readAllLines(String path) {
        try {
            List<String> lines = java.nio.file.Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
            return lines;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean exists(String path) {
        return new File(path).exists();
    }
}
