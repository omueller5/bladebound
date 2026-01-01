package net.owen.bladebound.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SetStaffSpellPayload(Identifier spellId) implements CustomPayload {

    public static final CustomPayload.Id<SetStaffSpellPayload> ID =
            new CustomPayload.Id<>(Identifier.of("bladebound", "set_staff_spell"));

    // Match your Payloads style: encode Identifier as string
    private static final PacketCodec<ByteBuf, Identifier> IDENTIFIER_AS_STRING =
            PacketCodecs.STRING.xmap(Identifier::of, Identifier::toString);

    public static final PacketCodec<RegistryByteBuf, SetStaffSpellPayload> CODEC =
            PacketCodec.tuple(
                    IDENTIFIER_AS_STRING, SetStaffSpellPayload::spellId,
                    SetStaffSpellPayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
