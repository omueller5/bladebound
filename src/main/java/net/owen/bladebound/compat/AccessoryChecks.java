package net.owen.bladebound.compat;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;

public final class AccessoryChecks {
    private AccessoryChecks() {}

    public static boolean isAccessoryEquipped(PlayerEntity player, Item item) {
        // You can extend your AccessoryApi to accept Item, but for now:
        return AccessoryCompat.api().isEquipped(player, item.getDefaultStack());
    }
}
