package net.owen.bladebound.magic;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.owen.bladebound.mana.ManaHolder;
import net.owen.bladebound.network.BarrierBreakPayload;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class BarrierManaDrain {

    private BarrierManaDrain() {}

    // MANA PER SECOND
    private static final double BARRIER_DRAIN_PER_SECOND = 30.0;

    // When mana goes to/below this, barrier will auto-break
    private static final int MIN_MANA_TO_KEEP_BARRIER = 0;

    private static final Map<UUID, Double> carry = new HashMap<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(BarrierManaDrain::onEndServerTick);
    }

    private static void onEndServerTick(MinecraftServer server) {
        if (BARRIER_DRAIN_PER_SECOND <= 0.0) return;

        double perTick = BARRIER_DRAIN_PER_SECOND / 20.0;

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (!((Object) player instanceof SpellHolder sh)) continue;

            if (!sh.bladebound$isBarrierActive()) {
                carry.remove(player.getUuid());
                continue;
            }

            if (!((Object) player instanceof ManaHolder mh)) continue;

            // =========================================================
            // IMPORTANT: break immediately if already empty
            // (this avoids "drainNow==0" ticks skipping the break check)
            // =========================================================
            int mana = mh.bladebound$getMana();
            if (mana <= MIN_MANA_TO_KEEP_BARRIER) {
                sh.bladebound$setBarrierActive(false);
                ServerPlayNetworking.send(player, new BarrierBreakPayload());
                carry.remove(player.getUuid());
                continue;
            }

            double buf = carry.getOrDefault(player.getUuid(), 0.0) + perTick;

            int drainNow = (int) Math.floor(buf);
            buf -= drainNow;

            // Nothing to drain this tick (fractional accumulation)
            if (drainNow <= 0) {
                carry.put(player.getUuid(), buf);
                continue;
            }

            int newMana = mana - drainNow;

            // Auto-break this tick if we'd hit 0 (or below minimum)
            if (newMana <= MIN_MANA_TO_KEEP_BARRIER) {
                mh.bladebound$setMana(MIN_MANA_TO_KEEP_BARRIER);
                sh.bladebound$setBarrierActive(false);
                ServerPlayNetworking.send(player, new BarrierBreakPayload());
                carry.remove(player.getUuid());
                continue;
            }

            // Normal drain
            mh.bladebound$setMana(newMana);
            carry.put(player.getUuid(), buf);
        }
    }
}
