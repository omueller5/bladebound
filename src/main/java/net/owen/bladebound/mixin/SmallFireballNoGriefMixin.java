package net.owen.bladebound.mixin;

import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SmallFireballEntity.class)
public class SmallFireballNoGriefMixin {

    /**
     * Cancels the block-ignite behavior when small fireballs hit blocks.
     * Entity damage still happens via onEntityHit.
     */
    @Inject(method = "onBlockHit", at = @At("HEAD"), cancellable = true)
    private void bladebound$noGriefOnBlockHit(BlockHitResult hit, CallbackInfo ci) {
        ci.cancel();
    }
}
