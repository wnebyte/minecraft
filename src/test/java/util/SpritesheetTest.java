package util;

import org.junit.Test;
import org.junit.Assert;
import com.github.wnebyte.minecraft.renderer.Sprite;
import com.github.wnebyte.minecraft.renderer.Spritesheet;
import com.github.wnebyte.minecraft.util.Settings;
import com.github.wnebyte.minecraft.util.Assets;
import com.github.wnebyte.minecraft.util.Files;

public class SpritesheetTest {

    @Test
    public void test00() {
        String json = Files.read(Assets.DIR + "/config/hudSprites.json");
        Spritesheet.Configuration conf = Settings.GSON.fromJson(json, Spritesheet.Configuration.class);
        Assert.assertNotNull(conf);
        Spritesheet spritesheet = new Spritesheet(conf);

        for (Sprite sprite : spritesheet) {
            System.out.println(sprite);
        }
    }
}
