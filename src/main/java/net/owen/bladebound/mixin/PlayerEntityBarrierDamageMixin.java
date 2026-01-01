package net.owen.bladebound.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.owen.bladebound.magic.SpellHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityBarrierDamageMixin {

    // Wider = blocks more side angles. Smaller = more strict “in front”.
    // 0.35 is a good starting point (blocks roughly a bit over a 100° cone).
    private static final double FRONT_DOT_THRESHOLD = 0.35;

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void bladebound$barrierBlocksFrontOnly(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity self = (PlayerEntity) (Object) this;

        if (!(self instanceof SpellHolder sh)) return;
        if (!sh.bladebound$isBarrierActive()) return;

        Vec3d incomingDir = getIncomingDirection(self, source);
        if (incomingDir == null) return; // no direction (fall, void, starvation, etc.)

        Vec3d look = self.getRotationVec(1.0f).normalize();
        double dot = look.dotProduct(incomingDir); // 1=front, 0=side, -1=behind

        // If hit is too far to the side / behind, DO NOT block
        if (dot < FRONT_DOT_THRESHOLD) return;

        // Otherwise: block it ✅
        cir.setReturnValue(false);
    }

    /**
     * Direction from player -> damage origin (attacker or position).
     */
    private static Vec3d getIncomingDirection(PlayerEntity player, DamageSource source) {
        Vec3d playerPos = player.getPos();

        Entity attacker = source.getAttacker();
        if (attacker != null) {
            Vec3d from = attacker.getPos().subtract(playerPos);
            if (from.lengthSquared() > 1.0e-6) return from.normalize();
        }

        Vec3d pos = source.getPosition();
        if (pos != null) {
            Vec3d from = pos.subtract(playerPos);
            if (from.lengthSquared() > 1.0e-6) return from.normalize();
        }

        return null;
    }
}
