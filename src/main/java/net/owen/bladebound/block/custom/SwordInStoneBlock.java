package net.owen.bladebound.block.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SwordInStoneBlock extends Block {
    public static final BooleanProperty HAS_SWORD = BooleanProperty.of("has_sword");

    private final ItemStack rewardStack;
    private final BlockState emptyState;

    public SwordInStoneBlock(Settings settings, ItemStack rewardStack, BlockState emptyState) {
        super(settings);
        this.rewardStack = rewardStack;
        this.emptyState = emptyState;
        this.setDefaultState(this.getDefaultState().with(HAS_SWORD, true));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(HAS_SWORD);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos,
                              PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) return ActionResult.SUCCESS;

        if (!state.get(HAS_SWORD)) return ActionResult.PASS;

        if (player instanceof ServerPlayerEntity sp) {
            ItemStack give = rewardStack.copy();
            if (!sp.getInventory().insertStack(give)) {
                sp.dropItem(give, false);
            }
        }

        world.setBlockState(pos, emptyState, Block.NOTIFY_ALL);
        return ActionResult.CONSUME;
    }

}
