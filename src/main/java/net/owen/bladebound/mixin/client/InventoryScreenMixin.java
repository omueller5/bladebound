package net.owen.bladebound.mixin.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.screen.slot.Slot;
import net.owen.bladebound.accessory.AccessoryHoverState;
import net.owen.bladebound.screen.slot.BladeboundAccessorySlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin {

    @Inject(method = "render", at = @At("HEAD"))
    private void bladebound$updateAccessoryHoverState(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Slot focused = ((HandledScreenFocusedSlotAccessor) this).bladebound$getFocusedSlot();

        if (focused == null) {
            AccessoryHoverState.hoveredArmorInvIndex = -1;
            return;
        }

        int idx = focused.getIndex();

        // Hovering vanilla armor slots (boots..helmet)
        if (idx >= 36 && idx <= 39) {
            AccessoryHoverState.hoveredArmorInvIndex = idx;
            return;
        }

        // Hovering one of our accessory slots: keep the linked armor "active"
        if (focused instanceof BladeboundAccessorySlot bb) {
            AccessoryHoverState.hoveredArmorInvIndex = bb.bladebound$getLinkedArmorIndex();
            return;
        }

        // Otherwise, close
        AccessoryHoverState.hoveredArmorInvIndex = -1;
    }
}
