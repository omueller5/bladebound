package net.owen.bladebound.effect;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.owen.bladebound.accessory.BladeboundAccessoryHolder;
import net.owen.bladebound.item.ModItems;

public final class GauntletImmunityTick {
    private GauntletImmunityTick() {}

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(GauntletImmunityTick::tick);
    }

    private static void tick(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            boolean hasGauntlets = hasEquippedGauntlets(player);

            if (hasGauntlets && player.hasStatusEffect(BladeboundEffects.MURASAME_CURSE)) {
                player.removeStatusEffect(BladeboundEffects.MURASAME_CURSE);
            }
        }
    }

    private static boolean hasEquippedGauntlets(ServerPlayerEntity player) {
        Inventory inv = ((BladeboundAccessoryHolder) player).bladebound$getAccessoryInv();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty() && stack.isOf(ModItems.MURASAME_GAUNTLETS)) {
                return true;
            }
        }
        return false;
    }
}