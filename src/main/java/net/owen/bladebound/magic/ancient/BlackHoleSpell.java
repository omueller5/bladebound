package net.owen.bladebound.magic.ancient;

import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.owen.bladebound.entity.ModEntities;
import net.owen.bladebound.entity.custom.BlackHoleEntity;
import net.owen.bladebound.mana.ManaHolder;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class BlackHoleSpell {

    public static final Identifier ID = Identifier.of("bladebound", "black_hole_spell");

    // =========================================================
    // Tunables
    // =========================================================

    // Duration in ticks (20 ticks = 1 second)
    // 30s = 600
    public static final int DURATION_TICKS = 20 * 30;

    public static final float CORE_DPS = 80.0f;
    public static final float MID_DPS  = 10.0f;

    public static final float CORE_RADIUS = 6.0f;
    public static final float MID_RADIUS  = 10.0f;
    public static final float PULL_RADIUS = 14.0f;

    // Pull baseline (entity also has swirl etc.)
    public static final double PULL_STRENGTH = 0.12;

    // =========================================================
    // PROJECTILE-LIKE TRAVEL (EDIT HERE)
    //
    // Travel speed is blocks per tick.
    // 0.9 feels like a fast spell projectile.
    //
    // Max travel distance ~= TRAVEL_SPEED * TRAVEL_TICKS
    // Example: 0.9 * 22 ~= 19.8 blocks
    // =========================================================
    private static final double TRAVEL_SPEED = 0.9;
    private static final int TRAVEL_TICKS = 22;

    // Safety: don't let it arm too close even if you look at a wall
    private static final double MIN_ARM_DISTANCE = 6.0;

    public static final int CAST_RANGE = 48; // can be larger now since we travel

    // Trail tuning
    private static final double TRAIL_STEP = 0.35;

    // Cost + cooldown
    public static final int MANA_COST = 350;

    // Keep at 0 for testing.
    // 20 minutes = 24000 ticks
    public static final int COOLDOWN_TICKS = 24000; // 240000 for 20m

    private static final Identifier CREATIVE_STAFF_ID = Identifier.of("bladebound", "creative_staff");
    private static final Map<UUID, Long> NEXT_ALLOWED_TICK = new HashMap<>();

    private BlackHoleSpell() {}

    public static boolean cast(ServerWorld world, ServerPlayerEntity caster) {
        boolean exempt = isHoldingCreativeStaff(caster.getMainHandStack());
        return cast(world, caster, exempt);
    }

    public static boolean cast(ServerWorld world, ServerPlayerEntity caster, boolean exemptFromCosts) {
        long now = world.getTime();

        // Cooldown check (skip if exempt)
        if (!exemptFromCosts) {
            long next = NEXT_ALLOWED_TICK.getOrDefault(caster.getUuid(), 0L);
            if (now < next) {
                long remainingTicks = next - now;
                int seconds = (int) Math.ceil(remainingTicks / 20.0);

                int mm = seconds / 60;
                int ss = seconds % 60;
                String s = mm > 0 ? (mm + "m " + ss + "s") : (ss + "s");

                caster.sendMessage(Text.literal("Black Hole on cooldown: " + s), true);
                return false;
            }
        }

        // Mana check + consume (skip if exempt)
        if (!exemptFromCosts) {
            if (!(caster instanceof ManaHolder mana)) return false;

            int currentMana = mana.bladebound$getMana();
            if (currentMana < MANA_COST) {
                caster.sendMessage(Text.literal("Not enough mana (" + currentMana + "/" + MANA_COST + ")."), true);
                return false;
            }

            mana.bladebound$setMana(currentMana - MANA_COST);
        }

        // =========================================================
        // Travel simulation: step forward like a projectile,
        // stop when we hit a solid block, then arm the black hole.
        // =========================================================
        Vec3d eye = caster.getEyePos();
        Vec3d look = caster.getRotationVec(1.0f).normalize();

        Vec3d pos = eye;
        Vec3d lastFree = eye;

        for (int i = 0; i < TRAVEL_TICKS; i++) {
            pos = pos.add(look.multiply(TRAVEL_SPEED));

            // If the traveled point is inside a solid block, stop BEFORE the block
            BlockPos bp = BlockPos.ofFloored(pos);
            if (world.getBlockState(bp).isSolidBlock(world, bp)) {
                break;
            }

            lastFree = pos;

            // Light travel trail while it flies
            spawnTrailStep(world, lastFree);
        }

        // Ensure it arms at a reasonable distance from the caster
        double distFromCaster = lastFree.distanceTo(eye);
        if (distFromCaster < MIN_ARM_DISTANCE) {
            lastFree = eye.add(look.multiply(MIN_ARM_DISTANCE));
        }

        // Also keep the old "must have target in range" vibe:
        // If you're aiming into the sky forever, we still allow it (because it's now a projectile spell),
        // but you can re-enable hard blocking if you want.
        HitResult skyCheck = caster.raycast(CAST_RANGE, 0.0f, false);
        if (skyCheck.getType() == HitResult.Type.MISS) {
            // Optional: comment this block out if you WANT sky casting
            // caster.sendMessage(Text.literal("No target in range."), true);
            // return false;
        }

        // Spawn entity that does the work
        BlackHoleEntity hole = getBlackHoleEntity(world, caster, lastFree);
        world.spawnEntity(hole);

        if (!exemptFromCosts) {
            NEXT_ALLOWED_TICK.put(caster.getUuid(), now + COOLDOWN_TICKS);
        }

        caster.sendMessage(Text.literal("Black Hole cast."), true);
        return true;
    }

    private static @NotNull BlackHoleEntity getBlackHoleEntity(ServerWorld world, ServerPlayerEntity caster, Vec3d lastFree) {
        BlackHoleEntity hole = new BlackHoleEntity(ModEntities.BLACK_HOLE, world);
        hole.setOwner(caster);
        hole.setCenter(lastFree);

        hole.setCoreRadius(CORE_RADIUS);
        hole.setMidRadius(MID_RADIUS);
        hole.setPullRadius(PULL_RADIUS);

        hole.setDurationTicks(DURATION_TICKS);
        hole.setPullStrength(PULL_STRENGTH);

        hole.setCoreDamagePerSecond(CORE_DPS);
        hole.setMidDamagePerSecond(MID_DPS);

        hole.refreshPositionAndAngles(lastFree.x, lastFree.y, lastFree.z, 0f, 0f);
        return hole;
    }

    private static void spawnTrailStep(ServerWorld world, Vec3d p) {
        // cheaper than drawing a full line each cast, and matches projectile travel
        world.spawnParticles(ParticleTypes.ASH, p.x, p.y, p.z, 1, 0.01, 0.01, 0.01, 0.0);
        world.spawnParticles(ParticleTypes.END_ROD, p.x, p.y, p.z, 1, 0.01, 0.01, 0.01, 0.0);
    }

    @SuppressWarnings("unused")
    private static void spawnTrail(ServerWorld world, Vec3d from, Vec3d to) {
        Vec3d diff = to.subtract(from);
        double len = diff.length();
        if (len < 0.001) return;

        Vec3d dir = diff.multiply(1.0 / len);
        int count = (int) Math.ceil(len / TRAIL_STEP);

        for (int i = 0; i <= count; i++) {
            Vec3d p = from.add(dir.multiply(i * TRAIL_STEP));
            world.spawnParticles(ParticleTypes.ASH, p.x, p.y, p.z, 1, 0.02, 0.02, 0.02, 0.0);
            world.spawnParticles(ParticleTypes.END_ROD, p.x, p.y, p.z, 1, 0.02, 0.02, 0.02, 0.0);
        }
    }

    private static boolean isHoldingCreativeStaff(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Identifier id = Registries.ITEM.getId(stack.getItem());
        return CREATIVE_STAFF_ID.equals(id);
    }
}
