package net.owen.bladebound.mixin;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.owen.bladebound.magic.SpellHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityBarrierDamageMixin {

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void bladebound$barrierCancelsDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity self = (PlayerEntity) (Object) this;

        if ((Object) self instanceof SpellHolder sh && sh.bladebound$isBarrierActive()) {
            cir.setReturnValue(false); // cancels damage completely
        }
    }
}
