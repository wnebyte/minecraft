package com.github.wnebyte.minecraft.ui;

import java.util.List;
import java.util.ArrayList;
import org.joml.Vector2f;
import org.joml.Vector3f;
import com.github.wnebyte.minecraft.world.BlockMap;
import com.github.wnebyte.minecraft.world.Item;
import com.github.wnebyte.minecraft.core.Scene;
import com.github.wnebyte.minecraft.core.Camera;
import com.github.wnebyte.minecraft.core.MouseListener;
import com.github.wnebyte.minecraft.components.Inventory;
import com.github.wnebyte.minecraft.renderer.Spritesheet;
import com.github.wnebyte.minecraft.renderer.Sprite;
import com.github.wnebyte.minecraft.renderer.Renderer;
import com.github.wnebyte.minecraft.util.Assets;
import static org.lwjgl.glfw.GLFW.*;

public class Hud {

    /*
    ###########################
    #      STATIC FIELDS      #
    ###########################
    */

    private static final float CROSSHAIR_SIZE = 0.08f;

    private static final float CROSSHAIR_HALF_SIZE = CROSSHAIR_SIZE / 2.0f;

    private static final Vector3f CROSSHAIR_COLOR = new Vector3f(177.0f / 255.0f, 199.0f / 255.0f, 179.0f / 255.0f);

    private static final Vector2f INVENTORY_SIZE_PX = new Vector2f(216.0f, 194.0f);

    private static final float INVENTORY_RATIO = (INVENTORY_SIZE_PX.y / INVENTORY_SIZE_PX.x);

    private static final Vector2f INVENTORY_SIZE = new Vector2f(2.4f, 2.4f).mul(INVENTORY_RATIO);

    private static final Vector2f INVENTORY_POSITION = new Vector2f(-(INVENTORY_SIZE.x / 2.0f), -(INVENTORY_SIZE.y / 2.0f));

    private static final Vector2f SLOT_SIZE = new Vector2f(21.0f, 21.0f)
            .mul(new Vector2f(1.0f / INVENTORY_SIZE_PX.x, 1.0f / INVENTORY_SIZE_PX.y)).mul(INVENTORY_SIZE);

    private static final float HOTBAR_POSITION_Y = -1.48f;

    private final Vector2f[] SLOT_OFFSETS = new Vector2f[Inventory.SIZE];

    /*
    ###########################
    #          FIELDS         #
    ###########################
    */

    private Inventory inventory;

    private Inventory.Hotbar hotbar;

    private Inventory.Item draggable;

    private Spritesheet spritesheet;

    private Sprite regSprite;

    private Sprite selSprite;

    private Sprite inventorySprite;

    private Renderer renderer;

    private Camera camera;

    private boolean showInventory;

    private boolean showHotbar;

    /*
    ###########################
    #       CONSTRUCTORS      #
    ###########################
    */

    public Hud(Camera camera) {
        this.camera = camera;
    }

    /*
    ###########################
    #          METHODS        #
    ###########################
    */

    public void start(Scene scene) {
        this.inventory = scene.getComponent(Inventory.class);
        assert (inventory != null) : "Error: (Hud) Inventory is null";
        this.hotbar = inventory.getHotbar();
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
            Item it = items.get(i);
            if (it.isBlock()) {
                continue;
            }
            Inventory.Item item = new Inventory.Item(it.getId(), it.getMaxStackCount());
            if (item.getMaxStackCount() > 1) {
                item.setStackCount(item.getMaxStackCount() / 2);
            } else {
                item.setStackCount(1);
            }
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
            drawCursor(x, y, CROSSHAIR_HALF_SIZE, CROSSHAIR_COLOR);
        } else {
            camera.unlock();
            drawCursor(0.0f, 0.0f, CROSSHAIR_HALF_SIZE, CROSSHAIR_COLOR);
        }
    }

    public void processInput(float dt) {

    }

    private void drawHotbar() {
        int size = hotbar.size();
        float x = -((SLOT_SIZE.x * size) / 2);
        float y = HOTBAR_POSITION_Y;
        for (int i = 0; i < size; i++) {
            Sprite sprite = hotbar.isSelected(i) ? selSprite : regSprite;
            renderer.drawTexture2D(x, y, -5, SLOT_SIZE.x, SLOT_SIZE.y, sprite, 0xFFFF, true);
            Inventory.Item item = hotbar.get(i);
            if (item != null) {
                drawItem(item, x, y, -4, SLOT_SIZE.x, SLOT_SIZE.y, 0.8f);
            }
            x += SLOT_SIZE.x;
        }
    }

    private void drawInventory() {
        renderer.drawTexture2D(INVENTORY_POSITION.x, INVENTORY_POSITION.y, -5,
                INVENTORY_SIZE.x, INVENTORY_SIZE.y, inventorySprite, 0xFFFF, false);
        for (int i = 0; i < SLOT_OFFSETS.length; i++) {
            Inventory.Item item = inventory.get(i);
            Vector2f pos = new Vector2f(INVENTORY_POSITION).add(SLOT_OFFSETS[i]);
            updateSlot(i, item, pos);
        }
        if (!MouseListener.isMouseButtonDown(GLFW_MOUSE_BUTTON_LEFT)) {
            draggable = null;
        }
    }

    private void updateSlot(int index, Inventory.Item item, Vector2f pos) {
        float x = MouseListener.getScreenX();
        float y = MouseListener.getScreenY();
        boolean click = MouseListener.isMouseButtonDown(GLFW_MOUSE_BUTTON_LEFT);

        boolean hover = ((x >= pos.x && x <= pos.x + SLOT_SIZE.x) && (y >= pos.y && y <= pos.y + SLOT_SIZE.y));
        boolean dragging = (draggable != null && draggable.equals(item));
        if (hover) {
            renderer.drawTexture2D(pos.x, pos.y, -3, SLOT_SIZE.x, SLOT_SIZE.y, regSprite, 0xFFFF, true);
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
                drawItem(item, x - CROSSHAIR_SIZE, y - CROSSHAIR_SIZE, -1, SLOT_SIZE.x, SLOT_SIZE.y, 0.90f);
            } else {
                drawItem(item, pos.x, pos.y, -2, SLOT_SIZE.x, SLOT_SIZE.y, 0.90f);
            }
        }
    }

    private void drawItem(Inventory.Item item, float x, float y, int z, float width, float height, float scale) {
        Item it = BlockMap.getItem(item.getId());
        Sprite sprite;
        if (it.isBlock()) {
            sprite = BlockMap.getBlockItemTextureFormat(it.getName()).getAsSprite();
        } else {
            sprite = BlockMap.getItemTextureFormat(it.getName()).getAsSprite();
        }
        Vector2f size = new Vector2f(scale * width, scale * height);
        float xPos = x + ((scale - size.x) * 0.020f);
        float yPos = y + ((scale - size.y) * 0.020f);
        renderer.drawTexture2D(xPos, yPos, z, size.x, size.y, sprite, 0xFFFF, true);

        if (item.getStackCount() > 1) {
            String text = String.valueOf(item.getStackCount());
        }
    }

    private void determineSlotOffsets() {
        Vector2f start = new Vector2f(10.0f, 10.0f)
                .mul(new Vector2f(1.0f / INVENTORY_SIZE_PX.x, 1.0f / INVENTORY_SIZE_PX.y)).mul(INVENTORY_SIZE);
        Vector2f pos = new Vector2f(start);

        for (int row = 0; row < inventory.getRows() + 1; row++) {
            for (int col = 0; col < inventory.getCols(); col++) {
                SLOT_OFFSETS[col + (row * inventory.getCols())] = new Vector2f(pos);
                pos.x += SLOT_SIZE.x + ((1.0f / INVENTORY_SIZE_PX.x) * INVENTORY_SIZE.x);
            }
            pos.x = start.x;
            pos.y += SLOT_SIZE.y + ((1.0f / INVENTORY_SIZE_PX.y) * INVENTORY_SIZE.y);

            if (row == 0) {
                pos.y += 9.0f * ((1.0f / INVENTORY_SIZE_PX.y) * INVENTORY_SIZE.y);
            }
        }
    }

    private void drawCursor(float x, float y, float halfSize, Vector3f color) {
        renderer.drawLine2D(
                new Vector2f(x, y - halfSize),
                new Vector2f(x, y + halfSize),
                0,
                color);
        renderer.drawLine2D(
                new Vector2f(x - halfSize, y),
                new Vector2f(x + halfSize, y),
                0,
                color);
    }

    public void setShowHotbar(boolean value) {
        this.showHotbar = value;
    }

    public boolean isShowHotbar() {
        return showHotbar;
    }

    public void setShowInventory(boolean value) {
        this.showInventory = value;
    }

    public boolean isShowInventory() {
        return showInventory;
    }

    public Inventory getInventory() {
        return inventory;
    }
}
