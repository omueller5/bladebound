package net.owen.bladebound.fx;

import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

public final class ThunderTrailFx {
    private ThunderTrailFx() {}

    // Zenitsu yellow (tweak if you want more orange or gold)
    private static final Vector3f YELLOW = new Vector3f(1.0f, 0.9f, 0.2f);
    private static final float SIZE = 1.2f;

    /**
     * Spawns a lightning-like particle trail between two points.
     * ServerWorld.spawnParticles will replicate to clients automatically.
     */
    public static void spawn(ServerWorld sw, Vec3d start, Vec3d end) {
        double dist = start.distanceTo(end);
        if (dist <= 0.01) return;

        int steps = Math.max(10, (int) (dist * 8.0));
        double jitter = 0.10;

        DustParticleEffect yellowDust = new DustParticleEffect(YELLOW, SIZE);

        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;
            Vec3d p = start.lerp(end, t);

            double jx = (sw.random.nextDouble() * 2 - 1) * jitter;
            double jy = (sw.random.nextDouble() * 2 - 1) * (jitter * 0.6);
            double jz = (sw.random.nextDouble() * 2 - 1) * jitter;

            double x = p.x + jx;
            double y = p.y + 0.10 + jy;
            double z = p.z + jz;

            // === Main yellow lightning ===
            sw.spawnParticles(
                    yellowDust,
                    x, y, z,
                    1,
                    0, 0, 0,
                    0.0
            );

            // === Accent sparks (keep subtle) ===
            if ((i % 3) == 0) {
                sw.spawnParticles(
                        ParticleTypes.ELECTRIC_SPARK,
                        x, y, z,
                        1,
                        0, 0, 0,
                        0.0
                );
            }
        }

        // Subtle flash at start/end (still white, but brief)
        sw.spawnParticles(ParticleTypes.FLASH, start.x, start.y + 0.1, start.z, 1, 0, 0, 0, 0.0);
        sw.spawnParticles(ParticleTypes.FLASH, end.x, end.y + 0.1, end.z, 1, 0, 0, 0, 0.0);
    }
}
