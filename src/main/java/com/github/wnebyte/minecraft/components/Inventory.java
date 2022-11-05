package com.github.wnebyte.minecraft.components;

import com.github.wnebyte.minecraft.world.Item;
import com.github.wnebyte.minecraft.core.Component;

public class Inventory extends Component {

    /**
     * Inner class can access the first n elements of the enclosing class's array of items.
     */
    public class Hotbar {

        private int size;

        private int selected;

        private Hotbar(int size) {
            this.size = size;
            this.selected = 0;
        }

        public int size() {
            return size;
        }

        public boolean isSelected(int index) {
            return (index == selected);
        }

        public Item getSelected() {
            if (selected < size) {
                return items[selected];
            } else {
                throw new ArrayIndexOutOfBoundsException(
                        ""
                );
            }
        }

        public Item get(int index) {
            if (index < size) {
                return items[index];
            } else {
                throw new ArrayIndexOutOfBoundsException(
                        "Index is out of bounds."
                );
            }
        }

        public void set(int index, Item item) {
            if (index < size) {
                items[index] = item;
            } else {
                throw new ArrayIndexOutOfBoundsException(
                        "Index is out of bounds."
                );
            }
        }

        public void swap(int i, int j) {
            if (i < size && j < size) {
                Item tmp = items[i];
                items[i] = items[j];
                items[j] = tmp;
            } else {
                throw new ArrayIndexOutOfBoundsException(
                        "Index is out of bounds."
                );
            }
        }

        public void next() {
            selected = (selected + 1) % size;
        }

        public void previous() {
            selected = (selected + (size - 1)) % size;
        }
    }

    private final Item[] items;

    private final int rows, cols;

    private final int size;

    private final Hotbar hotbar;

    public Inventory(int rows, int cols, int hotbarSize) {
        this.rows = rows;
        this.cols = cols;
        this.size = (rows * cols) + hotbarSize;
        this.items = new Item[size];
        this.hotbar = new Hotbar(hotbarSize);
    }

    public Item get(int index) {
        if (rangeCheck(index)) {
            return items[index];
        } else {
            throw new ArrayIndexOutOfBoundsException(
                    "Index is out of bounds."
            );
        }
    }

    public void set(int index, Item item) {
        if (rangeCheck(index)) {
            items[index] = item;
        } else {
            throw new ArrayIndexOutOfBoundsException(
                    "Index is out of bounds."
            );
        }
    }

    public int indexOf(Item item) {
        for (int i = 0; i < size; i++) {
            Item obj = items[i];
            if (item.equals(obj)) {
                return i;
            }
        }
        return -1;
    }

    public void swap(int i, int j) {
        if (rangeCheck(i, j)) {
            Item tmp = items[i];
            items[i] = items[j];
            items[j] = tmp;
        } else {
            throw new ArrayIndexOutOfBoundsException(
                    "Index is out of bounds."
            );
        }
    }

    public void swap(Item a, Item b) {
        int i = indexOf(a), j = indexOf(b);
        swap(i, j);
    }

    public void swap(int index, Item item) {
        int j = indexOf(item);
        if (rangeCheck(index, j)) {
            swap(index, j);
        }
    }

    private boolean rangeCheck(int... indices) {
        if (indices == null) return false;
        for (int i : indices) {
            if (i >= 0 && i < size) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }

    public int size() {
        return size;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public Hotbar getHotbar() {
        return hotbar;
    }
}
