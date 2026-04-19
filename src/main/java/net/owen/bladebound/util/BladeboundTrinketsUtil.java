package net.owen.bladebound.util;

import net.minecraft.entity.player.PlayerEntity;
import net.owen.bladebound.compat.AccessoryChecks;
import net.owen.bladebound.item.ModItems;

public final class BladeboundTrinketsUtil {
    private BladeboundTrinketsUtil() {}

    public static boolean hasCooldownBracelet(PlayerEntity player) {
        return !AccessoryChecks.getEquippedAccessoryStack(player, ModItems.COOLDOWN_BRACELET).isEmpty()
                || !AccessoryChecks.getEquippedAccessoryStack(player, ModItems.FIXED_COOLDOWN_BRACELET).isEmpty();
    }

    public static int applyCooldownBracelet(PlayerEntity player, int cooldownTicks) {
        if (cooldownTicks <= 0) return cooldownTicks;
        if (!hasCooldownBracelet(player)) return cooldownTicks;
        return Math.max(1, cooldownTicks / 2);
    }
}
