package net.owen.bladebound.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.GameRules;
import net.owen.bladebound.accessory.BladeboundAccessoryHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerDropAccessoriesMixin {

    @Inject(method = "dropInventory", at = @At("TAIL"))
    private void bladebound$dropAccessories(CallbackInfo ci) {
        PlayerEntity self = (PlayerEntity) (Object) this;

        // Respect keepInventory gamerule
        if (self.getWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) return;

        if (!(self instanceof BladeboundAccessoryHolder holder)) return;

        Inventory inv = holder.bladebound$getAccessoryInv();
        if (inv == null) return;

        for (int i = 0; i < inv.size(); i++) {
            ItemStack s = inv.getStack(i);
            if (s.isEmpty()) continue;

            self.dropItem(s, true, false);
            inv.setStack(i, ItemStack.EMPTY);
        }
    }
}
