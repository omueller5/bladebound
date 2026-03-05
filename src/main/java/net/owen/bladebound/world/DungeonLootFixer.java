package net.owen.bladebound.world;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.owen.bladebound.BladeboundBlocks;

public final class DungeonLootFixer {

    private static final LongOpenHashSet SEEN = new LongOpenHashSet();

    // 1.21.1 expects a RegistryKey<LootTable>
    private static final RegistryKey<LootTable> DUNGEON_LOOT =
            RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of("bladebound", "chest/test_dungeon"));

    private static final int SCAN_R = 64;
    private static final int SCAN_Y_DOWN = 96;
    private static final int SCAN_Y_UP = 64;

    private DungeonLootFixer() {}

    public static void init() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> SEEN.clear());
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> SEEN.clear());
        ServerTickEvents.END_WORLD_TICK.register(DungeonLootFixer::tickWorld);
    }

    private static void tickWorld(ServerWorld world) {
        if (world.getPlayers().isEmpty()) return;

        // once per second
        if ((world.getTime() % 20) != 0) return;

        for (var player : world.getPlayers()) {
            BlockPos base = player.getBlockPos();
            BlockPos.Mutable p = new BlockPos.Mutable();

            for (int dy = -SCAN_Y_DOWN; dy <= SCAN_Y_UP; dy++) {
                for (int dx = -SCAN_R; dx <= SCAN_R; dx++) {
                    for (int dz = -SCAN_R; dz <= SCAN_R; dz++) {
                        p.set(base.getX() + dx, base.getY() + dy, base.getZ() + dz);

                        long posKey = p.asLong();
                        if (SEEN.contains(posKey)) continue;

                        BlockState state = world.getBlockState(p);

                        // HARD WHITELIST: only target literal chest/barrel blocks
                        boolean isAllowedBlock =
                                state.isOf(Blocks.CHEST) ||
                                        state.isOf(Blocks.TRAPPED_CHEST) ||
                                        state.isOf(Blocks.BARREL);

                        if (!isAllowedBlock) continue;

                        BlockEntity be = world.getBlockEntity(p);

                        // HARD WHITELIST: only target the actual chest/barrel block entities
                        boolean isAllowedBE =
                                (be instanceof ChestBlockEntity) ||
                                        (be instanceof BarrelBlockEntity);

                        if (!isAllowedBE) continue;

                        // These should be lootable containers, but keep it safe.
                        if (!(be instanceof LootableContainerBlockEntity lootBe)) continue;

                        if (!isInsideDungeon(world, p)) continue;

                        // If it already has a loot table, never touch it again
                        if (lootBe.getLootTable() != null) {
                            SEEN.add(posKey);
                            continue;
                        }

                        // Assign loot table + seed
                        lootBe.setLootTable(DUNGEON_LOOT, world.random.nextLong());
                        lootBe.markDirty();

                        SEEN.add(posKey);
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
                    if (s.isOf(Blocks.RED_WOOL)) return true;

                    // your real dungeon signals (if present)
                    try {
                        if (s.isOf(BladeboundBlocks.DUNGEON_BRICKS)) return true;
                    } catch (Throwable ignored) {}

                    try {
                        if (s.isOf(BladeboundBlocks.BOSS_LOCK)) return true;
                    } catch (Throwable ignored) {}
                }
            }
        }

        return false;
    }
}
