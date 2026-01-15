package net.owen.bladebound.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.owen.bladebound.network.ModPackets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityWorldChangeMixin {

    @Inject(method = "worldChanged", at = @At("TAIL"))
    private void bladebound$syncManaAfterWorldChange(ServerWorld origin, CallbackInfo ci) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;

        // FIX: force immediate mana sync after dimension change
        ModPackets.sendMana(self);
    }
}
