package net.owen.bladebound.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import org.spongepowered.asm.mixin.Mixin;

@Environment(EnvType.CLIENT)
@Mixin(BipedEntityModel.class)
public class BipedEntityModelArmPoseAccessor {

    void bladebound$setLeftArmPose(BipedEntityModel.ArmPose pose) {

    }

    void bladebound$setRightArmPose(BipedEntityModel.ArmPose pose) {

    }
}
