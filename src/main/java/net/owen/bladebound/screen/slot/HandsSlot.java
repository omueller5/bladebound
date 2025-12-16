package net.owen.bladebound.screen.slot;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

import java.util.function.Predicate;

public class HandsSlot extends Slot {
    private final Predicate<ItemStack> validator;

    public HandsSlot(Inventory inv, int index, int x, int y, Predicate<ItemStack> validator) {
        super(inv, index, x, y);
        this.validator = validator;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return validator.test(stack);
    }

    @Override
    public int getMaxItemCount() {
        return 1;
    }
}
