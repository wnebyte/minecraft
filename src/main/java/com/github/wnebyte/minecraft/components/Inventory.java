package com.github.wnebyte.minecraft.components;

import com.github.wnebyte.minecraft.core.Component;

public class Inventory extends Component {

    public static class Item {

        public static final int DEFAULT_STACK_COUNT = 1;

        public static final int DEFAULT_MAX_STACK_COUNT = 1;

        private final short id;

        private int stackCount;

        private final int maxStackCount;

        public Item(short id) {
            this(id, DEFAULT_STACK_COUNT, DEFAULT_MAX_STACK_COUNT);
        }

        public Item(short id, int maxStackCount) {
            this(id, DEFAULT_STACK_COUNT, maxStackCount);
        }

        public Item(short id, int stackCount, int maxStackCount) {
            this.id = id;
            this.stackCount = stackCount;
            this.maxStackCount = maxStackCount;
        }

        public short getId() {
            return id;
        }

        public int getStackCount() {
            return stackCount;
        }

        public void setStackCount(int stackCount) {
            this.stackCount = stackCount;
        }

        public void addStackCount(int value) {
            this.stackCount += value;
        }

        public int getMaxStackCount() {
            return maxStackCount;
        }
    }

    /**
     * Inner class can access the first n elements of the enclosing class's array of items.
     */
    public class Hotbar {

        private transient int size;

        private int selected;

        private Hotbar() {
            this.selected = 0;
            this.size = COLS;
        }

        public int size() {
            return size;
        }

        public boolean isSelected(int index) {
            return (index == selected);
        }

        public Item getSelected() {
            if (rangeCheck(selected)) {
                return items[selected];
            } else {
                throw new ArrayIndexOutOfBoundsException(
                        "Index is out of bounds."
                );
            }
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

        public void next() {
            selected = (selected + 1) % size;
        }

        public void previous() {
            selected = (selected + (size - 1)) % size;
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
    }

    public static final int ROWS = 3;

    public static final int COLS = 9;

    public static final int HOTBAR_SIZE = COLS;

    public static final int SIZE = (ROWS * COLS) + HOTBAR_SIZE;

    private final Item[] items;

    private transient final int rows, cols;

    private transient final int size;

    private final Hotbar hotbar;

    public Inventory() {
        this.rows = ROWS;
        this.cols = COLS;
        this.size = SIZE;
        this.items = new Item[SIZE];
        this.hotbar = new Hotbar();
    }

    public void add(Item item) {

    }

    public void remove(Item item) {

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
