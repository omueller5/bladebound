package net.owen.bladebound.compat.trinkets;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.owen.bladebound.compat.AccessoryApi;

// Trinkets imports ONLY in this file
public final class TrinketsAccessoryApi implements AccessoryApi {

    @Override
    public void init() {
        // register slot groups etc (later)
    }

    @Override
    public boolean isEquipped(PlayerEntity player, ItemStack stack) {
        // implement using Trinkets API (later)
        return false;
    }

    @Override
    public ItemStack getEquippedStack(PlayerEntity player, Item item) {
        // implement using Trinkets API (later)
        return ItemStack.EMPTY;
    }
}
