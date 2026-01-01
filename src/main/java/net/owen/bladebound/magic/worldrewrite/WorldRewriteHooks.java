package net.owen.bladebound.magic.worldrewrite;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

/**
 * Registers ticking for WorldRewrite zones.
 */
public final class WorldRewriteHooks {

    private WorldRewriteHooks() {}

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(WorldRewriteHooks::onEndServerTick);
    }

    private static void onEndServerTick(MinecraftServer server) {
        for (ServerWorld world : server.getWorlds()) {
            if (WorldRewriteZoneManager.hasActiveZone(world)) {
                WorldRewriteZoneManager.tick(world);
            }
        }
    }
}
