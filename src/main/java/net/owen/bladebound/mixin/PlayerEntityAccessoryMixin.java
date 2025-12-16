package net.owen.bladebound.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.inventory.Inventories;
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

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void bladebound$writeAccessories(NbtCompound nbt, CallbackInfo ci) {
        PlayerEntity self = (PlayerEntity) (Object) this;

        NbtCompound acc = new NbtCompound();
        Inventories.writeNbt(acc, bladebound$accInv.bladebound$stacks(), self.getRegistryManager());

        nbt.put("BladeboundAccessories", acc);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void bladebound$readAccessories(NbtCompound nbt, CallbackInfo ci) {
        if (!nbt.contains("BladeboundAccessories", NbtElement.COMPOUND_TYPE)) return;

        PlayerEntity self = (PlayerEntity) (Object) this;

        NbtCompound acc = nbt.getCompound("BladeboundAccessories");
        Inventories.readNbt(acc, bladebound$accInv.bladebound$stacks(), self.getRegistryManager());
    }
}
