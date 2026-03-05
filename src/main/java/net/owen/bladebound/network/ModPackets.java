package net.owen.bladebound.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.owen.bladebound.magic.SpellHolder;
import net.owen.bladebound.magic.StaffSpell;
import net.owen.bladebound.mana.ManaHolder;

import java.util.ArrayList;
import java.util.List;

public class ModPackets {
    public static final Identifier MANA_SYNC = Identifier.of("bladebound", "mana_sync");

    public static void sendMana(ServerPlayerEntity sp) {
        if (!(sp instanceof ManaHolder mana)) return;

        int cur = mana.bladebound$getMana();

        int baseMax = mana.bladebound$getBaseMaxMana();

        ServerPlayNetworking.send(sp, new ManaSyncPayload(cur, baseMax));
    }

    public static void sendSpellState(ServerPlayerEntity player) {
        SpellHolder spells = (SpellHolder) player;

        Identifier selected = spells.bladebound$getSelectedSpellId();

        List<Identifier> learned = new ArrayList<>();
        for (StaffSpell s : StaffSpell.values()) {
            Identifier id = s.id;
            if (id == null) continue;
            if (spells.bladebound$hasLearnedSpell(id)) {
                learned.add(id);
            }
        }

        if (selected != null && !spells.bladebound$hasLearnedSpell(selected)) {
            selected = null;
        }

        ServerPlayNetworking.send(player, new SpellStateSyncPayload(selected, learned));
    }
}
