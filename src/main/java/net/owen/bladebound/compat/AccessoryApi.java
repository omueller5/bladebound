package net.owen.bladebound.compat;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public interface AccessoryApi {
    void init();

    /** True if an equivalent accessory is equipped. */
    boolean isEquipped(PlayerEntity player, ItemStack stack);

    /**
     * Return the ACTUAL equipped stack for this item (preserves NBT like your 10–35% roll).
     * Return ItemStack.EMPTY if not equipped.
     */
    ItemStack getEquippedStack(PlayerEntity player, Item item);
}
