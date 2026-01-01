package net.owen.bladebound.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.owen.bladebound.magic.SpellHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityCooldownTickMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void bladebound$tickSpellCooldowns(CallbackInfo ci) {
        PlayerEntity self = (PlayerEntity)(Object)this;
        if (self.getWorld().isClient) return;

        if ((Object) self instanceof SpellHolder sh) {
            sh.bladebound$tickSpellCooldowns();
        }
    }
}
