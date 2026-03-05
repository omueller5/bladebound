package net.owen.bladebound.accessory;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public final class BladeboundAccessoryInventory extends SimpleInventory {
    public static final int SIZE = 6;

    public BladeboundAccessoryInventory() {
        super(SIZE);
    }

    // --------------------------------------------------
    // NBT access (unchanged)
    // --------------------------------------------------
    public DefaultedList<ItemStack> bladebound$stacks() {
        return this.heldStacks;
    }

    // --------------------------------------------------
    // FIX: ensure inventory changes sync correctly
    // --------------------------------------------------

    @Override
    public void setStack(int slot, ItemStack stack) {
        super.setStack(slot, stack);
        this.markDirty(); // 🔧 REQUIRED
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack result = super.removeStack(slot, amount);
        if (!result.isEmpty()) {
            this.markDirty(); // 🔧 REQUIRED
        }
        return result;
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack result = super.removeStack(slot);
        if (!result.isEmpty()) {
            this.markDirty(); // 🔧 REQUIRED
        }
        return result;
    }
}
