package net.owen.bladebound.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.util.Hand;
import net.owen.bladebound.magic.SpellHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererBarrierPoseMixin {

    /**
     * Visual-only:
     * If barrier is active, always show the vanilla "BLOCK" arm pose.
     * This works even if you're not holding the staff (or holding nothing).
     */
    @Inject(method = "getArmPose", at = @At("HEAD"), cancellable = true)
    private static void bladebound$barrierArmPose(AbstractClientPlayerEntity player,
                                                  Hand hand,
                                                  CallbackInfoReturnable<BipedEntityModel.ArmPose> cir) {
        if (!((Object) player instanceof SpellHolder sh)) return;
        if (!sh.bladebound$isBarrierActive()) return;

        // Force block pose for both hands while barrier is active
        cir.setReturnValue(BipedEntityModel.ArmPose.BLOCK);
    }
}
