package util;

import org.junit.Test;
import org.junit.Assert;
import com.github.wnebyte.minecraft.renderer.TextureFormat;
import com.github.wnebyte.minecraft.util.Files;
import com.github.wnebyte.minecraft.util.Assets;
import com.github.wnebyte.minecraft.util.Settings;

public class TextureFormatTest {

    @Test
    public void test00() {
        String path = Assets.DIR + "/config/textureFormat.json";
        String json = Files.read(path);
        TextureFormat[] array = Settings.GSON.fromJson(json, TextureFormat[].class);
        int count = 0;
        for (TextureFormat tf : array) {
            if (tf == null) {
                count++;
            }
        }
        System.out.printf("null: %d/%d%n", count, array.length);
        Assert.assertEquals(0, count);
    }
}
