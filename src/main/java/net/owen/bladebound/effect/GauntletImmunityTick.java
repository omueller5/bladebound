package net.owen.bladebound.effect;

import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.owen.bladebound.item.ModItems;

public final class GauntletImmunityTick {
    private GauntletImmunityTick() {}

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(GauntletImmunityTick::tick);
    }

    private static void tick(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            boolean hasGauntlets = TrinketsApi.getTrinketComponent(player)
                    .map(comp -> comp.isEquipped(ModItems.MURASAME_GAUNTLETS))
                    .orElse(false);

            if (hasGauntlets && player.hasStatusEffect(BladeboundEffects.MURASAME_CURSE)) {
                player.removeStatusEffect(BladeboundEffects.MURASAME_CURSE);
            }
        }
    }
}
