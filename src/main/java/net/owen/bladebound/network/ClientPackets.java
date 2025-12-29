package net.owen.bladebound.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.owen.bladebound.BladeboundClient;
import net.owen.bladebound.mana.ManaHolder;

public class ClientPackets {

    // C2S: player selected spell index (NOT staff NBT anymore)
    public static void sendStaffSpellIndex(int index) {
        ClientPlayNetworking.send(new SetStaffSpellPayload(index));
    }

    public static void register() {
        // -----------------------
        // S2C mana sync
        // -----------------------
        ClientPlayNetworking.registerGlobalReceiver(ManaSyncPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                MinecraftClient mc = context.client();
                if (mc.player == null) return;

                ManaHolder holder = (ManaHolder) mc.player;
                holder.bladebound$setMaxMana(payload.maxMana());
                holder.bladebound$setMana(payload.mana());
            });
        });

        // -----------------------
        // S2C spell state sync (learned mask + selected)
        // -----------------------
        ClientPlayNetworking.registerGlobalReceiver(
                SpellStateSyncPayload.ID,
                (payload, context) -> context.client().execute(() -> {
                    net.owen.bladebound.client.ClientSpellState.set(payload.learnedMask(), payload.selected());
                })
        );

        // -----------------------
        // S2C staff cooldown timer (ticks)
        // -----------------------
        ClientPlayNetworking.registerGlobalReceiver(Payloads.StaffCooldownS2C.ID, (payload, context) -> {
            context.client().execute(() -> {
                // store the end tick inside BladeboundClient so HUD can show countdown
                BladeboundClient.clientStartStaffCooldown(payload.cooldownTicks());
            });
        });
    }
}
