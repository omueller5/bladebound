package net.owen.bladebound.network;

import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SetSelectedSpellPayload(int index) implements CustomPayload {

    public static final Id<SetSelectedSpellPayload> ID =
            new Id<>(Identifier.of("bladebound", "set_selected_spell"));

    public static final PacketCodec<net.minecraft.network.RegistryByteBuf, SetSelectedSpellPayload> CODEC =
            PacketCodec.tuple(PacketCodecs.VAR_INT, SetSelectedSpellPayload::index, SetSelectedSpellPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
