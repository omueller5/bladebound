package net.owen.bladebound.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.TintedParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class BlackFlash {
    private BlackFlash() {}

    // =========================================================
    // HOT WINDOW TUNING
    // =========================================================
    public static int denominator = 100;
    public static int hotWindowTicks = 60;
    public static double bonusPerStack = 0.49;
    public static double maxChanceCap = 0.50;
    public static boolean decayOnMiss = true;
    private static final int CLEANUP_GRACE_TICKS = 20 * 30; // 30 seconds

    // Command tag used for 100% testing
    public static final String TEST_TAG = "bladebound_blackflash_test";

    // =========================================================
    // CHAIN / HOT WINDOW STATE
    // =========================================================

    private static final class State {
        int streak;            // number of successful Black Flashes in current window
        long windowEndsAt;     // server tick when hot window expires
        long lastTouchedAt;    // for cleanup
    }

    private static final Map<UUID, State> STATES = new HashMap<>();

    private static State state(UUID id) {
        return STATES.computeIfAbsent(id, k -> new State());
    }

    private static void cleanupOccasionally(long nowTick) {
        // Run cleanup every 64 ticks (cheap)
        if ((nowTick & 0x3F) != 0) return;

        Iterator<Map.Entry<UUID, State>> it = STATES.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, State> e = it.next();
            State s = e.getValue();

            if (nowTick > s.windowEndsAt + CLEANUP_GRACE_TICKS
                    && nowTick > s.lastTouchedAt + CLEANUP_GRACE_TICKS) {
                it.remove();
            }
        }
    }

    private static void normalizeWindow(State s, long nowTick) {
        if (s.windowEndsAt > 0 && nowTick > s.windowEndsAt) {
            s.streak = 0;
            s.windowEndsAt = 0;
        }
        s.lastTouchedAt = nowTick;
    }

    private static double baseChance() {
        if (denominator <= 1) return 1.0;
        return 1.0 / (double) denominator;
    }

    private static double chanceWithStreak(State s) {
        double chance = baseChance() + (s.streak * bonusPerStack);
        if (chance > maxChanceCap) chance = maxChanceCap;
        if (chance < 0.0) chance = 0.0;
        return chance;
    }

    private static void onProc(State s, long nowTick) {
        s.streak += 1;
        s.windowEndsAt = nowTick + hotWindowTicks;
        s.lastTouchedAt = nowTick;
    }

    private static void onMiss(State s, long nowTick) {
        if (s.windowEndsAt > 0 && nowTick <= s.windowEndsAt) {
            if (decayOnMiss) {
                if (s.streak > 0) s.streak -= 1;
                // Keep the window alive; you're still “hot” as long as you're fighting in time.
            } else {
                s.streak = 0;
                s.windowEndsAt = 0;
            }
        } else {
            s.streak = 0;
            s.windowEndsAt = 0;
        }
        s.lastTouchedAt = nowTick;
    }

    // =========================================================
    // PROC CHECK
    // =========================================================

    public static boolean shouldProc(PlayerEntity attacker) {
        // 100% mode for testing
        if (attacker.getCommandTags().contains(TEST_TAG)) return true;

        // Only roll + mutate hot-window state on server
        if (!(attacker.getEntityWorld() instanceof ServerWorld sw)) {
            return false;
        }

        long nowTick = sw.getTime();
        cleanupOccasionally(nowTick);

        State s = state(attacker.getUuid());
        normalizeWindow(s, nowTick);

        double chance = chanceWithStreak(s);
        boolean proc = attacker.getRandom().nextDouble() < chance;

        if (proc) onProc(s, nowTick);
        else onMiss(s, nowTick);

        return proc;
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
        if (!(target.getEntityWorld() instanceof ServerWorld sw)) return;

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
        sw.spawnParticles(TintedParticleEffect.create(ParticleTypes.FLASH, 1.0f, 1.0f, 1.0f), x, y, z, 2, 0, 0, 0, 0);

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
        DustParticleEffect black = new DustParticleEffect(0x0D0D0D, 2.0f);
        DustParticleEffect darkRed = new DustParticleEffect(0xA60D0D, 2.0f);

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

        // Vanilla audio punch (coordinate overload works in 1.21.11)
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
