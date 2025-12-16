package net.owen.bladebound.block.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SwordInStoneBlock extends Block {

    public static final BooleanProperty SWORD = BooleanProperty.of("sword");

    private final boolean defaultHasSword;
    private final Item excaliburItem;

    /**
     * @param defaultHasSword true = placed with sword, false = placed empty
     */
    public SwordInStoneBlock(Settings settings, boolean defaultHasSword) {
        super(settings);
        this.defaultHasSword = defaultHasSword;
        this.excaliburItem = Registries.ITEM.get(Identifier.of("bladebound", "excalibur"));

        // default state must be set after properties exist
        this.setDefaultState(this.getStateManager().getDefaultState().with(SWORD, defaultHasSword));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(SWORD);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        return pullSword(state, world, pos, player) ? ActionResult.success(world.isClient) : ActionResult.PASS;
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos,
                                             PlayerEntity player, Hand hand, BlockHitResult hit) {
        return pullSword(state, world, pos, player)
                ? ItemActionResult.success(world.isClient)
                : ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    private boolean pullSword(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        if (!state.get(SWORD)) return false; // already empty

        if (!world.isClient) {
            ItemStack excalibur = new ItemStack(excaliburItem);

            if (!player.getInventory().insertStack(excalibur)) {
                player.dropItem(excalibur, false);
            }

            world.setBlockState(pos, state.with(SWORD, false), Block.NOTIFY_ALL);

            world.playSound(null, pos, SoundEvents.BLOCK_STONE_BREAK, SoundCategory.BLOCKS, 0.8f, 1.0f);
            world.playSound(null, pos, SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.BLOCKS, 0.6f, 1.2f);
        }

        return true;
    }
}
