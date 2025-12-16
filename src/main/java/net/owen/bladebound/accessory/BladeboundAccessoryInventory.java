package net.owen.bladebound.accessory;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public final class BladeboundAccessoryInventory extends SimpleInventory {
    public static final int SIZE = 6;

    public BladeboundAccessoryInventory() {
        super(SIZE);
    }

    // 1.21: we need the backing list for Inventories.writeNbt/readNbt
    public DefaultedList<ItemStack> bladebound$stacks() {
        return this.heldStacks; // SimpleInventory keeps this list
    }
}
