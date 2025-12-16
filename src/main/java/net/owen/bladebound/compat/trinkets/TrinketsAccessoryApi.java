package net.owen.bladebound.compat.trinkets;

import net.minecraft.entity.player.PlayerEntity;
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
}
