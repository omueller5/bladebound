package net.owen.bladebound.entity.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.owen.bladebound.entity.custom.FrierenBossEntity;

import java.util.EnumSet;
import java.util.concurrent.ThreadLocalRandom;

public class FrierenSpellcastingGoal extends Goal {

    private final FrierenBossEntity mob;

    // Offensive casts: every 10–15 seconds
    private int offensiveCooldownTicks = 60;

    // Heal: limited uses, only when low
    private int healCooldownTicks = 0;
    private int healsRemaining = 3;

    // 0-100 weight for zoltraak vs firebolt (ex: 70 = 70% zoltraak)
    private static final int ZOLTRAAK_CHANCE_PERCENT = 70;

    public FrierenSpellcastingGoal(FrierenBossEntity mob) {
        this.mob = mob;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        return mob.getTarget() != null && mob.getTarget().isAlive();
    }

    @Override
    public boolean shouldContinue() {
        return canStart();
    }

    @Override
    public void tick() {
        if (!(mob.getEntityWorld() instanceof ServerWorld sw)) return;

        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive()) return;

        mob.getLookControl().lookAt(target, 30.0f, 30.0f);

        // -------------------------
        // Heal (limited)
        // -------------------------
        if (healCooldownTicks > 0) healCooldownTicks--;

        if (healsRemaining > 0 && healCooldownTicks <= 0) {
            float hp = mob.getHealth();
            float max = mob.getMaxHealth();

            if (hp <= max * 0.40f) {
                castHeal(sw, mob);
                healsRemaining--;
                healCooldownTicks = 20 * 20; // 20s
                return;
            }
        }

        // -------------------------
        // Offensive (10–15 seconds)
        // -------------------------
        if (offensiveCooldownTicks > 0) {
            offensiveCooldownTicks--;
            return;
        }

        // Weighted random: Zoltraak more often than Firebolt
        int roll = ThreadLocalRandom.current().nextInt(100);
        if (roll < ZOLTRAAK_CHANCE_PERCENT) {
            castZoltraak(sw, mob);
        } else {
            castFirebolt(sw, mob, 1.2);
        }

        offensiveCooldownTicks = randomBetween(20 * 10, 20 * 15);
    }

    private static int randomBetween(int minInclusive, int maxInclusive) {
        return ThreadLocalRandom.current().nextInt(minInclusive, maxInclusive + 1);
    }

    // -------------------------
    // Zoltraak
    // -------------------------
    private static void castZoltraak(ServerWorld sw, LivingEntity caster) {
        double range = 65.0;
        float damage = 20.0f;

        Vec3d cam = caster.getEyePos();
        Vec3d look = caster.getRotationVec(1.0f).normalize();

        Vec3d start = cam
                .add(look.multiply(1.35))
                .add(0.0, -0.35, 0.0);

        Vec3d end = start.add(look.multiply(range));

        HitResult blockHit = caster.getEntityWorld().raycast(new RaycastContext(
                start, end,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                caster
        ));
        Vec3d blockEnd = blockHit.getPos();

        Box box = caster.getBoundingBox().stretch(look.multiply(range)).expand(1.5);
        EntityHitResult entityHit = ProjectileUtil.raycast(
                caster, start, blockEnd, box,
                e -> e instanceof LivingEntity le && le.isAlive() && e != caster,
                range * range
        );

        Vec3d finalEnd = blockEnd;

        if (entityHit != null) {
            Entity hit = entityHit.getEntity();
            finalEnd = entityHit.getPos();

            hit.damage(sw, caster.getEntityWorld().getDamageSources().magic(), damage);

            double shockRadius = 2.6;
            float knockbackStrength = 0.85f;

            sw.spawnParticles(
                    ParticleTypes.EXPLOSION,
                    finalEnd.x, finalEnd.y + 0.2, finalEnd.z,
                    1, 0, 0, 0, 0
            );

            sw.spawnParticles(
                    ParticleTypes.ELECTRIC_SPARK,
                    finalEnd.x, finalEnd.y + 0.2, finalEnd.z,
                    28, 0.35, 0.35, 0.35, 0.15
            );

            sw.spawnParticles(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    finalEnd.x, finalEnd.y + 0.2, finalEnd.z,
                    18, 0.30, 0.30, 0.30, 0.04
            );

            for (LivingEntity e : sw.getEntitiesByClass(
                    LivingEntity.class,
                    new Box(
                            finalEnd.x - shockRadius, finalEnd.y - shockRadius, finalEnd.z - shockRadius,
                            finalEnd.x + shockRadius, finalEnd.y + shockRadius, finalEnd.z + shockRadius
                    ),
                    e -> e.isAlive() && e != caster
            )) {
                Vec3d dir = new Vec3d(e.getX(), e.getY(), e.getZ()).subtract(finalEnd).normalize();
                e.addVelocity(
                        dir.x * knockbackStrength,
                        0.25,
                        dir.z * knockbackStrength
                );
                e.velocityDirty = true;
            }
        }

        double dist = start.distanceTo(finalEnd);
        int steps = Math.max(26, (int) (dist * 7));

        double radius = 0.12;
        int coreParticles = 2;
        int shellParticles = 2;

        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;
            Vec3d center = start.lerp(finalEnd, t);

            for (int j = 0; j < coreParticles; j++) {
                sw.spawnParticles(
                        ParticleTypes.END_ROD,
                        center.x, center.y, center.z,
                        1, 0.0, 0.0, 0.0, 0.0
                );
            }

            for (int j = 0; j < shellParticles; j++) {
                double ox = (sw.random.nextDouble() * 2 - 1) * radius;
                double oy = (sw.random.nextDouble() * 2 - 1) * radius;
                double oz = (sw.random.nextDouble() * 2 - 1) * radius;

                sw.spawnParticles(
                        ParticleTypes.ELECTRIC_SPARK,
                        center.x + ox,
                        center.y + oy,
                        center.z + oz,
                        1, 0.0, 0.0, 0.0, 0.0
                );
            }

            if ((i % 2) == 0) {
                sw.spawnParticles(
                        ParticleTypes.SOUL_FIRE_FLAME,
                        center.x, center.y, center.z,
                        1, 0.05, 0.05, 0.05, 0.0
                );
            }
        }

        sw.spawnParticles(
                ParticleTypes.ELECTRIC_SPARK,
                finalEnd.x, finalEnd.y, finalEnd.z,
                32, 0.22, 0.22, 0.22, 0.14
        );

        caster.getEntityWorld().playSound(null, caster.getBlockPos(),
                SoundEvents.ENTITY_GUARDIAN_ATTACK, SoundCategory.HOSTILE,
                0.9f, 1.6f);

        caster.getEntityWorld().playSound(null, BlockPos.ofFloored(finalEnd),
                SoundEvents.BLOCK_AMETHYST_BLOCK_RESONATE, SoundCategory.HOSTILE,
                0.8f, 1.2f);
    }

    // -------------------------
    // Firebolt
    // -------------------------
    private static void castFirebolt(ServerWorld sw, LivingEntity caster, double speed) {
        Vec3d look = caster.getRotationVec(1.0F).normalize().multiply(speed);

        SmallFireballEntity fireball = new SmallFireballEntity(sw, caster, look);
        fireball.setOwner(caster);
        fireball.setPosition(
                caster.getX(),
                caster.getEyeY() - 0.1,
                caster.getZ()
        );

        sw.spawnEntity(fireball);

        caster.playSound(SoundEvents.ITEM_FIRECHARGE_USE, 1.0F, 1.0F);
    }

    // -------------------------
    // Heal
    // -------------------------
    private static void castHeal(ServerWorld sw, LivingEntity caster) {
        float before = caster.getHealth();
        caster.heal(4.0F);

        caster.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8F, 1.3F);

        sw.spawnParticles(
                ParticleTypes.HAPPY_VILLAGER,
                caster.getX(),
                caster.getBodyY(0.6),
                caster.getZ(),
                10,
                0.3, 0.4, 0.3,
                0.0
        );

        if (caster.getHealth() <= before) {
            // BLOCK_NOTE_BLOCK_BASS is red in your mappings, so use a guaranteed sound:
            caster.playSound(SoundEvents.ENTITY_VILLAGER_NO, 0.5F, 0.9F);
        }
    }
}
