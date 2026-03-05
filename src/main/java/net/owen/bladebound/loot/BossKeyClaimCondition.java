package net.owen.bladebound.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.owen.bladebound.BladeboundBlocks;
import net.owen.bladebound.block.custom.BossLockBlock;

import java.util.Set;

public record BossKeyClaimCondition(int chunkRadius, int yRadius) implements LootCondition {

    // chunkRadius: how many chunks around the loot origin to scan (0 = same chunk)
    // yRadius: vertical scan range around origin
    public static final MapCodec<BossKeyClaimCondition> CODEC =
            RecordCodecBuilder.mapCodec(inst -> inst.group(
                    com.mojang.serialization.Codec.INT.optionalFieldOf("chunk_radius", 1)
                            .forGetter(BossKeyClaimCondition::chunkRadius),
                    com.mojang.serialization.Codec.INT.optionalFieldOf("y_radius", 64)
                            .forGetter(BossKeyClaimCondition::yRadius)
            ).apply(inst, BossKeyClaimCondition::new));

    @Override
    public LootConditionType getType() {
        return ModLootConditions.BOSS_KEY_CLAIM;
    }

    @Override
    public Set<net.minecraft.loot.context.LootContextParameter<?>> getRequiredParameters() {
        return Set.of(); // don't hard-require params
    }

    @Override
    public boolean test(LootContext ctx) {
        if (!(ctx.getWorld() instanceof ServerWorld world)) return false;

        Vec3d origin;
        try {
            origin = ctx.get(LootContextParameters.ORIGIN);
        } catch (Throwable t) {
            origin = null;
        }
        if (origin == null) return false;

        BlockPos o = BlockPos.ofFloored(origin);
        ChunkPos base = new ChunkPos(o);

        BlockPos lockPos = findBossLockInNearbyChunks(world, base, o.getY(), Math.max(0, chunkRadius), Math.max(1, yRadius));
        if (lockPos == null) return false;

        BlockState st = world.getBlockState(lockPos);

        // Enforce "only one key per dungeon"
        if (!st.contains(BossLockBlock.KEY_CLAIMED)) return false;
        if (st.get(BossLockBlock.KEY_CLAIMED)) return false;

        world.setBlockState(lockPos, st.with(BossLockBlock.KEY_CLAIMED, true), 3);
        return true;
    }

    private static BlockPos findBossLockInNearbyChunks(ServerWorld world, ChunkPos base, int originY, int chunkR, int yR) {
        int minY = Math.max(world.getBottomY(), originY - yR);
        int maxY = Math.min(world.getTopY() - 1, originY + yR);

        for (int cx = -chunkR; cx <= chunkR; cx++) {
            for (int cz = -chunkR; cz <= chunkR; cz++) {
                ChunkPos cp = new ChunkPos(base.x + cx, base.z + cz);

                int x0 = cp.getStartX();
                int z0 = cp.getStartZ();

                BlockPos.Mutable p = new BlockPos.Mutable();

                for (int x = x0; x < x0 + 16; x++) {
                    for (int z = z0; z < z0 + 16; z++) {
                        for (int y = minY; y <= maxY; y++) {
                            p.set(x, y, z);
                            if (world.getBlockState(p).isOf(BladeboundBlocks.BOSS_LOCK)) {
                                return p.toImmutable();
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
