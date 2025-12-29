package net.owen.bladebound.network;

import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SetStaffSpellPayload(int index) implements CustomPayload {

    public static final CustomPayload.Id<SetStaffSpellPayload> ID =
            new CustomPayload.Id<>(Identifier.of("bladebound", "set_staff_spell"));

    public static final PacketCodec<net.minecraft.network.RegistryByteBuf, SetStaffSpellPayload> CODEC =
            PacketCodec.tuple(PacketCodecs.VAR_INT, SetStaffSpellPayload::index, SetStaffSpellPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
