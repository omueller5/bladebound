package net.owen.bladebound.compat;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public final class AccessoryChecks {
    private AccessoryChecks() {}

    // Reuse one instance instead of creating a new one every cast
    private static final BuiltInAccessoryApi BUILTIN = new BuiltInAccessoryApi();

    public static ItemStack getEquippedAccessoryStack(PlayerEntity player, Item item) {
        // First: ask the active API (Trinkets if present)
        ItemStack stack = AccessoryCompat.api().getEquippedStack(player, item);
        if (!stack.isEmpty()) {
            return stack;
        }

        // Fallback: always check Bladebound built-in accessories
        return BUILTIN.getEquippedStack(player, item);
    }
}
