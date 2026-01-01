package net.owen.bladebound.magic.worldrewrite;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.util.*;

/**
 * Server-only manager for "World Rewrite" zones.
 *
 * - Freeze mobs + projectiles inside radius
 * - DO NOT freeze players (movement stays normal)
 * - Actionbar countdown for caster (AQUA->YELLOW near end)
 * - Visual: hollow sphere outline using dark dust particles
 * - Restore everything cleanly
 */
public final class WorldRewriteZoneManager {

    private static ActiveZone active;
    private static final Map<UUID, Snapshot> snapshots = new HashMap<>();

    // Actionbar timer frequency
    private static final int TIMER_INTERVAL_TICKS = 20; // once per second

    // Outline visuals
    private static final int OUTLINE_INTERVAL_TICKS = 2; // every 2 ticks
    private static final int OUTLINE_POINTS = 96;        // more = smoother, more cost
    private static final float OUTLINE_SIZE = 0.9f;      // particle size
    private static final DustParticleEffect OUTLINE_DUST =
            new DustParticleEffect(new Vector3f(0.05f, 0.05f, 0.05f), OUTLINE_SIZE); // very dark gray

    private WorldRewriteZoneManager() {}

    public static boolean hasActiveZone(ServerWorld world) {
        return active != null && active.world == world && active.ticksRemaining > 0;
    }

    public static boolean isInsideActiveZone(ServerWorld world, Vec3d pos) {
        if (!hasActiveZone(world)) return false;
        return active.box.contains(pos);
    }

    public static UUID getActiveCasterUuid(ServerWorld world) {
        if (!hasActiveZone(world)) return null;
        return active.casterUuid;
    }

    public static void start(ServerWorld world, UUID casterUuid, Vec3d center, double radius, int durationTicks) {
        if (active != null) endActiveZone();

        Box box = new Box(
                center.x - radius, center.y - radius, center.z - radius,
                center.x + radius, center.y + radius, center.z + radius
        );

        active = new ActiveZone(world, casterUuid, center, radius, box, durationTicks);

        tick(world); // apply immediately
    }

    public static void tick(ServerWorld world) {
        if (active == null) return;
        if (active.world != world) return;

        if (active.ticksRemaining <= 0) {
            endActiveZone();
            return;
        }

        // === Visual outline (hollow sphere surface only) ===
        if ((active.ticksRemaining % OUTLINE_INTERVAL_TICKS) == 0) {
            spawnOutline(world, active.center, active.radius);
        }

        // === Actionbar countdown (caster only) ===
        if ((active.ticksRemaining % TIMER_INTERVAL_TICKS) == 0 || active.ticksRemaining == 1) {
            ServerPlayerEntity caster = getCaster(world);
            if (caster != null) {
                int secondsLeft = Math.max(0, (active.ticksRemaining + 19) / 20); // round up

                Formatting color = secondsLeft <= 5 ? Formatting.YELLOW : Formatting.AQUA;

                caster.sendMessage(
                        Text.literal("World Rewrite: " + secondsLeft + "s")
                                .formatted(color, Formatting.BOLD),
                        true
                );
            }
        }

        // === Freeze entities each tick (catches entities that enter mid-duration) ===
        List<Entity> entities = world.getOtherEntities(null, active.box);
        for (Entity e : entities) {
            if (!e.isAlive()) continue;

            // Do NOT freeze players (movement stays normal)
            if (e instanceof PlayerEntity) continue;

            snapshots.computeIfAbsent(e.getUuid(), uuid -> Snapshot.capture(e));
            freezeEntity(e);
        }

        active.ticksRemaining--;
        if (active.ticksRemaining <= 0) {
            endActiveZone();
        }
    }

    private static void spawnOutline(ServerWorld world, Vec3d center, double radius) {
        // Random points ON the sphere surface (not inside), so it stays hollow.
        // Using a uniform-ish method: pick z in [-1,1], theta in [0,2pi).
        for (int i = 0; i < OUTLINE_POINTS; i++) {
            double z = world.random.nextDouble() * 2.0 - 1.0;           // [-1, 1]
            double t = world.random.nextDouble() * Math.PI * 2.0;       // [0, 2pi)
            double r = Math.sqrt(Math.max(0.0, 1.0 - z * z));           // circle radius at this z

            double x = r * Math.cos(t);
            double y = z;
            double zz = r * Math.sin(t);

            Vec3d p = center.add(x * radius, y * radius, zz * radius);

            // Spawn exactly 1 particle at that surface point
            world.spawnParticles(OUTLINE_DUST, p.x, p.y, p.z, 1, 0, 0, 0, 0.0);
        }
    }

    private static ServerPlayerEntity getCaster(ServerWorld world) {
        if (active == null) return null;
        if (world.getServer() == null) return null;
        return world.getServer().getPlayerManager().getPlayer(active.casterUuid);
    }

    private static void endActiveZone() {
        if (active != null) {
            ServerWorld world = active.world;

            for (Iterator<Map.Entry<UUID, Snapshot>> it = snapshots.entrySet().iterator(); it.hasNext();) {
                Map.Entry<UUID, Snapshot> entry = it.next();
                Entity e = world.getEntity(entry.getKey());
                if (e != null) entry.getValue().restore(e);
                it.remove();
            }
        }
        active = null;
    }

    private static void freezeEntity(Entity e) {
        e.setNoGravity(true);

        if (e instanceof ProjectileEntity) {
            e.setVelocity(Vec3d.ZERO);
            e.velocityDirty = true;
            return;
        }

        if (e instanceof LivingEntity) {
            e.setVelocity(Vec3d.ZERO);
            e.velocityDirty = true;

            if (e instanceof MobEntity mob) {
                mob.setAiDisabled(true);
            }
        }
    }

    private static final class ActiveZone {
        final ServerWorld world;
        final UUID casterUuid;
        final Vec3d center;
        final double radius;
        final Box box;
        int ticksRemaining;

        ActiveZone(ServerWorld world, UUID casterUuid, Vec3d center, double radius, Box box, int ticksRemaining) {
            this.world = world;
            this.casterUuid = casterUuid;
            this.center = center;
            this.radius = radius;
            this.box = box;
            this.ticksRemaining = ticksRemaining;
        }
    }

    private static final class Snapshot {
        final boolean noGravity;
        final Vec3d velocity;
        final boolean aiDisabled;

        private Snapshot(boolean noGravity, Vec3d velocity, boolean aiDisabled) {
            this.noGravity = noGravity;
            this.velocity = velocity;
            this.aiDisabled = aiDisabled;
        }

        static Snapshot capture(Entity e) {
            boolean ai = false;
            if (e instanceof MobEntity mob) ai = mob.isAiDisabled();
            return new Snapshot(e.hasNoGravity(), e.getVelocity(), ai);
        }

        void restore(Entity e) {
            e.setNoGravity(noGravity);
            e.setVelocity(velocity);
            e.velocityDirty = true;

            if (e instanceof MobEntity mob) {
                mob.setAiDisabled(aiDisabled);
            }
        }
    }
}
