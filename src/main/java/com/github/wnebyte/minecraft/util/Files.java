package com.github.wnebyte.minecraft.util;

import java.io.*;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import com.github.wnebyte.minecraft.world.Chunk;

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

    public static boolean write(String path, byte[] bytes) {
        try {
            java.nio.file.Files.write(Paths.get(path), bytes,
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

    public static String read(String path) {
        return String.join(System.lineSeparator(), readAllLines(path));
    }

    public static boolean exists(String path) {
        return new File(path).exists();
    }

    public static boolean isDirectory(String path) {
        return new File(path).isDirectory();
    }

    public static void mkdir(String path) {
        mkdir(new File(path));
    }

    public static void mkdir(File file) {
        if (!file.isDirectory()) {
            file.mkdir();
        }
    }

    public static boolean compress(String path, byte[] bytes) {
        try {
            FileOutputStream fos = new FileOutputStream(path);
            DeflaterOutputStream dos = new DeflaterOutputStream(fos);
            dos.write(bytes, 0, bytes.length);
            dos.close();
            fos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static byte[] decompress(String path) {
        try {
            FileInputStream fis = new FileInputStream(path);
            InflaterInputStream iis = new InflaterInputStream(fis);
            byte[] bytes = new byte[Chunk.WIDTH * Chunk.HEIGHT * Chunk.DEPTH];
            byte data;
            int index = 0;
            while ((data = (byte)iis.read()) != -1) {
                bytes[index++] = data;
            }
            iis.close();
            fis.close();
            return bytes;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
