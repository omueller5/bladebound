package net.owen.bladebound.mixin.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.owen.bladebound.screen.slot.BladeboundAccessorySlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenBladeboundSlotIconMixin extends HandledScreen<PlayerScreenHandler> {

    private static final Identifier GAUNTLET_SLOT_ICON =
            Identifier.of("bladebound", "textures/gui/slot/gauntlet.png");

    public InventoryScreenBladeboundSlotIconMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    // InventoryScreen has drawBackground(DrawContext, float, int, int)
    @Inject(method = "drawBackground", at = @At("TAIL"))
    private void bladebound$drawBladeboundSlotIcons(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {

        for (Slot slot : this.handler.slots) {
            if (!(slot instanceof BladeboundAccessorySlot)) continue;

            int screenX = this.x + slot.x;
            int screenY = this.y + slot.y;

            // Draw icon behind the slot so it's visible even when empty
            context.drawTexture(
                    GAUNTLET_SLOT_ICON,
                    screenX, screenY,
                    0, 0,
                    16, 16,
                    16, 16
            );
        }
    }
}
