package net.owen.bladebound.fx;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class ZenitsuTrailTicker {

    private ZenitsuTrailTicker() {}

    // uuid -> ticks remaining
    private static final Map<UUID, Integer> ACTIVE = new HashMap<>();
    // uuid -> last look direction (normalized)
    private static final Map<UUID, Vec3d> LOOK = new HashMap<>();

    private static boolean registered = false;

    public static void ensureRegistered() {
        if (registered) return;
        registered = true;

        ServerTickEvents.END_SERVER_TICK.register(ZenitsuTrailTicker::tick);
    }

    /**
     * Start trailing lightning from the player for a few ticks.
     */
    public static void start(ServerPlayerEntity sp, Vec3d lookDir, int ticks) {
        if (sp == null || sp.getServer() == null) return;
        ensureRegistered();

        UUID id = sp.getUuid();
        ACTIVE.put(id, Math.max(1, ticks));
        LOOK.put(id, lookDir.normalize());
    }

    private static void tick(MinecraftServer server) {
        if (ACTIVE.isEmpty()) return;

        Iterator<Map.Entry<UUID, Integer>> it = ACTIVE.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Integer> e = it.next();
            UUID id = e.getKey();
            int left = e.getValue();

            ServerPlayerEntity sp = server.getPlayerManager().getPlayer(id);
            if (sp == null || !sp.isAlive()) {
                it.remove();
                LOOK.remove(id);
                continue;
            }

            if (!(sp.getWorld() instanceof ServerWorld sw)) {
                left--;
            } else {
                Vec3d look = LOOK.getOrDefault(id, sp.getRotationVec(1.0f).normalize());
                // short “bolt” behind player (waist height)
                Vec3d base = sp.getPos().add(0.0, 0.9, 0.0);
                Vec3d back = look.multiply(-1.0).normalize();

                // Make it a little longer for the first ticks, then shorter (feels like dissipating trail)
                double len = (left >= 6) ? 3.4 : (left >= 3) ? 2.7 : 2.1;

                Vec3d a = base;
                Vec3d b = base.add(back.multiply(len));

                ThunderTrailFx.spawn(sw, a, b);

                left--;
            }

            if (left <= 0) {
                it.remove();
                LOOK.remove(id);
            } else {
                e.setValue(left);
            }
        }
    }
}
