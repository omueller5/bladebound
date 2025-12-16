package net.owen.bladebound.compat;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.owen.bladebound.accessory.BladeboundAccessoryHolder;

public final class BuiltInAccessoryApi implements AccessoryApi {

    @Override
    public void init() {
        // nothing required for the fallback storage path
    }

    @Override
    public boolean isEquipped(PlayerEntity player, ItemStack stack) {
        Inventory inv = ((BladeboundAccessoryHolder) player).bladebound$getAccessoryInv();

        for (int i = 0; i < inv.size(); i++) {
            ItemStack equipped = inv.getStack(i);
            // Same item type check (ignore count)
            if (!equipped.isEmpty() && ItemStack.areItemsEqual(equipped, stack)) {
                return true;
            }
        }
        return false;
    }
}
