package net.owen.bladebound.magic;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.owen.bladebound.mana.ManaHolder;

import java.util.List;

public enum StaffSpell {

    FIREBOLT("Firebolt", SpellRarity.STARTER, 20, 10),
    FROST_RAY("Frost Ray", SpellRarity.STARTER, 25, 15),
    HEAL("Heal", SpellRarity.STARTER, 30, 25),

    LIGHTNING_STRIKE("Lightning Strike", SpellRarity.RARE, 45, 35),

    ZOLTRAAK("Zoltraak", SpellRarity.LEGENDARY, 50, 65),

    PERFECT_HEAL("Perfect Heal Spell", SpellRarity.LEGENDARY, 0, 0);

    public final String displayName;
    public final SpellRarity rarity;
    public final int manaCost;

    /**
     * NOTE: This value is currently treated as SECONDS in your project (e.g., Zoltraak = 100).
     * Keep the name to avoid breaking other files that reference cooldownTicks.
     */
    public final int cooldownTicks;

    StaffSpell(String displayName, SpellRarity rarity, int manaCost, int cooldownSeconds) {
        this.displayName = displayName;
        this.rarity = rarity;
        this.manaCost = manaCost;
        this.cooldownTicks = cooldownSeconds; // yes, seconds (legacy name)
    }

    public enum SpellRarity {
        STARTER,
        RARE,
        LEGENDARY
    }

    // Dynamic + safe mapping: index aligns with enum order
    public static StaffSpell fromIndex(int idx) {
        StaffSpell[] v = values();
        if (idx < 0 || idx >= v.length) return v[0];
        return v[idx];
    }

    /**
     * Cast the spell.
     * @return cooldown override in SECONDS; return <= 0 to use the default cooldownTicks (seconds).
     */
    public int cast(World world, PlayerEntity user, double fireballSpeed) {
        return switch (this) {
            case FIREBOLT -> { castFirebolt(world, user, fireballSpeed); yield 0; }
            case FROST_RAY -> { castFrostRay(world, user); yield 0; }
            case HEAL -> { castHeal(world, user); yield 0; }
            case LIGHTNING_STRIKE -> { castLightning(world, user); yield 0; }
            case ZOLTRAAK -> { castZoltraak(world, user); yield 0; }
            case PERFECT_HEAL -> castPerfectHeal(world, user);
        };
    }

    // -------------------------
    // Lightning
    // -------------------------

    private static void castLightning(World world, PlayerEntity user) {
        double range = 32.0;

        Vec3d start = user.getCameraPosVec(1.0F);
        Vec3d look = user.getRotationVec(1.0F).normalize();
        Vec3d end = start.add(look.multiply(range));

        Box box = user.getBoundingBox().stretch(look.multiply(range)).expand(1.0);
        EntityHitResult entityHit = ProjectileUtil.raycast(
                user,
                start,
                end,
                box,
                (e) -> !e.isSpectator() && e.isAlive() && e.canHit() && e != user,
                range * range
        );

        if (entityHit != null) {
            Entity target = entityHit.getEntity();
            strike(world, target.getBlockPos());
            target.damage(world.getDamageSources().lightningBolt(), 6.0F);
            return;
        }

        HitResult blockHit = world.raycast(new RaycastContext(
                start,
                end,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                user
        ));

        if (blockHit.getType() != HitResult.Type.MISS) {
            strike(world, BlockPos.ofFloored(blockHit.getPos()));
        }
    }

    private static void strike(World world, BlockPos pos) {
        if (world.isClient) return;

        var bolt = net.minecraft.entity.EntityType.LIGHTNING_BOLT.create(world);
        if (bolt == null) return;

        bolt.refreshPositionAfterTeleport(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        world.spawnEntity(bolt);
    }

    // -------------------------
    // Zoltraak
    // -------------------------

    private static void castZoltraak(World world, PlayerEntity user) {
        if (world.isClient) return;
        if (!(world instanceof ServerWorld sw)) return;

        double range = 48.0;
        float damage = 9.0f;

        Vec3d cam = user.getCameraPosVec(1.0f);
        Vec3d look = user.getRotationVec(1.0f).normalize();

        Vec3d right = look.crossProduct(new Vec3d(0.0, 1.0, 0.0)).normalize();

        Vec3d start = cam
                .add(look.multiply(0.45))
                .add(0.0, -0.35, 0.0)
                .add(right.multiply(0.28));

        Vec3d end = start.add(look.multiply(range));

        HitResult blockHit = world.raycast(new RaycastContext(
                start, end,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                user
        ));
        Vec3d blockEnd = blockHit.getPos();

        Box box = user.getBoundingBox().stretch(look.multiply(range)).expand(1.25);
        EntityHitResult entityHit = ProjectileUtil.raycast(
                user, start, blockEnd, box,
                e -> e instanceof LivingEntity && e.isAlive() && e != user,
                range * range
        );

        Vec3d finalEnd = blockEnd;

        if (entityHit != null) {
            Entity hit = entityHit.getEntity();
            finalEnd = entityHit.getPos();

            hit.damage(world.getDamageSources().magic(), damage);

            sw.spawnParticles(ParticleTypes.CRIT, finalEnd.x, finalEnd.y, finalEnd.z,
                    12, 0.12, 0.12, 0.12, 0.05);
        }

        double dist = start.distanceTo(finalEnd);
        int steps = Math.max(18, (int) (dist * 6));

        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;
            Vec3d p = start.lerp(finalEnd, t);

            sw.spawnParticles(ParticleTypes.END_ROD, p.x, p.y, p.z,
                    1, 0.0, 0.0, 0.0, 0.0);

            if ((i & 1) == 0) {
                sw.spawnParticles(ParticleTypes.ELECTRIC_SPARK, p.x, p.y, p.z,
                        1, 0.06, 0.06, 0.06, 0.0);
            }
            if ((i % 3) == 0) {
                sw.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, p.x, p.y, p.z,
                        1, 0.04, 0.04, 0.04, 0.0);
            }
        }

        sw.spawnParticles(ParticleTypes.ELECTRIC_SPARK, finalEnd.x, finalEnd.y, finalEnd.z,
                24, 0.18, 0.18, 0.18, 0.12);

        world.playSound(null, user.getBlockPos(),
                SoundEvents.ENTITY_GUARDIAN_ATTACK, SoundCategory.PLAYERS,
                0.9f, 1.6f);

        world.playSound(null, BlockPos.ofFloored(finalEnd),
                SoundEvents.BLOCK_AMETHYST_BLOCK_RESONATE, SoundCategory.PLAYERS,
                0.8f, 1.2f);
    }

    // -------------------------
    // Perfect Heal
    // -------------------------

    private static int castPerfectHeal(World world, PlayerEntity user) {
        if (world.isClient) return 0;
        if (!(user instanceof ManaHolder mana)) return 0;

        int currentMana = mana.bladebound$getMana();
        int maxMana = mana.bladebound$getMaxMana();

        if (currentMana <= 0) {
            // No mana → no cast
            return 0;
        }

        // --- Consume ALL mana ---
        mana.bladebound$setMana(0);

        // --- Full heal ---
        user.setHealth(user.getMaxHealth());

        // --- Remove negative effects ---
        for (StatusEffectInstance inst : List.copyOf(user.getStatusEffects())) {
            if (inst.getEffectType().value().getCategory() == StatusEffectCategory.HARMFUL) {
                user.removeStatusEffect(inst.getEffectType());
            }
        }

        // --- Cooldown scaling ---
        double percentUsed = currentMana / (double) Math.max(1, maxMana);

        int cooldownSeconds;
        if (percentUsed <= 0.50) cooldownSeconds = 115;
        else if (percentUsed <= 0.75) cooldownSeconds = 130;
        else if (percentUsed <= 0.90) cooldownSeconds = 145;
        else cooldownSeconds = 160;

        return cooldownSeconds;
    }


    // -------------------------
    // Sound helpers
    // -------------------------

    private static SoundEvent sound(String vanillaId) {
        return Registries.SOUND_EVENT.get(Identifier.of("minecraft", vanillaId));
    }

    private static final SoundEvent SND_FIRECHARGE = sound("item.firecharge.use");
    private static final SoundEvent SND_CHIME = sound("block.amethyst_block.chime");
    private static final SoundEvent SND_GLASS_BREAK = sound("block.glass.break");
    private static final SoundEvent SND_XP = sound("entity.experience_orb.pickup");
    private static final SoundEvent SND_BASS = sound("block.note_block.bass");

    // -------------------------
    // Spell implementations
    // -------------------------

    private static void castFirebolt(World world, PlayerEntity user, double speed) {
        Vec3d look = user.getRotationVec(1.0F).normalize().multiply(speed);

        SmallFireballEntity fireball = new SmallFireballEntity(
                world,
                user.getX(),
                user.getEyeY() - 0.1,
                user.getZ(),
                look
        );
        fireball.setOwner(user);
        world.spawnEntity(fireball);

        user.playSound(SND_FIRECHARGE, 1.0F, 1.0F);
    }

    private static void castFrostRay(World world, PlayerEntity user) {
        double range = 20.0;

        Vec3d start = user.getCameraPosVec(1.0F);
        Vec3d look = user.getRotationVec(1.0F).normalize();
        Vec3d end = start.add(look.multiply(range));

        List<LivingEntity> candidates = world.getEntitiesByClass(
                LivingEntity.class,
                user.getBoundingBox().stretch(look.multiply(range)).expand(1.5),
                e -> e.isAlive() && e != user && user.canSee(e)
        );

        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;

        for (LivingEntity e : candidates) {
            var hit = e.getBoundingBox().raycast(start, end);
            if (hit.isPresent()) {
                double d = start.squaredDistanceTo(hit.get());
                if (d < bestDist) {
                    bestDist = d;
                    best = e;
                }
            }
        }

        user.playSound(SND_CHIME, 0.8F, 1.4F);

        if (best != null) {
            best.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 80, 1));
            user.playSound(SND_GLASS_BREAK, 0.6F, 1.2F);

            if (world instanceof ServerWorld sw) {
                sw.spawnParticles(
                        ParticleTypes.SNOWFLAKE,
                        best.getX(),
                        best.getBodyY(0.6),
                        best.getZ(),
                        12,
                        0.2, 0.2, 0.2,
                        0.02
                );
            }
        } else if (world instanceof ServerWorld sw) {
            Vec3d p = start.add(look.multiply(3.0));
            sw.spawnParticles(
                    ParticleTypes.SNOWFLAKE,
                    p.x, p.y, p.z,
                    8,
                    0.15, 0.15, 0.15,
                    0.01
            );
        }
    }

    private static void castHeal(World world, PlayerEntity user) {
        float before = user.getHealth();
        user.heal(4.0F);

        user.playSound(SND_XP, 0.8F, 1.3F);

        if (world instanceof ServerWorld sw) {
            sw.spawnParticles(
                    ParticleTypes.HAPPY_VILLAGER,
                    user.getX(),
                    user.getBodyY(0.6),
                    user.getZ(),
                    10,
                    0.3, 0.4, 0.3,
                    0.0
            );
        }

        if (user.getHealth() <= before) {
            user.playSound(SND_BASS, 0.4F, 0.7F);
        }
    }
}
