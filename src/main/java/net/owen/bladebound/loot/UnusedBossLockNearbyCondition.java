package net.owen.bladebound.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Set;

import net.owen.bladebound.BladeboundBlocks;
import net.owen.bladebound.block.custom.BossLockBlock;

public record UnusedBossLockNearbyCondition(int radius) implements LootCondition {

    public static final MapCodec<UnusedBossLockNearbyCondition> CODEC =
            RecordCodecBuilder.mapCodec(inst -> inst.group(
                    com.mojang.serialization.Codec.INT.optionalFieldOf("radius", 128)
                            .forGetter(UnusedBossLockNearbyCondition::radius)
            ).apply(inst, UnusedBossLockNearbyCondition::new));

    @Override
    public LootConditionType getType() {
        return ModLootConditions.UNUSED_BOSS_LOCK_NEARBY;
    }

    @Override
    public Set<net.minecraft.loot.context.LootContextParameter<?>> getRequiredParameters() {
        return Set.of();
    }

    @Override
    public boolean test(LootContext ctx) {
        if (!(ctx.getWorld() instanceof ServerWorld world)) return false;

        BlockPos center = null;

        // 1) Most reliable for container loot: BLOCK_ENTITY is the chest
        try {
            BlockEntity be = ctx.get(LootContextParameters.BLOCK_ENTITY);
            if (be != null) center = be.getPos();
        } catch (Throwable ignored) {}

        // 2) Fallback: ORIGIN (if present)
        if (center == null) {
            try {
                Vec3d origin = ctx.get(LootContextParameters.ORIGIN);
                if (origin != null) center = BlockPos.ofFloored(origin);
            } catch (Throwable ignored) {}
        }

        if (center == null) return false;

        int r = Math.max(1, radius);
        BlockPos.Mutable m = new BlockPos.Mutable();

        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    m.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);

                    BlockState st = world.getBlockState(m);
                    if (!st.isOf(BladeboundBlocks.BOSS_LOCK)) continue;

                    // Only allow once per dungeon: first successful chest "consumes" the lock
                    if (st.contains(BossLockBlock.USED) && !st.get(BossLockBlock.USED)) {
                        world.setBlockState(m, st.with(BossLockBlock.USED, true), 3);
                        return true;
                    }

                    // If USED somehow isn't present (older world/state), treat it as unused
                    if (!st.contains(BossLockBlock.USED)) {
                        world.setBlockState(m, st.with(BossLockBlock.USED, true), 3);
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
