package net.owen.bladebound.compat;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public interface AccessoryApi {
    void init();
    boolean isEquipped(PlayerEntity player, ItemStack stack);
}
