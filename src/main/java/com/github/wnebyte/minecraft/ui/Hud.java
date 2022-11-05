package com.github.wnebyte.minecraft.ui;

import java.util.List;
import java.util.ArrayList;
import org.joml.Vector2f;
import org.joml.Vector3f;
import com.github.wnebyte.minecraft.world.Item;
import com.github.wnebyte.minecraft.world.BlockMap;
import com.github.wnebyte.minecraft.core.Camera;
import com.github.wnebyte.minecraft.core.MouseListener;
import com.github.wnebyte.minecraft.components.Inventory;
import com.github.wnebyte.minecraft.renderer.Spritesheet;
import com.github.wnebyte.minecraft.renderer.Sprite;
import com.github.wnebyte.minecraft.renderer.Renderer;
import com.github.wnebyte.minecraft.util.Assets;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

public class Hud {

    private static final float CROSSHAIR_SIZE = 0.10f;

    private static final float CROSSHAIR_HALF_SIZE = CROSSHAIR_SIZE / 2.0f;

    private static final Vector2f INVENTORY_SIZE_PX = new Vector2f(216.0f, 194.0f);

    private static final float INVENTORY_RATIO = (INVENTORY_SIZE_PX.y / INVENTORY_SIZE_PX.x);

    private static final Vector2f INVENTORY_SIZE = new Vector2f(2.4f, 2.4f).mul(INVENTORY_RATIO);

    private static final Vector2f INVENTORY_POSITION = new Vector2f(-(INVENTORY_SIZE.x / 2.0f), -(INVENTORY_SIZE.y / 2.0f));

    private static final Vector2f SLOT_SIZE = new Vector2f(21.0f, 21.0f)
            .mul(new Vector2f(1.0f / INVENTORY_SIZE_PX.x, 1.0f / INVENTORY_SIZE_PX.y)).mul(INVENTORY_SIZE);

    private static final float HOTBAR_POSITION_Y = -1.48f;

    private final Inventory inventory = new Inventory(3, 9, 9);

    private final Inventory.Hotbar hotbar = inventory.getHotbar();

    private final Vector2f[] SLOT_OFFSETS = new Vector2f[inventory.size()];

    private Item draggable;

    private Spritesheet spritesheet;

    private Sprite regSprite;

    private Sprite selSprite;

    private Sprite inventorySprite;

    private Renderer renderer;

    private Camera camera;

    private boolean showInventory;

    private boolean showHotbar;

    public Hud(Camera camera) {
        this.camera = camera;
    }

    public void start() {
        this.spritesheet = Assets.getSpritesheet(Assets.DIR + "/images/spritesheets/hudSprites.png");
        this.selSprite = spritesheet.getSprite(5);
        this.regSprite = spritesheet.getSprite(6);
        this.inventorySprite = spritesheet.getSprite(7);
        this.renderer = Renderer.getInstance();
        this.showHotbar = true;
        this.showInventory = false;
        this.determineSlotOffsets();
        List<Item> items = new ArrayList<>(BlockMap.getAllItems());
        for (int i = 0; i < Math.min(items.size(), inventory.size()); i++) {
            Item item = items.get(i);
            inventory.set(i, item);
        }
    }

    public void update(float dt) {
        drawHud();
    }

    private void drawHud() {
        if (showHotbar) {
            drawHotbar();
        }
        if (showInventory) {
            drawInventory();
            camera.lock();
            float x = MouseListener.getScreenX();
            float y = MouseListener.getScreenY();
            drawCursor(x, y, CROSSHAIR_HALF_SIZE);
        } else {
            camera.unlock();
            drawCursor(0.0f, 0.0f, CROSSHAIR_HALF_SIZE);
        }
    }

    private void drawHotbar() {
        int size = hotbar.size();
        float x = ((SLOT_SIZE.x * size) / 2);
        float y = HOTBAR_POSITION_Y;
        for (int i = 0; i < size; i++) {
            Sprite sprite = hotbar.isSelected(i) ? selSprite : regSprite;
            renderer.drawTexture2D(-x, y, -5, SLOT_SIZE.x, SLOT_SIZE.y, sprite, 0xFFFF, true);
            Item item = hotbar.get(i);
            if (item != null) {
                drawItem(item, -x, y, -1, SLOT_SIZE.x, SLOT_SIZE.y, 0.8f);
            }
            x -= SLOT_SIZE.x;
        }
    }

    private void drawInventory() {
        renderer.drawTexture2D(INVENTORY_POSITION.x, INVENTORY_POSITION.y, -5,
                INVENTORY_SIZE.x, INVENTORY_SIZE.y, inventorySprite, 0xFFFF, false);
        for (int i = 0; i < SLOT_OFFSETS.length; i++) {
            Item item = inventory.get(i);
            Vector2f pos = new Vector2f(INVENTORY_POSITION).add(SLOT_OFFSETS[i]);
            updateSlot(i, item, pos);
        }
        if (!MouseListener.isMouseButtonDown(GLFW_MOUSE_BUTTON_LEFT)) {
            draggable = null;
        }
    }

    private void updateSlot(int index, Item item, Vector2f pos) {
        float x = MouseListener.getScreenX();
        float y = MouseListener.getScreenY();
        boolean click = MouseListener.isMouseButtonDown(GLFW_MOUSE_BUTTON_LEFT);

        boolean hover = ((x >= pos.x && x <= pos.x + SLOT_SIZE.x) && (y >= pos.y && y <= pos.y + SLOT_SIZE.y));
        boolean dragging = (draggable != null && draggable.equals(item));
        if (hover) {
            renderer.drawBox2D(new Vector2f(pos.x + (SLOT_SIZE.x / 2.0f), pos.y + (SLOT_SIZE.y / 2.0f)), SLOT_SIZE,
                    0, 0f, new Vector3f(0f, 0f, 0f));
            if (click && draggable == null) {
                draggable = item;
            } else if (!click && draggable != null) {
                inventory.swap(index, draggable);
                draggable = null;
            }
            dragging = (draggable != null && draggable.equals(item));
        }

        if (item != null) {
            if (dragging) {
                drawItem(item, x - CROSSHAIR_SIZE, y - CROSSHAIR_SIZE, 0, SLOT_SIZE.x, SLOT_SIZE.y, 0.90f);
            } else {
                drawItem(item, pos.x, pos.y, -1, SLOT_SIZE.x, SLOT_SIZE.y, 0.90f);
            }
        }
    }

    private void drawItem(Item item, float x, float y, int z, float width, float height, float scale) {
        Sprite sprite;
        if (item.isBlock()) {
            sprite = BlockMap.getBlockItemTextureFormat(item.getName()).getAsSprite();
        } else {
            sprite = BlockMap.getItemTextureFormat(item.getName()).getAsSprite();
        }
        Vector2f size = new Vector2f(scale * width, scale * height);
        float xPos = x + ((scale - size.x) * 0.025f);
        float yPos = y + ((scale - size.y) * 0.025f);
        renderer.drawTexture2D(xPos, yPos, z, size.x, size.y, sprite, 0xFFFF, true);

        if (item.getStackCount() > 1) {
            scale = 0.0012f;
            float padding = 0.001f;
            String text = String.valueOf(item.getStackCount());
        }
    }

    private void determineSlotOffsets() {
        Vector2f start = new Vector2f(10.0f, 10.0f)
                .mul(new Vector2f(1.0f / INVENTORY_SIZE_PX.x, 1.0f / INVENTORY_SIZE_PX.y)).mul(INVENTORY_SIZE);
        Vector2f pos = new Vector2f(start);
        float maxRowY = Float.MIN_VALUE;

        for (int row = 0; row < inventory.getRows() + 1; row++) {
            for (int col = 0; col < inventory.getCols(); col++) {
                SLOT_OFFSETS[col + (row * inventory.getCols())] = new Vector2f(pos);
                pos.x += SLOT_SIZE.x;
                pos.x += (1.0f / INVENTORY_SIZE_PX.x) * INVENTORY_SIZE.x;
            }
            pos.x = start.x;
            pos.y += SLOT_SIZE.y;
            pos.y += (1.0f / INVENTORY_SIZE_PX.y) * INVENTORY_SIZE.y;
            float maxY = pos.y + (1.0f / INVENTORY_SIZE_PX.y) * INVENTORY_SIZE.y;
            maxRowY = Math.max(maxRowY, maxY);

            if (row == 0) {
                pos.y += 9.0f * (1.0f / INVENTORY_SIZE_PX.y) * INVENTORY_SIZE.y;
            }
        }
    }

    private void drawCursor(float x, float y, float halfSize) {
        renderer.drawLine2D(
                new Vector2f(x, y - halfSize),
                new Vector2f(x, y + halfSize),
                0,
                new Vector3f(0f, 0f, 0f));
        renderer.drawLine2D(
                new Vector2f(x - halfSize, y),
                new Vector2f(x + halfSize, y),
                0,
                new Vector3f(0f, 0f, 0f));
    }

    public void showHotbar(boolean value) {
        this.showHotbar = value;
    }

    public boolean isHotbarShowing() {
        return showHotbar;
    }

    public void showInventory(boolean value) {
        this.showInventory = value;
    }

    public boolean isInventoryShowing() {
        return showInventory;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Inventory.Hotbar getHotbar() {
        return hotbar;
    }
}
