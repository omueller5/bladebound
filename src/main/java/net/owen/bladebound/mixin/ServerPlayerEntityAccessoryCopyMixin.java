package net.owen.bladebound.mixin;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.owen.bladebound.accessory.BladeboundAccessoryHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityAccessoryCopyMixin {

    @Inject(method = "copyFrom", at = @At("TAIL"))
    private void bladebound$copyAccessoryInventory(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;

        if (!((Object) oldPlayer instanceof BladeboundAccessoryHolder oldHolder)) return;
        if (!((Object) self instanceof BladeboundAccessoryHolder newHolder)) return;

        Inventory oldInv = oldHolder.bladebound$getAccessoryInv();
        Inventory newInv = newHolder.bladebound$getAccessoryInv();
        if (oldInv == null || newInv == null) return;

        int n = Math.min(oldInv.size(), newInv.size());
        for (int i = 0; i < n; i++) {
            ItemStack s = oldInv.getStack(i);
            newInv.setStack(i, s.isEmpty() ? ItemStack.EMPTY : s.copy());
        }
    }
}
