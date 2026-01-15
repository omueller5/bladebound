package net.owen.bladebound.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import net.owen.bladebound.mana.ManaHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityManaCopyMixin {

    @Inject(method = "copyFrom", at = @At("TAIL"))
    private void bladebound$copyMana(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;

        if ((Object) oldPlayer instanceof ManaHolder oldMana
                && (Object) self instanceof ManaHolder newMana) {

            newMana.bladebound$setMaxMana(oldMana.bladebound$getMaxMana());
            newMana.bladebound$setMana(oldMana.bladebound$getMana());
            newMana.bladebound$setInfiniteMana(oldMana.bladebound$hasInfiniteMana());
        }
    }
}

