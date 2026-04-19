package net.owen.bladebound.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.owen.bladebound.entity.ModEntities;
import net.owen.bladebound.magic.SpellHolder;
import net.owen.bladebound.mana.ManaHolder;
import net.owen.bladebound.network.ModPackets;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class BarrierEntityUtil {

    private static boolean TICK_HOOKED = false;

    // Barrier entity behavior
    private static final double SEARCH_RADIUS = 20.0;
    private static final double FRONT_OFFSET = 0.9;

    // =========================================================
    // MANA DRAIN (EDIT HERE)
    // 30 mana per second @ 20 TPS
    // =========================================================
    private static final int MANA_DRAIN_PER_SECOND = 30;
    private static final int TPS = 20;

    // Accumulator for fractional drain (because 30 / 20 = 1.5 per tick)
    private static final Map<UUID, Float> MANA_DRAIN_ACCUMULATOR = new HashMap<>();

    // How often to send mana sync packets (avoid spamming packets every tick)
    private static final int MANA_SYNC_INTERVAL_TICKS = 5;

    // Anti-spam for "out of mana" message
    private static final Map<UUID, Long> NEXT_OOM_MESSAGE_TICK = new HashMap<>();

    private BarrierEntityUtil() {}

    public static void onBarrierToggled(ServerWorld world, PlayerEntity player, boolean nowActive) {
        ensureTickHooked();

        if (nowActive) {
            ensureBarrierExists(world, player);
        } else {
            removeBarrier(world, player);
        }
    }

    private static void ensureTickHooked() {
        if (TICK_HOOKED) return;
        TICK_HOOKED = true;

        ServerTickEvents.END_WORLD_TICK.register(BarrierEntityUtil::tickWorld);
    }

    private static void tickWorld(ServerWorld world) {
        long now = world.getTime();

        for (ServerPlayerEntity player : world.getPlayers()) {
            if (!(player instanceof SpellHolder sh)) continue;

            boolean active = sh.bladebound$isBarrierActive();

            if (!active) {
                // Cleanup if toggled off
                removeBarrier(world, player);
                continue;
            }

            // Active: drain mana (survival staff only)
            boolean exemptFromDrain = isHoldingCreativeStaff(player);

            if (!exemptFromDrain) {
                if (!(player instanceof ManaHolder mh)) {
                    // If somehow no mana component, force off
                    forceOff(world, player, sh, now, "Barrier disabled.");
                    continue;
                }

                int current = mh.bladebound$getMana();

                // Accumulate fractional drain so 1.5/tick works correctly
                float acc = MANA_DRAIN_ACCUMULATOR.getOrDefault(player.getUuid(), 0f);
                acc += (float) MANA_DRAIN_PER_SECOND / TPS;  // 30 / 20 = 1.5 per tick

                int drainNow = (int) acc;
                acc -= drainNow;
                MANA_DRAIN_ACCUMULATOR.put(player.getUuid(), acc);

                // Only drain when we have at least 1 whole mana accumulated
                if (drainNow > 0) {
                    if (current < drainNow) {
                        forceOff(world, player, sh, now, "Barrier ran out of mana!");
                        MANA_DRAIN_ACCUMULATOR.remove(player.getUuid());
                        continue;
                    }

                    mh.bladebound$setMana(current - drainNow);
                }


                // Sync mana periodically (not every tick)
                if ((now % MANA_SYNC_INTERVAL_TICKS) == 0) {
                    ModPackets.sendMana(player);
                }
            }

            // Keep barrier alive while active
            ensureBarrierExists(world, player);
        }
    }

    private static void forceOff(ServerWorld world, ServerPlayerEntity player, SpellHolder sh, long now, String msg) {
        MANA_DRAIN_ACCUMULATOR.remove(player.getUuid());
        sh.bladebound$setBarrierActive(false);
        removeBarrier(world, player);

        // Rate-limit the message to once per second per player
        long nextOk = NEXT_OOM_MESSAGE_TICK.getOrDefault(player.getUuid(), 0L);
        if (now >= nextOk) {
            player.sendMessage(Text.literal(msg), true);
            NEXT_OOM_MESSAGE_TICK.put(player.getUuid(), now + 20);
        }

        // Sync mana once when we force off
        ModPackets.sendMana(player);
    }

    private static void ensureBarrierExists(ServerWorld world, PlayerEntity player) {
        if (findExistingBarrier(world, player) != null) return;

        Entity barrier = new net.owen.bladebound.entity.BarrierEntity(ModEntities.BARRIER, world);
        if (barrier == null) return;

        Vec3d eye = player.getEyePos();
        Vec3d look = player.getRotationVec(1.0f).normalize();
        Vec3d spawn = eye.add(look.multiply(FRONT_OFFSET));

        barrier.refreshPositionAndAngles(spawn.x, spawn.y, spawn.z, player.getYaw(), player.getPitch());
        world.spawnEntity(barrier);
    }

    private static void removeBarrier(ServerWorld world, PlayerEntity player) {
        Entity existing = findExistingBarrier(world, player);
        if (existing != null) existing.discard();
    }

    private static Entity findExistingBarrier(ServerWorld world, PlayerEntity player) {
        Box box = player.getBoundingBox().expand(SEARCH_RADIUS);
        List<Entity> list = world.getOtherEntities(player, box, e -> e.getType() == ModEntities.BARRIER);
        return list.isEmpty() ? null : list.get(0);
    }

    // Uses your existing creative staff item (same one you check elsewhere)
    private static boolean isHoldingCreativeStaff(ServerPlayerEntity player) {
        return player.getMainHandStack() != null
                && !player.getMainHandStack().isEmpty()
                && player.getMainHandStack().isOf(net.owen.bladebound.item.ModItems.FRIEREN_STAFF_CREATIVE);
    }
}
