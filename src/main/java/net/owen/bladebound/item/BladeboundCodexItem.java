package net.owen.bladebound.item;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.owen.bladebound.client.screen.CodexScreen;

public class BladeboundCodexItem extends Item {

    public BladeboundCodexItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (world.isClient) {
            MinecraftClient.getInstance().setScreen(new CodexScreen());
        }

        return TypedActionResult.success(stack, world.isClient());
    }
}
