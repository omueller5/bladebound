package net.owen.bladebound.effect;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.owen.bladebound.accessory.BladeboundAccessoryHolder;
import net.owen.bladebound.item.ModItems;
import net.owen.bladebound.util.InventoryUtil;

public final class MurasamePoisonHandler {
    private MurasamePoisonHandler() {}

    // How long you can “hold/possess” Murasame before dying (no gauntlets).
    // Change this to whatever you want.
    private static final int HOLD_DEATH_TICKS = 20 * 20; // 20 seconds

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {

                boolean hasMurasame = InventoryUtil.hasItemAnywhere(player, ModItems.MURASAME);
                boolean hasGauntlets = hasEquippedGauntlets(player);

                RegistryEntry<StatusEffect> curse = BladeboundEffects.MURASAME_CURSE;

                // If they are safe (no murasame OR gauntlets equipped), remove the curse if present.
                if (!hasMurasame || hasGauntlets) {
                    if (player.hasStatusEffect(curse)) {
                        player.removeStatusEffect(curse);
                    }
                    continue;
                }

                // They have Murasame and DO NOT have gauntlets:
                // Apply the curse ONCE and let it count down to death.
                if (!player.hasStatusEffect(curse)) {
                    player.addStatusEffect(new StatusEffectInstance(
                            curse,
                            HOLD_DEATH_TICKS,
                            0,
                            true,   // ambient
                            false,  // showParticles
                            false    // showIcon (set false if you want it fully hidden)
                    ));
                }
            }
        });
    }

    private static boolean hasEquippedGauntlets(ServerPlayerEntity player) {
        Inventory inv = ((BladeboundAccessoryHolder) player).bladebound$getAccessoryInv();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack s = inv.getStack(i);
            if (!s.isEmpty() && s.isOf(ModItems.MURASAME_GAUNTLETS)) return true;
        }
        return false;
    }
}
