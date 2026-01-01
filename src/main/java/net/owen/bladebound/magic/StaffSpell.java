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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.owen.bladebound.mana.ManaHolder;
import net.owen.bladebound.magic.spells.WorldRewriteSpell;
import net.owen.bladebound.magic.worldrewrite.WorldRewriteZoneManager;

import java.util.List;

public enum StaffSpell {

    FIREBOLT(
            Identifier.of("bladebound", "firebolt"),
            "Firebolt", SpellRarity.STARTER, 20, 10
    ),
    FROST_RAY(
            Identifier.of("bladebound", "frost_ray"),
            "Frost Ray", SpellRarity.STARTER, 25, 15
    ),
    HEAL(
            Identifier.of("bladebound", "heal"),
            "Heal", SpellRarity.STARTER, 30, 25
    ),
    STONE_DART(
            Identifier.of("bladebound", "stone_dart"),
            "Stone Dart", SpellRarity.STARTER, 5, 10
    ),
    LIGHTNING_STRIKE(
            Identifier.of("bladebound", "lightning_strike"),
            "Lightning Strike", SpellRarity.RARE, 45, 35
    ),
    MANA_BARRIER(
            Identifier.of("bladebound", "mana_barrier"),
            "Defensive Magic", SpellRarity.RARE, 0, 0
    ),
    ZOLTRAAK(
            Identifier.of("bladebound", "zoltraak"),
            "Zoltraak", SpellRarity.LEGENDARY, 50, 65
    ),
    PERFECT_HEAL(
            Identifier.of("bladebound", "perfect_heal"),
            "Perfect Heal Spell", SpellRarity.LEGENDARY, 0, 0
    ),
    WORLD_REWRITE(
            Identifier.of("bladebound", "world_rewrite"),
            "World Rewrite", SpellRarity.ANCIENT, 500, 900
    );

    // =========================================================
    // Fields
    // =========================================================
    public final Identifier id;
    public final String displayName;
    public final SpellRarity rarity;
    public final int manaCost;

    /**
     * NOTE: This value is currently treated as SECONDS in your project (e.g., Zoltraak = 65).
     * Keep the name to avoid breaking other files that reference cooldownTicks.
     */
    public final int cooldownTicks;

    private static final java.util.Map<Identifier, StaffSpell> BY_ID = new java.util.HashMap<>();

    static {
        for (StaffSpell s : values()) {
            BY_ID.put(s.id, s);
        }
    }

    public static StaffSpell fromId(Identifier id) {
        StaffSpell s = BY_ID.get(id);
        return s != null ? s : values()[0];
    }


    // =========================================================
    // Constructor
    // =========================================================
    StaffSpell(Identifier id, String displayName, SpellRarity rarity, int manaCost, int cooldownSeconds) {
        this.id = id;
        this.displayName = displayName;
        this.rarity = rarity;
        this.manaCost = manaCost;
        this.cooldownTicks = cooldownSeconds; // yes, seconds (legacy name)
    }

    public enum SpellRarity {
        STARTER,
        RARE,
        LEGENDARY,
        ANCIENT
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

        // Block ALL spell casting inside an active World Rewrite zone (including the caster)
        if (!world.isClient && world instanceof ServerWorld sw) {
            if (WorldRewriteZoneManager.isInsideActiveZone(sw, user.getPos())) {
                user.sendMessage(Text.literal("Reality is frozen. You can't cast right now."), true);
                return 0;
            }
        }

        return switch (this) {
            case FIREBOLT -> { castFirebolt(world, user, fireballSpeed); yield 0; }
            case FROST_RAY -> { castFrostRay(world, user); yield 0; }
            case HEAL -> { castHeal(world, user); yield 0; }
            case STONE_DART -> { castStoneDart(world, user); yield 0; }
            case LIGHTNING_STRIKE -> { castLightning(world, user); yield 0; }
            case MANA_BARRIER -> { castManaBarrier(world, user); yield 0; }
            case ZOLTRAAK -> { castZoltraak(world, user); yield 0; }
            case PERFECT_HEAL -> castPerfectHeal(world, user);
            case WORLD_REWRITE -> castWorldRewrite(world, user);
        };
    }

    // -------------------------
    // Stone Dart
    // -------------------------
    private static int castStoneDart(World world, PlayerEntity user) {
        if (world.isClient) return 0;
        if (!(world instanceof net.minecraft.server.world.ServerWorld sw)) return 0;

        double range = 28.0;
        float damage = 4.0f;

        Vec3d cam = user.getCameraPosVec(1.0f);
        Vec3d look = user.getRotationVec(1.0f).normalize();

        // Build a "right" vector from look (hand side offset)
        Vec3d up = new Vec3d(0, 1, 0);
        Vec3d right = look.crossProduct(up);
        if (right.lengthSquared() < 1.0e-6) {
            // looking almost straight up/down; pick a fallback right vector
            right = new Vec3d(1, 0, 0);
        } else {
            right = right.normalize();
        }

        // If player is left-handed, flip the hand side
        if (user.getMainArm() == Arm.LEFT) {
            right = right.multiply(-1.0);
        }

        // Start from where the staff would be in main hand:
        //  - a bit to the right (hand side)
        //  - slightly down
        //  - slightly forward so it's not in your face
        Vec3d start = cam
                .add(right.multiply(0.28))
                .add(0.0, -0.18, 0.0)
                .add(look.multiply(0.45));

        Vec3d end = start.add(look.multiply(range));

        // Block raycast (dart stops at blocks)
        HitResult blockHit = world.raycast(new RaycastContext(
                start, end,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                user
        ));

        Vec3d blockEnd = (blockHit.getType() == HitResult.Type.MISS) ? end : blockHit.getPos();

        // Entity raycast up to the block stop point
        Box box = user.getBoundingBox().stretch(look.multiply(range)).expand(1.0);
        EntityHitResult entityHit = ProjectileUtil.raycast(
                user,
                start,
                blockEnd,
                box,
                e -> !e.isSpectator() && e.isAlive() && e.canHit() && e != user,
                range * range
        );

        Vec3d to = (entityHit != null) ? entityHit.getPos() : blockEnd;

        // Visible dart streak (server -> clients)
        Vec3d dir = to.subtract(start);
        double len = dir.length();
        if (len > 1.0e-3) {
            Vec3d step = dir.normalize().multiply(0.30);
            Vec3d p = start;

            int steps = (int) Math.min(90, (len / 0.30));
            for (int i = 0; i < steps; i++) {
                sw.spawnParticles(
                        net.minecraft.particle.ParticleTypes.CRIT, // swap particle if you want
                        p.x, p.y, p.z,
                        1,
                        0.0, 0.0, 0.0,
                        0.0
                );
                p = p.add(step);
            }
        }

        // Apply damage on hit
        if (entityHit != null) {
            Entity target = entityHit.getEntity();
            target.damage(world.getDamageSources().magic(), damage);
        }

        return 0; // use your default cooldown from SpellCooldowns
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
    // Defensive Magic
    // -------------------------
    private static void castManaBarrier(World world, PlayerEntity user) {
        // Intentionally empty.
        // Actual barrier behavior happens while holding right click in FrierenStaffItem.usageTick().
    }

    // -------------------------
    // Zoltraak
    // -------------------------
    private static void castZoltraak(World world, PlayerEntity user) {
        if (world.isClient) return;
        if (!(world instanceof ServerWorld sw)) return;

        double range = 65.0;
        float damage = 20.0f;

        Vec3d cam = user.getCameraPosVec(1.0f);
        Vec3d look = user.getRotationVec(1.0f).normalize();

        Vec3d right = look.crossProduct(new Vec3d(0.0, 1.0, 0.0)).normalize();
        Vec3d up = right.crossProduct(look).normalize();

        // If player is left-handed, flip the hand side
        if (user.getMainArm() == Arm.LEFT) {
            right = right.multiply(-1.0);
        }

        Vec3d start = cam
                .add(look.multiply(1.35))
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

        Box box = user.getBoundingBox().stretch(look.multiply(range)).expand(1.5);
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
                    e -> e.isAlive() && e != user
            )) {
                Vec3d dir = e.getPos().subtract(finalEnd).normalize();
                e.addVelocity(
                        dir.x * knockbackStrength,
                        0.25,
                        dir.z * knockbackStrength
                );
                e.velocityModified = true;
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
            return 0;
        }

        mana.bladebound$setMana(0);
        user.setHealth(user.getMaxHealth());

        for (StatusEffectInstance inst : List.copyOf(user.getStatusEffects())) {
            if (inst.getEffectType().value().getCategory() == StatusEffectCategory.HARMFUL) {
                user.removeStatusEffect(inst.getEffectType());
            }
        }

        double percentUsed = currentMana / (double) Math.max(1, maxMana);

        int cooldownSeconds;
        if (percentUsed <= 0.50) cooldownSeconds = 115;
        else if (percentUsed <= 0.75) cooldownSeconds = 130;
        else if (percentUsed <= 0.90) cooldownSeconds = 145;
        else cooldownSeconds = 160;

        return cooldownSeconds;
    }

    // -------------------------
    // World Rewrite (Ancient) — TEST VERSION (no mana/cooldown yet)
    // -------------------------
    private static int castWorldRewrite(World world, PlayerEntity user) {
        if (world.isClient) return 0;
        if (!(world instanceof ServerWorld sw)) return 0;
        if (!(user instanceof ServerPlayerEntity sp)) return 0;

        WorldRewriteSpell.cast(sw, sp);
        return 0;
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
