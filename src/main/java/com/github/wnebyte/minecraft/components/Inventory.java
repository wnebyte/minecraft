package com.github.wnebyte.minecraft.components;

import com.github.wnebyte.minecraft.core.Component;

public class Inventory extends Component {

    public static class Slot {

        private byte blockId;

        private int count;

        public Slot(byte blockId) {
            this(blockId, 0);
        }

        public Slot(byte blockId, int count) {
            this.blockId = blockId;
            this.count = count;
        }

        public byte getBlockId() {
            return blockId;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public void incrementCount() {
            count++;
        }

        public void decrementCount() {
            count--;
        }
    }

    public static final int NUM_SLOTS = 10;

    private final Slot[] slots = new Slot[NUM_SLOTS];

    private int selectedSlot = 0;

    public boolean isSlotSelected(int index) {
        return (selectedSlot == index);
    }

    public Slot getSelectedSlot() {
        if (selectedSlot < NUM_SLOTS) {
            return slots[selectedSlot];
        } else {
            throw new ArrayIndexOutOfBoundsException(
                    "Index if out of bounds."
            );
        }
    }

    public void setSelectedSlot(int index) {
        if (index < NUM_SLOTS) {
            selectedSlot = index;
        } else {
            throw new ArrayIndexOutOfBoundsException(
                    "Index if out of bounds."
            );
        }
    }

    public Slot getSlot(int index) {
        if (index < NUM_SLOTS) {
            return slots[index];
        } else {
            throw new ArrayIndexOutOfBoundsException(
                    "Index if out of bounds."
            );
        }
    }

    public void setSlot(int index, Slot slot) {
        if (index < NUM_SLOTS) {
            slots[index] = slot;
        } else {
            throw new ArrayIndexOutOfBoundsException(
                    "Index if out of bounds."
            );
        }
    }

    public boolean slotIsEmpty(int index) {
        if (index < NUM_SLOTS) {
            return (slots[index] == null);
        } else {
            throw new ArrayIndexOutOfBoundsException(
                    "Index if out of bounds."
            );
        }
    }
}
