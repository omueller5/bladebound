package net.owen.bladebound.mana;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.owen.bladebound.item.ModItems;

public final class ArchmageHatEffects {
    private ArchmageHatEffects() {}

    public static boolean wearingArchmageHat(PlayerEntity player) {
        ItemStack head = player.getEquippedStack(EquipmentSlot.HEAD);
        return !head.isEmpty() && head.isOf(ModItems.ARCHMAGE_HAT);
    }

    public static float maxManaMultiplier(PlayerEntity player) {
        return wearingArchmageHat(player) ? 1.10f : 1.0f;
    }

    public static float regenMultiplier(PlayerEntity player) {
        return wearingArchmageHat(player) ? 1.25f : 1.0f;
    }
}
