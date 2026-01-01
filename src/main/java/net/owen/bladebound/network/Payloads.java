package net.owen.bladebound.network;

import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public class Payloads {

    // Spell cooldown started/updated (server -> client)
    public record SpellCooldownS2C(Identifier spellId, int cooldownTicks) implements CustomPayload {
        public static final Id<SpellCooldownS2C> ID =
                new Id<>(Identifier.of("bladebound", "spell_cooldown_s2c"));

        // Your version doesn't have PacketCodecs.IDENTIFIER, so encode the identifier as a string.
        private static final PacketCodec<ByteBuf, Identifier> IDENTIFIER_AS_STRING =
                PacketCodecs.STRING.xmap(Identifier::of, Identifier::toString);

        public static final PacketCodec<RegistryByteBuf, SpellCooldownS2C> CODEC =
                PacketCodec.tuple(
                        IDENTIFIER_AS_STRING, SpellCooldownS2C::spellId,
                        PacketCodecs.VAR_INT,  SpellCooldownS2C::cooldownTicks,
                        SpellCooldownS2C::new
                );

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    private static boolean registered = false;

    public static void register() {
        if (registered) return;
        registered = true;

        PayloadTypeRegistry.playS2C().register(ManaSyncPayload.ID, ManaSyncPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SetStaffSpellPayload.ID, SetStaffSpellPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SpellStateSyncPayload.ID, SpellStateSyncPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(BarrierBreakPayload.ID, BarrierBreakPayload.CODEC);

        // New per-spell cooldown packet
        PayloadTypeRegistry.playS2C().register(SpellCooldownS2C.ID, SpellCooldownS2C.CODEC);
    }
}
