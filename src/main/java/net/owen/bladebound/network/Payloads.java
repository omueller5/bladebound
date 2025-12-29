package net.owen.bladebound.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public class Payloads {

    // Staff cooldown started (server -> client)
    public record StaffCooldownS2C(int cooldownTicks) implements CustomPayload {
        public static final Id<StaffCooldownS2C> ID =
                new Id<>(Identifier.of("bladebound", "staff_cooldown_s2c"));

        // FIX: use VAR_INT (INT isn't available in your version)
        public static final PacketCodec<RegistryByteBuf, StaffCooldownS2C> CODEC =
                PacketCodec.tuple(PacketCodecs.VAR_INT, StaffCooldownS2C::cooldownTicks, StaffCooldownS2C::new);

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

        // Staff cooldown timer
        PayloadTypeRegistry.playS2C().register(StaffCooldownS2C.ID, StaffCooldownS2C.CODEC);
    }
}
