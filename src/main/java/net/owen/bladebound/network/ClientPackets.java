package net.owen.bladebound.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.owen.bladebound.BladeboundClient;
import net.owen.bladebound.client.spell.ClientSpellState;
import net.owen.bladebound.mana.ManaHolder;

public class ClientPackets {

    // C2S: player selected spell id
    public static void sendSelectedSpellId(Identifier spellId) {
        if (spellId == null) return;
        ClientPlayNetworking.send(new SetStaffSpellPayload(spellId));
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
        // S2C spell state sync (learned IDs + selected ID)
        // -----------------------
        ClientPlayNetworking.registerGlobalReceiver(
                SpellStateSyncPayload.ID,
                (payload, context) -> context.client().execute(() -> {
                    ClientSpellState.set(payload.learnedSpellIds(), payload.selectedSpellId());
                })
        );

        // -----------------------
        // S2C per-spell cooldowns (spellId + ticks)
        // -----------------------
        ClientPlayNetworking.registerGlobalReceiver(Payloads.SpellCooldownS2C.ID, (payload, context) -> {
            context.client().execute(() -> {
                BladeboundClient.clientSetSpellCooldown(payload.spellId(), payload.cooldownTicks());
            });
        });
    }
}
