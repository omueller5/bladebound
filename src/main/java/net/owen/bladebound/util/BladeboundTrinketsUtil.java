package net.owen.bladebound.util;

import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.entity.player.PlayerEntity;
import net.owen.bladebound.item.ModItems;

public final class BladeboundTrinketsUtil {
    private BladeboundTrinketsUtil() {}

    public static boolean hasCooldownBracelet(PlayerEntity player) {
        return TrinketsApi.getTrinketComponent(player)
                .map(comp -> comp.isEquipped(ModItems.COOLDOWN_BRACELET))
                .orElse(false);
    }

    /** Bracelet effect: cooldown / 2 (minimum 1 tick). */
    public static int applyCooldownBracelet(PlayerEntity player, int cooldownTicks) {
        if (cooldownTicks <= 0) return cooldownTicks;
        if (!hasCooldownBracelet(player)) return cooldownTicks;
        return Math.max(1, cooldownTicks / 2);
    }
}
