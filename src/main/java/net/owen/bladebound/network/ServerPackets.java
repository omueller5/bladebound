package net.owen.bladebound.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.owen.bladebound.magic.SpellHolder;

public class ServerPackets {

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(SetStaffSpellPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayerEntity player = context.player();
                SpellHolder spells = (SpellHolder) player;

                Identifier id = payload.spellId();
                if (id == null) {
                    ModPackets.sendSpellState(player);
                    return;
                }

                // If they haven't learned it, don't allow selection
                if (!spells.bladebound$hasLearnedSpell(id)) {
                    ModPackets.sendSpellState(player); // re-sync client UI
                    return;
                }

                spells.bladebound$setSelectedSpellId(id);

                // Sync back so the client UI and casting are always correct
                ModPackets.sendSpellState(player);
            });
        });
    }
}
