package net.owen.bladebound.block.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.owen.bladebound.BladeboundBlocks;
import net.owen.bladebound.item.ModItems;

public class BossLockBlock extends Block {

    public static final BooleanProperty KEY_CLAIMED = BooleanProperty.of("key_claimed");

    public BossLockBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.getStateManager().getDefaultState()
                // keep your existing defaults here (locked, etc)
                .with(KEY_CLAIMED, false)
        );
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        // keep your existing properties here too
        builder.add(KEY_CLAIMED);
    }


    public static final BooleanProperty USED = BooleanProperty.of("used");


    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos,
                              PlayerEntity player, BlockHitResult hit) {

        if (world.isClient) return ActionResult.SUCCESS;

        ItemStack main = player.getMainHandStack();
        ItemStack off = player.getOffHandStack();

        boolean hasKeyMain = main.isOf(ModItems.BOSS_KEY);
        boolean hasKeyOff = off.isOf(ModItems.BOSS_KEY);

        if (!hasKeyMain && !hasKeyOff) {
            world.playSound(null, pos, SoundEvents.BLOCK_CHEST_LOCKED, SoundCategory.BLOCKS, 0.8f, 1.0f);
            return ActionResult.CONSUME;
        }

        if (!player.getAbilities().creativeMode) {
            if (hasKeyMain) main.decrement(1);
            else off.decrement(1);
        }

        world.playSound(null, pos, SoundEvents.BLOCK_IRON_DOOR_OPEN, SoundCategory.BLOCKS, 0.9f, 1.1f);
        world.playSound(null, pos, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.BLOCKS, 0.6f, 1.3f);

        if (world instanceof ServerWorld sw) {
            openDetected3x3RedWool(sw, pos);
        }

        // delete lock after use (remove if you want it to stay)
        world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);

        return ActionResult.CONSUME;
    }
    private static void openDetected3x3RedWool(ServerWorld world, BlockPos lockPos) {
        Candidate best = null;

        // How far from the lock we’ll search for the 3x3 door plane
        int rXZ = 8; // sideways/forward radius
        int rY = 5;  // vertical radius

        for (int dx = -rXZ; dx <= rXZ; dx++) {
            for (int dy = -rY; dy <= rY; dy++) {
                for (int dz = -rXZ; dz <= rXZ; dz++) {
                    BlockPos bottomCenter = lockPos.add(dx, dy, dz);

                    // Two possible orientations for the 3-wide door:
                    // - width along X (normal along Z)
                    // - width along Z (normal along X)
                    int countX = count3x3WidthX(world, bottomCenter);
                    int countZ = count3x3WidthZ(world, bottomCenter);

                    if (best == null || countX > best.count) {
                        best = new Candidate(bottomCenter, Orientation.WIDTH_X, countX);
                        if (countX == 9) break;
                    }
                    if (best == null || countZ > best.count) {
                        best = new Candidate(bottomCenter, Orientation.WIDTH_Z, countZ);
                        if (countZ == 9) break;
                    }
                }
            }
        }

        if (best == null) return;

        // Require a real door plane. If you want STRICT (must be a full 3x3), change 7 -> 9.
        if (best.count < 7) return;

        if (best.orientation == Orientation.WIDTH_X) {
            carve3x3WidthX(world, best.bottomCenter);
        } else {
            carve3x3WidthZ(world, best.bottomCenter);
        }
    }
    private static int count3x3WidthX(ServerWorld world, BlockPos bottomCenter) {
        int count = 0;
        for (int dy = 0; dy < 3; dy++) {
            for (int w = -1; w <= 1; w++) {
                BlockPos p = bottomCenter.add(w, dy, 0);
                if (world.getBlockState(p).isOf(BladeboundBlocks.DUNGEON_DOOR)) count++;
            }
        }
        return count;
    }

    private static int count3x3WidthZ(ServerWorld world, BlockPos bottomCenter) {
        int count = 0;
        for (int dy = 0; dy < 3; dy++) {
            for (int w = -1; w <= 1; w++) {
                BlockPos p = bottomCenter.add(0, dy, w);
                if (world.getBlockState(p).isOf(BladeboundBlocks.DUNGEON_DOOR)) count++;
            }
        }
        return count;
    }

    private static void carve3x3WidthX(ServerWorld world, BlockPos bottomCenter) {
        for (int dy = 0; dy < 3; dy++) {
            for (int w = -1; w <= 1; w++) {
                BlockPos p = bottomCenter.add(w, dy, 0);
                if (world.getBlockState(p).isOf(BladeboundBlocks.DUNGEON_DOOR)) {
                    world.setBlockState(p, Blocks.AIR.getDefaultState(), 3);
                }
            }
        }
    }

    private static void carve3x3WidthZ(ServerWorld world, BlockPos bottomCenter) {
        for (int dy = 0; dy < 3; dy++) {
            for (int w = -1; w <= 1; w++) {
                BlockPos p = bottomCenter.add(0, dy, w);
                if (world.getBlockState(p).isOf(BladeboundBlocks.DUNGEON_DOOR)) {
                    world.setBlockState(p, Blocks.AIR.getDefaultState(), 3);
                }
            }
        }
    }

    private enum Orientation { WIDTH_X, WIDTH_Z }

    private record Candidate(BlockPos bottomCenter, Orientation orientation, int count) {}
}
