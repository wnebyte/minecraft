package util;

import org.junit.Test;
import com.github.wnebyte.minecraft.util.Assets;
import com.github.wnebyte.minecraft.util.Files;
import com.github.wnebyte.minecraft.util.FnlState;
import com.github.wnebyte.minecraft.util.Settings;

public class FnlStateTest {

    @Test
    public void test00() {
        String json = Files.read(Assets.DIR + "/config/terrainNoise.json");
        FnlState[] fnlStates = Settings.GSON.fromJson(json, FnlState[].class);

        for (FnlState fnlState : fnlStates) {
            System.out.println(fnlState.getNoiseType());
        }
    }
}
