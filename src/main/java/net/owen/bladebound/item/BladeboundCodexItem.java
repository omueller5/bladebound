package net.owen.bladebound.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import net.owen.bladebound.client.ClientHooks;

public class BladeboundCodexItem extends Item {

    public BladeboundCodexItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient()) {
            ClientHooks.openCodexScreen();
        }

        return ActionResult.SUCCESS;
    }
}
