package net.owen.bladebound.magic;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.Entity;
import net.owen.bladebound.entity.ModEntities;

public final class BarrierEntityUtil {

    private BarrierEntityUtil() {}

    /** Finds an existing barrier entity owned by this player (best-effort). */
    public static Entity findBarrierFor(ServerWorld world, ServerPlayerEntity player) {
        // If you already have a custom BarrierEntity with an owner field, use that instead.
        // This generic scan works without NBT, but is less efficient (fine for 1 entity/player).
        for (Entity e : world.iterateEntities()) {
            if (e.getType() != ModEntities.BARRIER) continue;

            // If your barrier entity is always kept at the player's position, this is enough:
            if (e.squaredDistanceTo(player) < 4.0) {
                return e;
            }
        }
        return null;
    }

    /** Spawns a barrier entity at the player's position if one isn't already there. */
    public static void spawnBarrier(ServerPlayerEntity player) {
        if (!(player.getWorld() instanceof ServerWorld sw)) return;

        Entity existing = findBarrierFor(sw, player);
        if (existing != null && existing.isAlive()) return;

        Entity barrier = ModEntities.BARRIER.create(sw);
        if (barrier == null) return;

        Vec3d p = player.getPos();
        barrier.refreshPositionAndAngles(p.x, p.y, p.z, player.getYaw(), 0.0f);
        barrier.setNoGravity(true);

        sw.spawnEntity(barrier);
    }

    /** Removes the nearby barrier entity. */
    public static void removeBarrier(ServerPlayerEntity player) {
        if (!(player.getWorld() instanceof ServerWorld sw)) return;

        Entity existing = findBarrierFor(sw, player);
        if (existing != null) {
            existing.discard();
        }
    }
}
