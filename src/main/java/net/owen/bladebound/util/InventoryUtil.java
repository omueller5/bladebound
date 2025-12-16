package net.owen.bladebound.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;

public final class InventoryUtil {
    private InventoryUtil() {}

    public static boolean hasItemAnywhere(PlayerEntity player, Item item) {
        // main inventory + hotbar + armor/offhand are all in PlayerInventory
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.size(); i++) {
            if (!inv.getStack(i).isEmpty() && inv.getStack(i).isOf(item)) {
                return true;
            }
        }
        return false;
    }
}
