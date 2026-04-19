package net.owen.bladebound.world;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.owen.bladebound.BladeboundBlocks;
import net.owen.bladebound.entity.ModEntities;
import net.owen.bladebound.entity.custom.FrierenBossEntity;

import java.util.HashSet;
import java.util.Set;

public final class BossRoomSpawner {

    // Tracks anchors we already processed THIS server session
    private static final Set<Long> SEEN = new HashSet<>();

    private static final int SCAN_RADIUS = 48;
    private static final int SCAN_Y_DOWN = 160;
    private static final int SCAN_Y_UP = 80;

    private BossRoomSpawner() {}

    public static void init() {
        // IMPORTANT: clear between worlds / server restarts (integrated server counts)
        ServerLifecycleEvents.SERVER_STARTED.register(server -> SEEN.clear());
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> SEEN.clear());

        ServerTickEvents.END_WORLD_TICK.register(BossRoomSpawner::tickWorld);
    }

    private static void tickWorld(ServerWorld world) {
        if (world.getPlayers().isEmpty()) return;

        // once per second, keep it cheap
        if ((world.getTime() % 20) != 0) return;

        for (var player : world.getPlayers()) {
            BlockPos base = player.getBlockPos();

            for (int dy = -SCAN_Y_DOWN; dy <= SCAN_Y_UP; dy++) {
                for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS; dx++) {
                    for (int dz = -SCAN_RADIUS; dz <= SCAN_RADIUS; dz++) {

                        BlockPos p = base.add(dx, dy, dz);

                        BlockState st = world.getBlockState(p);
                        if (!st.isOf(BladeboundBlocks.FRIEREN_BOSS_ANCHOR)) continue;

                        long key = p.asLong();
                        if (SEEN.contains(key)) continue;

                        // Only trigger if it’s actually in your dungeon area.
                        if (!isInsideDungeon(world, p)) continue;

                        // Now mark as processed
                        SEEN.add(key);

                        boolean bossExists = !world.getEntitiesByClass(
                                FrierenBossEntity.class,
                                new Box(p).expand(32),
                                e -> e.isAlive()
                        ).isEmpty();

                        if (!bossExists) {
                            FrierenBossEntity boss;
                            boss = new net.owen.bladebound.entity.custom.FrierenBossEntity(ModEntities.FRIEREN_BOSS, world);
                            if (boss != null) {
                                boss.refreshPositionAndAngles(
                                        p.getX() + 0.5, p.getY(), p.getZ() + 0.5,
                                        world.random.nextFloat() * 360.0f, 0.0f
                                );
                                world.spawnEntity(boss);
                            }
                        }

                        // remove anchor -> once per dungeon
                        world.removeBlock(p, false);

                        // one anchor per second per world tick is plenty
                        return;
                    }
                }
            }
        }
    }

    private static boolean isInsideDungeon(ServerWorld world, BlockPos pos) {
        int r = 3;

        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    BlockPos q = pos.add(dx, dy, dz);
                    BlockState s = world.getBlockState(q);

                    // your test dungeon material
                    if (s.isOf(net.minecraft.block.Blocks.RED_WOOL)) return true;

                    // your new unbreakable dungeon brick block (if present)
                    if (s.isOf(BladeboundBlocks.DUNGEON_BRICKS)) return true;

                    // strong “yes this is boss room” signal
                    if (s.isOf(BladeboundBlocks.BOSS_LOCK)) return true;
                }
            }
        }

        return false;
    }
}
