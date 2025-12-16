package net.owen.bladebound.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.owen.bladebound.accessory.BladeboundAccessoryHolder;
import net.owen.bladebound.screen.slot.BladeboundAccessorySlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.owen.bladebound.accessory.AccessoryHoverState;
import net.owen.bladebound.screen.slot.ConditionalSlot;
import net.minecraft.item.ItemStack;

@Mixin(PlayerScreenHandler.class)
public abstract class PlayerScreenHandlerMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void bladebound$addAccessorySlots(PlayerInventory inventory, boolean onServer, PlayerEntity owner, CallbackInfo ci) {
        Inventory accInv = ((BladeboundAccessoryHolder) owner).bladebound$getAccessoryInv();

        ScreenHandlerInvoker inv = (ScreenHandlerInvoker) (Object) this;

        inv.bladebound$invokeAddSlot(new Slot(accInv, 0, 176, 18));
        inv.bladebound$invokeAddSlot(new Slot(accInv, 1, 176, 36));
        inv.bladebound$invokeAddSlot(new Slot(accInv, 2, 176, 54));
        inv.bladebound$invokeAddSlot(new Slot(accInv, 3, 176, 72));
        inv.bladebound$invokeAddSlot(new Slot(accInv, 4, 176, 90));
        inv.bladebound$invokeAddSlot(new Slot(accInv, 5, 176, 108));

        // Positions match the vanilla armor column (left side), with our slot to the right (+18).
// Armor slot column is at x=8, y=8/26/44/62 in the inventory screen layout.
        int baseX = 8;
        int baseY = 8;

        inv.bladebound$invokeAddSlot(new ConditionalSlot(
                accInv, 0, baseX + 18, baseY + 54,
                () -> AccessoryHoverState.hoveredArmorInvIndex == 36 // boots
        ));

        inv.bladebound$invokeAddSlot(new ConditionalSlot(
                accInv, 1, baseX + 18, baseY + 36,
                () -> AccessoryHoverState.hoveredArmorInvIndex == 37 // leggings
        ));

        inv.bladebound$invokeAddSlot(new ConditionalSlot(
                accInv, 2, baseX + 18, baseY + 18,
                () -> AccessoryHoverState.hoveredArmorInvIndex == 38 // chest
        ));

        inv.bladebound$invokeAddSlot(new ConditionalSlot(
                accInv, 3, baseX + 18, baseY + 0,
                () -> AccessoryHoverState.hoveredArmorInvIndex == 39 // helmet
        ));

        // accInv = ((BladeboundAccessoryHolder) player).bladebound$getAccessoryInv()

        int ax = 26; // just to the right of armor column
        int ay = 8;

        inv.bladebound$invokeAddSlot(new BladeboundAccessorySlot(accInv, 0, ax, ay + 54, 36, () -> true)); // boots-linked
        inv.bladebound$invokeAddSlot(new BladeboundAccessorySlot(accInv, 1, ax, ay + 36, 37, () -> true)); // legs-linked
        inv.bladebound$invokeAddSlot(new BladeboundAccessorySlot(accInv, 2, ax, ay + 18, 38, () -> true)); // chest-linked
        inv.bladebound$invokeAddSlot(new BladeboundAccessorySlot(accInv, 3, ax, ay + 0,  39, () -> true)); // helm-linked


    }
}
