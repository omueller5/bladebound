package net.owen.bladebound.mixin.accessor;

import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PersistentProjectileEntity.class)
public interface PersistentProjectileAsItemStackInvoker {

    // Exposes the protected method PersistentProjectileEntity#asItemStack()
    @Invoker("asItemStack")
    ItemStack bladebound$asItemStack();
}
