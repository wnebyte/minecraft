package components;

import java.util.Random;
import org.junit.Test;
import com.github.wnebyte.minecraft.util.Settings;
import com.github.wnebyte.minecraft.components.Inventory;

public class InventoryTest {

    @Test
    public void test00() {
        Inventory inventory = new Inventory();
        populateInventory(inventory, 3);
        String json = Settings.GSON.toJson(inventory);
        inventory = Settings.GSON.fromJson(json, Inventory.class);
        System.out.println(Settings.GSON.toJson(inventory));
    }

    private void populateInventory(Inventory inventory, int numItems) {
        Random rand = new Random();
        for (int i = 0; i < Math.min(inventory.size(), numItems); i++) {
            int maxStackCount = (i % 2 == 0) ? 64 : 32;
            Inventory.Item item = new Inventory.Item((short)i, maxStackCount);
            item.addStackCount(rand.nextInt(maxStackCount));
            inventory.set(i, item);
        }
    }
}
