package net.owen.bladebound.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.owen.bladebound.accessory.BladeboundAccessoryHolder;
import net.owen.bladebound.accessory.BladeboundAccessoryInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityAccessoryMixin implements BladeboundAccessoryHolder {

    @Unique
    private final BladeboundAccessoryInventory bladebound$accInv = new BladeboundAccessoryInventory();

    @Override
    public Inventory bladebound$getAccessoryInv() {
        return bladebound$accInv;
    }

    @Inject(method = "writeCustomData", at = @At("TAIL"))
    private void bladebound$writeAccessories(WriteView view, CallbackInfo ci) {
        WriteView acc = view.get("BladeboundAccessories");
        Inventories.writeData(acc, bladebound$accInv.bladebound$stacks(), true);
    }

    @Inject(method = "readCustomData", at = @At("TAIL"))
    private void bladebound$readAccessories(ReadView view, CallbackInfo ci) {
        view.getOptionalReadView("BladeboundAccessories")
                .ifPresent(acc -> Inventories.readData(acc, bladebound$accInv.bladebound$stacks()));
    }
}
