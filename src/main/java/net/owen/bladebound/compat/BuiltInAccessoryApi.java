package net.owen.bladebound.compat;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.owen.bladebound.accessory.BladeboundAccessoryHolder;

public final class BuiltInAccessoryApi implements AccessoryApi {

    @Override
    public void init() {
        // nothing required for the fallback storage path
    }

    @Override
    public boolean isEquipped(PlayerEntity player, ItemStack stack) {
        return !getEquippedStack(player, stack.getItem()).isEmpty();
    }

    @Override
    public ItemStack getEquippedStack(PlayerEntity player, Item item) {
        Inventory inv = ((BladeboundAccessoryHolder) player).bladebound$getAccessoryInv();

        for (int i = 0; i < inv.size(); i++) {
            ItemStack equipped = inv.getStack(i);
            if (!equipped.isEmpty() && equipped.isOf(item)) {
                // Return the ACTUAL stack from the accessory inventory (keeps NBT)
                return equipped;
            }
        }
        return ItemStack.EMPTY;
    }
}
