package net.owen.bladebound.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin(BipedEntityModel.class)
public interface BipedEntityModelArmPoseAccessor {

    @Accessor("leftArmPose")
    void bladebound$setLeftArmPose(BipedEntityModel.ArmPose pose);

    @Accessor("rightArmPose")
    void bladebound$setRightArmPose(BipedEntityModel.ArmPose pose);
}
