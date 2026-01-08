package net.owen.bladebound.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.owen.bladebound.combat.BlackFlash;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityBlackFlashMixin {

    @ModifyVariable(
            method = "damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private float bladebound$blackFlashDamage(float amount, DamageSource source) {
        LivingEntity target = (LivingEntity) (Object) this;

        PlayerEntity attacker = BlackFlash.getMeleePlayerAttacker(source);
        if (attacker == null) return amount;

        if (!BlackFlash.shouldProc(attacker)) return amount;

        // Make sure you can SEE it
        BlackFlash.spawnVanillaEffects(target);

        boolean emptyHand = attacker.getMainHandStack().isEmpty();
        return BlackFlash.applyExponent(amount, emptyHand);
    }
}
