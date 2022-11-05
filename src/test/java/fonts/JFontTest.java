package fonts;

import java.util.Map;
import org.junit.Test;
import com.github.wnebyte.minecraft.renderer.fonts.JFont;
import com.github.wnebyte.minecraft.renderer.fonts.CharInfo;
import com.github.wnebyte.minecraft.util.Assets;

public class JFontTest {

    @Test
    public void test00() {
        JFont font = new JFont(Assets.DIR + "/fonts/Minecraft.ttf", 16);
        font.generateBitmap();
        Map<Integer, CharInfo> characters = font.getCharacters();
        for (Map.Entry<Integer, CharInfo> entry : characters.entrySet()) {
            char c = (char)(int)(entry.getKey());
            System.out.println(c);
        }
    }
}
