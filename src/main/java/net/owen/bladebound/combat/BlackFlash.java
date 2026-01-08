// FILE: src/main/java/net/owen/bladebound/combat/BlackFlash.java
package net.owen.bladebound.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import org.joml.Vector3f;

public final class BlackFlash {
    private BlackFlash() {}

    // Canon rarity: 1 / 100000
    public static int denominator = 100_000;

    // Command tag used for 100% testing
    public static final String TEST_TAG = "bladebound_blackflash_test";

    public static boolean shouldProc(PlayerEntity attacker) {
        // 100% mode for testing
        if (attacker.getCommandTags().contains(TEST_TAG)) return true;

        if (denominator <= 1) return true;
        return attacker.getRandom().nextInt(denominator) == 0;
    }

    /**
     * Returns the player if (and only if) this is a direct melee hit from a player.
     * - Melee: source.getSource() == attacker (both are the player)
     * - Projectile: source.getSource() is arrow/trident/etc, attacker is player -> rejected
     */
    public static PlayerEntity getMeleePlayerAttacker(DamageSource source) {
        Entity attacker = source.getAttacker();
        Entity direct = source.getSource();

        if (!(attacker instanceof PlayerEntity p)) return null;
        if (direct != attacker) return null;

        return p;
    }

    /**
     * JJK: destructive power = (normal hit)^(2.5)
     * NOTE: fists often come through as 1.0 damage, and 1^2.5 = 1 (no visible change),
     * so we floor empty-hand hits to 2.0 just to make Black Flash visibly spike on punches.
     * This does NOT affect weapons.
     */
    public static float applyExponent(float baseDamage, boolean emptyHand) {
        if (baseDamage <= 0.0f) return baseDamage;

        float d = baseDamage;
        if (emptyHand) {
            d = Math.max(d, 2.0f); // 1 heart (so fists actually change)
        }

        return (float) Math.pow(d, 2.5);
    }

    /**
     * Vanilla-only, larger/more pronounced Black Flash impact.
     * CHANGE SIZE HERE:
     * - Increase BF_SPREAD_* to make the particle burst reach further.
     * - Increase BF_SPEED_* to make the particles fly outward faster.
     */
    public static void spawnVanillaEffects(LivingEntity target) {
        if (!(target.getWorld() instanceof ServerWorld sw)) return;

        double x = target.getX();
        double y = target.getBodyY(0.6);
        double z = target.getZ();

        // =========================================================
        // CHANGE SIZE HERE (LARGER PRESET)
        // =========================================================
        final double BF_SPREAD_CORE_XZ = 0.9; // core radius (CRIT / ENCHANTED_HIT)
        final double BF_SPREAD_CORE_Y  = 0.7;

        final double BF_SPREAD_DUST_XZ = 1.1; // outer ink radius (black/red dust)
        final double BF_SPREAD_DUST_Y  = 0.8;

        final double BF_SPREAD_SMOKE_XZ = 1.2; // outer smoke/ash radius
        final double BF_SPREAD_SMOKE_Y  = 0.5;

        final double BF_SPEED_CORE = 0.70; // outward velocity
        final double BF_SPEED_DUST = 0.18;
        final double BF_SPEED_SMOKE = 0.04;
        // =========================================================

        // Sharp impact flash (keep tight so it reads as a "hit frame")
        sw.spawnParticles(ParticleTypes.FLASH, x, y, z, 2, 0, 0, 0, 0);

        // Big vanilla crit burst
        sw.spawnParticles(ParticleTypes.CRIT, x, y, z,
                95,
                BF_SPREAD_CORE_XZ, BF_SPREAD_CORE_Y, BF_SPREAD_CORE_XZ,
                BF_SPEED_CORE
        );

        sw.spawnParticles(ParticleTypes.ENCHANTED_HIT, x, y, z,
                80,
                BF_SPREAD_CORE_XZ, BF_SPREAD_CORE_Y, BF_SPREAD_CORE_XZ,
                BF_SPEED_CORE * 0.55
        );

        // Black + red “ink” accents (vanilla dust particles)
        DustParticleEffect black = new DustParticleEffect(new Vector3f(0.05f, 0.05f, 0.05f), 2.0f);
        DustParticleEffect darkRed = new DustParticleEffect(new Vector3f(0.65f, 0.05f, 0.05f), 2.0f);

        sw.spawnParticles(black, x, y, z,
                80,
                BF_SPREAD_DUST_XZ, BF_SPREAD_DUST_Y, BF_SPREAD_DUST_XZ,
                BF_SPEED_DUST
        );

        sw.spawnParticles(darkRed, x, y, z,
                65,
                BF_SPREAD_DUST_XZ, BF_SPREAD_DUST_Y, BF_SPREAD_DUST_XZ,
                BF_SPEED_DUST
        );

        // Expanding smoke fallout (adds scale)
        sw.spawnParticles(ParticleTypes.LARGE_SMOKE, x, y, z,
                22,
                BF_SPREAD_SMOKE_XZ, BF_SPREAD_SMOKE_Y, BF_SPREAD_SMOKE_XZ,
                BF_SPEED_SMOKE
        );

        sw.spawnParticles(ParticleTypes.ASH, x, y, z,
                30,
                BF_SPREAD_SMOKE_XZ, BF_SPREAD_SMOKE_Y, BF_SPREAD_SMOKE_XZ,
                BF_SPEED_SMOKE
        );

        // Vanilla audio punch (coordinate overload works in 1.21.1)
        sw.playSound(null, x, y, z,
                SoundEvents.ENTITY_PLAYER_ATTACK_CRIT,
                SoundCategory.PLAYERS,
                1.1f,
                0.85f
        );

        sw.playSound(null, x, y, z,
                SoundEvents.ENTITY_GENERIC_EXPLODE,
                SoundCategory.PLAYERS,
                0.25f,
                1.8f
        );
    }
}
