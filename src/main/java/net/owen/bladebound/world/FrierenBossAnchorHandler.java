package net.owen.bladebound.world;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.owen.bladebound.BladeboundBlocks;
import net.owen.bladebound.entity.ModEntities;
import net.owen.bladebound.entity.custom.FrierenBossEntity;

public final class FrierenBossAnchorHandler {

    private static final LongOpenHashSet SEEN = new LongOpenHashSet();

    private FrierenBossAnchorHandler() {}

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(FrierenBossAnchorHandler::onServerTick);
    }

    private static void onServerTick(MinecraftServer server) {
        if ((server.getTicks() % 20) != 0) return;

        for (ServerWorld world : server.getWorlds()) {


            world.getPlayers().forEach(player -> {
                BlockPos center = player.getBlockPos();
                int r = 64;

                BlockPos.Mutable m = new BlockPos.Mutable();

                for (int dx = -r; dx <= r; dx++) {
                    for (int dy = -r; dy <= r; dy++) {
                        for (int dz = -r; dz <= r; dz++) {
                            m.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);

                            if (!world.getBlockState(m).isOf(BladeboundBlocks.FRIEREN_BOSS_ANCHOR_BUILDER)) continue;

                            if (!isBossLockNearby(world, m, 128)) {
                                continue;
                            }

                            long key = m.asLong();
                            if (SEEN.contains(key)) continue;
                            SEEN.add(key);

                            Box check = new Box(m).expand(48);
                            boolean already = !world.getEntitiesByClass(
                                    FrierenBossEntity.class,
                                    check,
                                    e -> e.isAlive()
                            ).isEmpty();

                            if (!already) {
                                FrierenBossEntity boss = new FrierenBossEntity(ModEntities.FRIEREN_BOSS, world);
                                if (boss != null) {
                                    boss.refreshPositionAndAngles(m.getX() + 0.5, m.getY(), m.getZ() + 0.5, 0.0F, 0.0F);
                                    boss.initialize(world, world.getLocalDifficulty(m), SpawnReason.STRUCTURE, null);
                                    world.spawnEntity(boss);
                                }
                            }

                            // Remove anchor either way so it can’t keep trying
                            world.removeBlock(m, false);

                            // Stop after handling one anchor this tick for this player (keeps it very cheap)
                            return;
                        }
                    }
                }
            });
        }
    }

    private static boolean isBossLockNearby(ServerWorld world, BlockPos center, int radius) {
        int r = Math.max(1, radius);
        BlockPos.Mutable p = new BlockPos.Mutable();

        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    p.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    if (world.getBlockState(p).isOf(BladeboundBlocks.BOSS_LOCK)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
