package net.owen.bladebound.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public record SpellStateSyncPayload(Identifier selectedSpellId, List<Identifier> learnedSpellIds) implements CustomPayload {

    public static final CustomPayload.Id<SpellStateSyncPayload> ID =
            new CustomPayload.Id<>(Identifier.of("bladebound", "spell_state_sync"));

    private static final PacketCodec<ByteBuf, Identifier> IDENTIFIER_AS_STRING =
            PacketCodecs.STRING.xmap(Identifier::of, Identifier::toString);

    public static final PacketCodec<RegistryByteBuf, SpellStateSyncPayload> CODEC =
            PacketCodec.ofStatic(SpellStateSyncPayload::write, SpellStateSyncPayload::read);

    private static void write(RegistryByteBuf buf, SpellStateSyncPayload payload) {
        // selected (optional)
        if (payload.selectedSpellId() != null) {
            buf.writeBoolean(true);
            IDENTIFIER_AS_STRING.encode(buf, payload.selectedSpellId());
        } else {
            buf.writeBoolean(false);
        }

        // learned list
        List<Identifier> learned = payload.learnedSpellIds();
        if (learned == null) learned = List.of();

        buf.writeVarInt(learned.size());
        for (Identifier id : learned) {
            // Skip nulls safely
            IDENTIFIER_AS_STRING.encode(buf, id == null ? Identifier.of("minecraft", "air") : id);
        }
    }

    private static SpellStateSyncPayload read(RegistryByteBuf buf) {
        Identifier selected = null;
        boolean hasSelected = buf.readBoolean();
        if (hasSelected) {
            selected = IDENTIFIER_AS_STRING.decode(buf);
        }

        int n = buf.readVarInt();
        List<Identifier> learned = new ArrayList<>(Math.max(0, n));
        for (int i = 0; i < n; i++) {
            Identifier id = IDENTIFIER_AS_STRING.decode(buf);
            // Ignore placeholder "air" if it ever appears
            if (id != null && !id.equals(Identifier.of("minecraft", "air"))) {
                learned.add(id);
            }
        }

        return new SpellStateSyncPayload(selected, learned);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
