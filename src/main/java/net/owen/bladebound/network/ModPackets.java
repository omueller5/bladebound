package net.owen.bladebound.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.owen.bladebound.magic.SpellHolder;
import net.owen.bladebound.magic.StaffSpell;
import net.owen.bladebound.mana.ManaHolder;

public class ModPackets {
    public static final Identifier MANA_SYNC = Identifier.of("bladebound", "mana_sync");

    public static void sendMana(ServerPlayerEntity player) {
        ManaHolder mana = (ManaHolder) player;
        ServerPlayNetworking.send(player, new ManaSyncPayload(
                mana.bladebound$getMana(),
                mana.bladebound$getMaxMana()
        ));
    }

    public static void sendSpellState(ServerPlayerEntity player) {
        SpellHolder spells = (SpellHolder) player;

        int count = StaffSpell.values().length;
        int validMask = (count >= 32) ? -1 : ((1 << count) - 1);

        // Ensure only valid bits can ever be sent
        int learned = spells.bladebound$getLearnedMask() & validMask;

        // Ensure starter spells are always learned (0,1,2)
        learned |= 0b0111;

        int sel = spells.bladebound$getSelectedSpell();
        if (sel < 0 || sel >= count) sel = 0;
        if (((learned >> sel) & 1) == 0) sel = 0;

        ServerPlayNetworking.send(player, new SpellStateSyncPayload(learned, sel));
    }
}
