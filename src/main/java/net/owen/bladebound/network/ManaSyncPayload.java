package net.owen.bladebound.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ManaSyncPayload(int mana, int maxMana) implements CustomPayload {

    public static final Id<ManaSyncPayload> ID =
            new Id<>(Identifier.of("bladebound", "mana_sync"));

    // ✅ Uses Mojang's built-in integer codecs (no manual writeInt/readInt needed)
    public static final PacketCodec<RegistryByteBuf, ManaSyncPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.INTEGER, ManaSyncPayload::mana,
                    PacketCodecs.INTEGER, ManaSyncPayload::maxMana,
                    ManaSyncPayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
