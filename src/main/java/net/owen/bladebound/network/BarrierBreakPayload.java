package net.owen.bladebound.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/*
    BarrierBreakPayload
    -------------------
    Purpose:
    - A tiny S2C "ping" packet used ONLY to tell the client:
        "your barrier just auto-broke because mana ran out"

    Safety / non-game-breaking:
    - Contains ZERO data.
    - No world edits, no entity edits, no gameplay logic.
    - If you register it but never send it, nothing happens.
    - If you send it but never register a client handler, it will not be received (so nothing happens).
*/
public record BarrierBreakPayload() implements CustomPayload {

    // Unique packet id
    public static final Id<BarrierBreakPayload> ID =
            new Id<>(Identifier.of("bladebound", "barrier_break"));

    // Codec for a no-data payload
    public static final PacketCodec<RegistryByteBuf, BarrierBreakPayload> CODEC =
            PacketCodec.unit(new BarrierBreakPayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
