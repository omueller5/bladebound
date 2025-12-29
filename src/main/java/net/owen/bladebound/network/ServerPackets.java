package net.owen.bladebound.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.owen.bladebound.magic.SpellHolder;
import net.owen.bladebound.magic.StaffSpell;

public class ServerPackets {

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(SetStaffSpellPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayerEntity player = context.player();
                SpellHolder spells = (SpellHolder) player;

                int idx = payload.index();

                int max = StaffSpell.values().length - 1;
                if (idx < 0) idx = 0;
                if (idx > max) idx = max;

                // If they haven't learned it, don't allow selection
                if (!spells.bladebound$hasLearnedSpell(idx)) {
                    ModPackets.sendSpellState(player); // re-sync client UI
                    return;
                }

                spells.bladebound$setSelectedSpell(idx);

                // Sync back so the client UI and casting are always correct
                ModPackets.sendSpellState(player);
            });
        });
    }
}
