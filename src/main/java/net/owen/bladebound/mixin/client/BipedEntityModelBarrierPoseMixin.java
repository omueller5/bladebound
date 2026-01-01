package net.owen.bladebound.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.owen.bladebound.item.ModItems;
import net.owen.bladebound.magic.SpellHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(BipedEntityModel.class)
public abstract class BipedEntityModelBarrierPoseMixin {

    /**
     * Visual-only: if barrier is active AND a Frieren staff is held,
     * force the vanilla "BLOCK" arm pose (like holding up a shield).
     *
     * Works with click-to-toggle because it does NOT depend on isUsingItem().
     */
    @Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void bladebound$barrierBlockingPose(LivingEntity entity,
                                                float limbAngle, float limbDistance,
                                                float animationProgress, float headYaw, float headPitch,
                                                CallbackInfo ci) {
        if (!(entity instanceof AbstractClientPlayerEntity player)) return;
        if (!((Object) player instanceof SpellHolder sh)) return;
        if (!sh.bladebound$isBarrierActive()) return;

        boolean holdingStaff =
                player.getMainHandStack().isOf(ModItems.FRIEREN_STAFF)
                        || player.getMainHandStack().isOf(ModItems.FRIEREN_STAFF_CREATIVE)
                        || player.getOffHandStack().isOf(ModItems.FRIEREN_STAFF)
                        || player.getOffHandStack().isOf(ModItems.FRIEREN_STAFF_CREATIVE);

        if (!holdingStaff) return;

        if ((Object) this instanceof BipedEntityModelArmPoseAccessor acc) {
            acc.bladebound$setLeftArmPose(BipedEntityModel.ArmPose.BLOCK);
            acc.bladebound$setRightArmPose(BipedEntityModel.ArmPose.BLOCK);
        }
    }
}
