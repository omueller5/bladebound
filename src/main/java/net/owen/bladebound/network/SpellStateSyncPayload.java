package net.owen.bladebound.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SpellStateSyncPayload(int learnedMask, int selected) implements CustomPayload {

    public static final CustomPayload.Id<SpellStateSyncPayload> ID =
            new CustomPayload.Id<>(Identifier.of("bladebound", "spell_state_sync"));

    public static final PacketCodec<RegistryByteBuf, SpellStateSyncPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.VAR_INT, SpellStateSyncPayload::learnedMask,
                    PacketCodecs.VAR_INT, SpellStateSyncPayload::selected,
                    SpellStateSyncPayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
