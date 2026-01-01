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

    public static void sendMana(ServerPlayerEntity player) {
        ManaHolder mana = (ManaHolder) player;
        ServerPlayNetworking.send(player, new ManaSyncPayload(
                mana.bladebound$getMana(),
                mana.bladebound$getMaxMana()
        ));
    }

    /**
     * NEW: ID-based spell state sync.
     * Sends:
     *  - selected spell Identifier (nullable)
     *  - list of learned spell Identifiers
     */
    public static void sendSpellState(ServerPlayerEntity player) {
        SpellHolder spells = (SpellHolder) player;

        // Selected
        Identifier selected = spells.bladebound$getSelectedSpellId();

        // Learned list: use StaffSpell enum as the authoritative list of "known" spells,
        // but DO NOT use ordinal/index. We only send IDs that are actually learned.
        List<Identifier> learned = new ArrayList<>();
        for (StaffSpell s : StaffSpell.values()) {
            Identifier id = s.id; // if your enum uses a getter, replace with s.getId()
            if (id == null) continue;
            if (spells.bladebound$hasLearnedSpell(id)) {
                learned.add(id);
            }
        }

        // If selected isn't learned, clear it (client can fall back)
        if (selected != null && !spells.bladebound$hasLearnedSpell(selected)) {
            selected = null;
        }

        ServerPlayNetworking.send(player, new SpellStateSyncPayload(selected, learned));
    }
}
