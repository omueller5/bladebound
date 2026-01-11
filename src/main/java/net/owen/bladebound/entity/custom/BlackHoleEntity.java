package net.owen.bladebound.entity.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.UUID;

public class BlackHoleEntity extends Entity {

    private Vec3d center = Vec3d.ZERO;

    // =========================================================
    // 3-ZONE RADII (EDIT HERE)
    // =========================================================
    private float coreRadius = 6.0f;
    private float midRadius  = 10.0f;
    private float pullRadius = 14.0f;

    // =========================================================
    // DURATION (EDIT HERE)
    // =========================================================
    private int durationTicks = 20 * 12;

    // =========================================================
    // DAMAGE (EDIT HERE)
    // =========================================================
    private float coreDamagePerSecond = 80.0f;
    private float midDamagePerSecond  = 10.0f;

    // Base inward pull (radial)
    private double pullStrength = 0.12;

    // Spiral settings
    private double swirlStrength = 0.18;
    private boolean swirlClockwise = true;

    // =========================================================
    // EVENT HORIZON (EDIT HERE)
    // =========================================================
    private boolean eventHorizonEnabled = true;
    private int eventHorizonDurationTicks = 25;
    private int slownessAmplifier = 3;
    private int weaknessAmplifier = 1;

    // =========================================================
    // SINGULARITY COLLAPSE (EDIT HERE)
    // =========================================================
    private boolean collapseEnabled = true;
    private float collapseDamage = 10.0f;
    private float collapseRadius = 6.0f;
    private boolean collapseHitCaster = false;
    private boolean collapseDone = false;

    // =========================================================
    // FALLING-BLOCK RIP SETTINGS (EDIT HERE)
    // =========================================================
    private boolean ripBlocksAsFalling = true;
    private int blocksPerRip = 5;
    private int ripIntervalTicks = 2;
    private int ripCooldown = 0;
    private double fallingBlockKick = 0.45;

    // =========================================================
    // PERFORMANCE BUDGETS (EDIT HERE)
    // =========================================================
    private int maxTargetsPerTick = 90;
    private int maxFallingBlocksNearby = 140;

    // IMPORTANT: core cleanup
    private boolean deleteDebrisInCore = true;
    private boolean deleteItemsInCore = true;

    // =========================================================
    // PROJECTILES (NEW)
    // =========================================================
    // Destroy most projectiles in the CORE.
    // Tridents are NOT destroyed; they are dropped as an item with enchants intact.
    private boolean destroyProjectilesInCore = true;

    private UUID ownerUuid;

    public BlackHoleEntity(EntityType<?> type, World world) {
        super(type, world);
        this.noClip = true;
        this.setNoGravity(true);
        this.ignoreCameraFrustum = true;
    }

    public void setOwner(net.minecraft.entity.player.PlayerEntity owner) {
        this.ownerUuid = owner == null ? null : owner.getUuid();
    }

    public void setCenter(Vec3d center) {
        this.center = center;
    }

    // --- setters you already use ---
    public void setDurationTicks(int ticks) { this.durationTicks = ticks; }
    public void setPullStrength(double pullStrength) { this.pullStrength = pullStrength; }

    public void setCoreRadius(float r) {
        this.coreRadius = Math.max(0.5f, r);
        if (midRadius < coreRadius) midRadius = coreRadius;
        if (pullRadius < midRadius) pullRadius = midRadius;
    }
    public void setMidRadius(float r) {
        this.midRadius = Math.max(r, coreRadius);
        if (pullRadius < midRadius) pullRadius = midRadius;
    }
    public void setPullRadius(float r) {
        this.pullRadius = Math.max(r, midRadius);
    }

    public void setCoreDamagePerSecond(float dps) { this.coreDamagePerSecond = Math.max(0.0f, dps); }
    public void setMidDamagePerSecond(float dps) { this.midDamagePerSecond = Math.max(0.0f, dps); }

    public void setSwirlStrength(double swirlStrength) { this.swirlStrength = MathHelper.clamp(swirlStrength, 0.0, 5.0); }
    public void setSwirlClockwise(boolean clockwise) { this.swirlClockwise = clockwise; }

    public void setRipBlocksAsFalling(boolean b) { this.ripBlocksAsFalling = b; }
    public void setBlocksPerRip(int n) { this.blocksPerRip = MathHelper.clamp(n, 0, 64); }
    public void setRipIntervalTicks(int ticks) { this.ripIntervalTicks = Math.max(1, ticks); }
    public void setFallingBlockKick(double kick) { this.fallingBlockKick = MathHelper.clamp(kick, 0.0, 3.0); }

    public void setMaxTargetsPerTick(int n) { this.maxTargetsPerTick = MathHelper.clamp(n, 10, 1000); }
    public void setMaxFallingBlocksNearby(int n) { this.maxFallingBlocksNearby = MathHelper.clamp(n, 0, 5000); }
    public void setDeleteDebrisInCore(boolean b) { this.deleteDebrisInCore = b; }
    public void setDeleteItemsInCore(boolean b) { this.deleteItemsInCore = b; }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        // no tracked data needed
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getWorld().isClient) return;

        if (!(this.getWorld() instanceof ServerWorld sw)) {
            this.discard();
            return;
        }

        if (--durationTicks <= 0) {
            doSingularityCollapse(sw);
            this.discard();
            return;
        }

        this.setPos(center.x, center.y, center.z);

        spawnDebug(sw);
        applySpiralPullDamageAndHorizon(sw);

        if (ripBlocksAsFalling) {
            tickBlockRippingAsFalling(sw);
        }
    }

    private void doSingularityCollapse(ServerWorld sw) {
        if (!collapseEnabled) return;
        if (collapseDone) return;
        collapseDone = true;

        float r = Math.max(0.5f, collapseRadius);

        Box box = new Box(
                center.x - r, center.y - r, center.z - r,
                center.x + r, center.y + r, center.z + r
        );

        List<Entity> targets = sw.getOtherEntities(this, box, e ->
                e.isAlive() && !e.isSpectator()
        );

        for (Entity e : targets) {
            if (e instanceof LivingEntity living) {
                if (!collapseHitCaster && ownerUuid != null && ownerUuid.equals(living.getUuid())) continue;

                double dist = living.getPos().distanceTo(center);
                if (dist > r) continue;

                living.damage(sw.getDamageSources().magic(), collapseDamage);
            }
        }

        sw.spawnParticles(ParticleTypes.SMOKE, center.x, center.y, center.z, 40, 0.35, 0.25, 0.35, 0.02);
        sw.spawnParticles(ParticleTypes.END_ROD, center.x, center.y, center.z, 18, 0.25, 0.15, 0.25, 0.02);
        sw.spawnParticles(ParticleTypes.EXPLOSION, center.x, center.y, center.z, 2, 0.0, 0.0, 0.0, 0.0);
    }

    private void applySpiralPullDamageAndHorizon(ServerWorld sw) {
        float scanR = pullRadius;

        Box box = new Box(
                center.x - scanR, center.y - scanR, center.z - scanR,
                center.x + scanR, center.y + scanR, center.z + scanR
        );

        List<Entity> targets = sw.getOtherEntities(this, box, e ->
                e.isAlive()
                        && !e.isSpectator()
                        && !(e instanceof BlackHoleEntity)
        );

        Vec3d axis = new Vec3d(0, 1, 0);
        int processed = 0;

        for (Entity e : targets) {
            if (processed >= maxTargetsPerTick) break;

            Vec3d toCenter = center.subtract(e.getPos());
            double dist = toCenter.length();
            if (dist < 0.001) continue;
            if (dist > pullRadius) continue;

            // =========================
            // CORE CLEANUP (PERF)
            // =========================
            if (dist <= coreRadius * 0.60) {
                if (deleteDebrisInCore && e instanceof FallingBlockEntity) {
                    e.discard();
                    continue;
                }
                if (deleteItemsInCore && e instanceof ItemEntity item) {
                    // DO NOT delete trident drops (keeps enchants/name fair)
                    if (!isProtectedCoreItem(item)) {
                        e.discard();
                        continue;
                    }
                }
            }

            Vec3d radialDir = toCenter.multiply(1.0 / dist);

            // Pull factor based on outer radius
            double tPull = 1.0 - (dist / pullRadius);
            double radial = pullStrength * (0.35 + 1.65 * tPull);

            if (dist <= midRadius) radial *= 1.15;
            if (dist <= coreRadius) radial *= 1.35;

            // Tangential orbit
            Vec3d tangent = axis.crossProduct(radialDir);
            if (tangent.lengthSquared() < 1.0e-6) {
                tangent = new Vec3d(1, 0, 0).crossProduct(radialDir);
            }
            tangent = tangent.normalize();
            if (!swirlClockwise) tangent = tangent.multiply(-1.0);

            double swirl = swirlStrength * (0.20 + 1.80 * tPull);

            if (e instanceof ItemEntity || e instanceof FallingBlockEntity) {
                swirl *= 1.25;
                radial *= 1.15;
            }

            Vec3d dv = radialDir.multiply(radial).add(tangent.multiply(swirl));
            dv = new Vec3d(dv.x, dv.y * 0.55, dv.z);

            e.addVelocity(dv.x, dv.y, dv.z);
            e.velocityModified = true;

            // =========================
            // PROJECTILES: pull all
            // destroy most in CORE
            // but TRIDENTS drop as items
            // =========================
            if (destroyProjectilesInCore && dist <= coreRadius && e instanceof ProjectileEntity proj) {
                if (proj instanceof TridentEntity trident) {
                    // Convert to a dropped stack (keeps name + enchants)
                    dropTridentStack(sw, trident);

                    // Delete the trident projectile entity so it doesn't keep flying around
                    trident.discard();

                    processed++;
                    continue;
                } else {
                    // Arrows, potions, fireballs, etc: deleted in the core
                    proj.discard();
                    processed++;
                    continue;
                }
            }

            // =========================
            // DAMAGE TIERS
            // =========================
            if (e instanceof LivingEntity living) {
                if (dist <= coreRadius) {
                    applyDamage(sw, living, dist, coreRadius, coreDamagePerSecond);
                    if (eventHorizonEnabled) {
                        applyEventHorizon(living);
                    }
                } else if (dist <= midRadius) {
                    applyDamage(sw, living, dist, midRadius, midDamagePerSecond);
                }
            }

            processed++;
        }
    }

    private boolean isProtectedCoreItem(ItemEntity item) {
        ItemStack s = item.getStack();
        return !s.isEmpty() && s.isOf(Items.TRIDENT);
    }

    private void dropTridentStack(ServerWorld sw, TridentEntity trident) {
        ItemStack stack;

        // Yarn typically exposes this as public on TridentEntity (not via PersistentProjectileEntity),
        // which avoids the "protected access" error.
        stack = trident.getItemStack().copy();

        if (stack.isEmpty()) {
            stack = new ItemStack(Items.TRIDENT);
        }

        ItemEntity drop = new ItemEntity(sw, trident.getX(), trident.getY(), trident.getZ(), stack);
        drop.setPickupDelay(20);
        sw.spawnEntity(drop);
    }

    private void applyDamage(ServerWorld sw, LivingEntity living, double dist, double radius, float dps) {
        if (dps <= 0.0f) return;

        double t = 1.0 - (dist / Math.max(0.001, radius));
        float perTick = dps / 20.0f;

        float scaled = perTick * (0.25f + 1.75f * (float) t);
        scaled = MathHelper.clamp(scaled, 0.05f, 200.0f);

        DamageSource src = sw.getDamageSources().magic();
        living.damage(src, scaled);
    }

    private void applyEventHorizon(LivingEntity living) {
        living.addStatusEffect(new StatusEffectInstance(
                StatusEffects.SLOWNESS,
                eventHorizonDurationTicks,
                slownessAmplifier,
                true,
                true
        ));

        living.addStatusEffect(new StatusEffectInstance(
                StatusEffects.WEAKNESS,
                eventHorizonDurationTicks,
                weaknessAmplifier,
                true,
                true
        ));
    }

    // =========================================================
    // RIP SAFETY FILTERS
    // =========================================================
    private boolean isRippableBlock(ServerWorld sw, BlockPos pos, BlockState state) {
        if (state.isAir()) return false;
        if (!state.getFluidState().isEmpty()) return false;
        if (state.hasBlockEntity()) return false;

        Block b = state.getBlock();
        if (b == Blocks.BEDROCK
                || b == Blocks.END_PORTAL_FRAME
                || b == Blocks.END_PORTAL
                || b == Blocks.NETHER_PORTAL
                || b == Blocks.REINFORCED_DEEPSLATE
                || b == Blocks.SPAWNER) {
            return false;
        }

        if (b == Blocks.LEVER
                || b == Blocks.STONE_BUTTON || b == Blocks.OAK_BUTTON
                || b == Blocks.IRON_DOOR || b == Blocks.OAK_DOOR
                || b == Blocks.IRON_TRAPDOOR || b == Blocks.OAK_TRAPDOOR
                || b == Blocks.REDSTONE_WIRE
                || b == Blocks.REPEATER
                || b == Blocks.COMPARATOR) {
            return false;
        }

        if (state.isIn(BlockTags.LEAVES) || state.isIn(BlockTags.LOGS)) return true;

        if (b == Blocks.STONE
                || b == Blocks.COBBLESTONE
                || b == Blocks.DEEPSLATE
                || b == Blocks.COBBLED_DEEPSLATE
                || b == Blocks.DIRT
                || b == Blocks.GRASS_BLOCK
                || b == Blocks.COARSE_DIRT
                || b == Blocks.ROOTED_DIRT
                || b == Blocks.MUD
                || b == Blocks.CLAY
                || b == Blocks.SAND
                || b == Blocks.RED_SAND
                || b == Blocks.GRAVEL
                || b == Blocks.NETHERRACK
                || b == Blocks.BASALT
                || b == Blocks.BLACKSTONE
                || b == Blocks.SOUL_SAND
                || b == Blocks.SOUL_SOIL
                || b == Blocks.END_STONE
                || b == Blocks.TUFF
                || b == Blocks.CALCITE) {
            return true;
        }

        return false;
    }

    private void tickBlockRippingAsFalling(ServerWorld sw) {
        if (ripCooldown > 0) {
            ripCooldown--;
            return;
        }
        ripCooldown = ripIntervalTicks;

        if (maxFallingBlocksNearby > 0) {
            Box countBox = new Box(
                    center.x - pullRadius, center.y - pullRadius, center.z - pullRadius,
                    center.x + pullRadius, center.y + pullRadius, center.z + pullRadius
            );

            int fallingCount = sw.getOtherEntities(this, countBox, e -> e instanceof FallingBlockEntity).size();
            if (fallingCount >= maxFallingBlocksNearby) {
                return;
            }
        }

        int minX = MathHelper.floor(center.x - pullRadius);
        int maxX = MathHelper.floor(center.x + pullRadius);
        int minY = MathHelper.floor(center.y - pullRadius);
        int maxY = MathHelper.floor(center.y + pullRadius);
        int minZ = MathHelper.floor(center.z - pullRadius);
        int maxZ = MathHelper.floor(center.z + pullRadius);

        int ripped = 0;
        int attempts = blocksPerRip * 12;

        for (int i = 0; i < attempts && ripped < blocksPerRip; i++) {
            int x = MathHelper.nextInt(sw.random, minX, maxX);
            int y = MathHelper.nextInt(sw.random, minY, maxY);
            int z = MathHelper.nextInt(sw.random, minZ, maxZ);

            BlockPos pos = new BlockPos(x, y, z);

            Vec3d p = Vec3d.ofCenter(pos);
            double dist = p.distanceTo(center);

            if (dist > pullRadius) continue;
            if (dist <= midRadius) continue;

            BlockState state = sw.getBlockState(pos);
            if (state.getHardness(sw, pos) < 0) continue;
            if (!isRippableBlock(sw, pos, state)) continue;

            if (spawnRippedFallingBlock(sw, pos, state)) {
                ripped++;
            }
        }
    }

    private boolean spawnRippedFallingBlock(ServerWorld sw, BlockPos pos, BlockState state) {
        FallingBlockEntity fb = FallingBlockEntity.spawnFromBlock(sw, pos, state);
        if (fb == null) return false;

        fb.setNoGravity(true);

        Vec3d from = new Vec3d(fb.getX(), fb.getY(), fb.getZ());
        Vec3d toCenter = center.subtract(from);
        double dist = Math.max(0.001, toCenter.length());
        Vec3d dir = toCenter.multiply(1.0 / dist);

        fb.setVelocity(
                dir.x * fallingBlockKick,
                dir.y * fallingBlockKick * 0.35,
                dir.z * fallingBlockKick
        );
        fb.velocityModified = true;

        return true;
    }

    private void spawnDebug(ServerWorld sw) {
        spawnRing(sw, pullRadius, ParticleTypes.ASH, ParticleTypes.END_ROD, 36, 0.15);
        spawnRing(sw, midRadius, ParticleTypes.SMOKE, ParticleTypes.END_ROD, 30, 0.10);
        spawnRing(sw, coreRadius, ParticleTypes.SMOKE, ParticleTypes.END_ROD, 24, 0.05);

        sw.spawnParticles(ParticleTypes.SMOKE, center.x, center.y, center.z, 2, 0.03, 0.03, 0.03, 0.0);
        sw.spawnParticles(ParticleTypes.END_ROD, center.x, center.y + 0.08, center.z, 1, 0.01, 0.01, 0.01, 0.0);
    }

    private void spawnRing(ServerWorld sw, double r,
                           ParticleEffect dark,
                           ParticleEffect light,
                           int points,
                           double yOffset) {
        double y = center.y + yOffset;

        for (int i = 0; i < points; i++) {
            double a = (Math.PI * 2.0) * (i / (double) points);
            double x = center.x + Math.cos(a) * r;
            double z = center.z + Math.sin(a) * r;

            sw.spawnParticles(dark, x, y, z, 1, 0.02, 0.02, 0.02, 0.0);
            sw.spawnParticles(light, x, y + 0.05, z, 1, 0.02, 0.02, 0.02, 0.0);
        }
    }

    @Override
    protected void readCustomDataFromNbt(net.minecraft.nbt.NbtCompound nbt) {}

    @Override
    protected void writeCustomDataToNbt(net.minecraft.nbt.NbtCompound nbt) {}
}
