package net.owen.bladebound.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.owen.bladebound.accessory.BladeboundAccessoryHolder;
import net.owen.bladebound.screen.slot.BladeboundAccessorySlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerScreenHandler.class)
public abstract class PlayerScreenHandlerMixin {

    /**
     * Adds exactly TWO Bladebound accessory slots to the vanilla inventory screen.
     * These are positioned in the column between the player model and the crafting grid.
     */
    @Inject(method = "<init>", at = @At("TAIL"))
    private void bladebound$addAccessorySlots(PlayerInventory inventory, boolean onServer, PlayerEntity owner, CallbackInfo ci) {
        Inventory accInv = ((BladeboundAccessoryHolder) owner).bladebound$getAccessoryInv();
        ScreenHandlerInvoker inv = (ScreenHandlerInvoker) (Object) this;

        // Column between player model and crafting grid (matches your screenshot target area)
        // You can tweak these two values if you want them nudged a few pixels.
        int x = 77;
        int yTop = 8;

        // Slot 0 (top)
        inv.bladebound$invokeAddSlot(new BladeboundAccessorySlot(
                accInv, 0,
                x, yTop,
                -1, // not linked to vanilla armor slot
                () -> true
        ));

        // Slot 1 (below)
        inv.bladebound$invokeAddSlot(new BladeboundAccessorySlot(
                accInv, 1,
                x, yTop + 18,
                -1, // not linked to vanilla armor slot
                () -> true
        ));
    }
}
