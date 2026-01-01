package net.owen.bladebound.item.custom;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.owen.bladebound.fx.ZenitsuTrailTicker;

import java.util.List;

public class ZenitsuSwordItem extends SwordItem {

    private static final int TCF_COOLDOWN_TICKS = 20 * 6; // tune

    // “Anime distance” (clamped by walls)
    private static final double DASH_RANGE = 18.0; // try 16–26

    // How much of the distance happens instantly (blink feel)
    private static final double SNAP_MAX = 10.0;   // try 8–14

    // Extra push after the snap (keeps it feeling like a dash)
    private static final double DASH_IMPULSE = 1.05; // try 0.8–1.5

    private static final double STOP_BEFORE_BLOCK = 0.65;

    // How long the lightning “aftertrail” lasts
    private static final int TRAIL_TICKS = 10; // ~0.5s (try 8–14)

    public ZenitsuSwordItem(ToolMaterial material, int attackDamage, float attackSpeed, Settings settings) {
        super(material, settings.attributeModifiers(SwordItem.createAttributeModifiers(material, attackDamage, attackSpeed)));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        // Only Shift + Right Click
        if (!user.isSneaking()) {
            return TypedActionResult.pass(stack);
        }

        if (user.getItemCooldownManager().isCoolingDown(this)) {
            return TypedActionResult.fail(stack);
        }

        if (!world.isClient) {
            Vec3d look = user.getRotationVec(1.0f).normalize();
            Vec3d start = user.getPos().add(0.0, 0.1, 0.0);

            Vec3d end = getDashEnd(world, user, start, look, DASH_RANGE);
            Vec3d delta = end.subtract(start);
            double dist = delta.length();

            if (dist > 0.01) {
                Vec3d dir = delta.multiply(1.0 / dist);

                // Start trailing lightning OFF the player for a few ticks
                if (user instanceof ServerPlayerEntity sp) {
                    ZenitsuTrailTicker.start(sp, look, TRAIL_TICKS);
                }

                // Snap forward (big distance)
                double snap = Math.min(SNAP_MAX, dist);
                Vec3d snapped = start.add(dir.multiply(snap));
                user.requestTeleport(snapped.x, user.getY(), snapped.z);

                // Add some momentum
                user.addVelocity(dir.x * DASH_IMPULSE, 0.02, dir.z * DASH_IMPULSE);
                user.velocityModified = true;

                user.fallDistance = 0.0f;

                // Sound
                world.playSound(null, user.getBlockPos(),
                        SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS,
                        0.30f, 1.8f);
            }

            // Cooldown
            user.getItemCooldownManager().set(this, TCF_COOLDOWN_TICKS);
        }

        return TypedActionResult.success(stack, world.isClient);
    }

    private static Vec3d getDashEnd(World world, PlayerEntity user, Vec3d start, Vec3d look, double range) {
        Vec3d intendedEnd = start.add(look.multiply(range));

        HitResult hit = world.raycast(new RaycastContext(
                start,
                intendedEnd,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                user
        ));

        if (hit.getType() != HitResult.Type.MISS) {
            Vec3d hitPos = hit.getPos();
            Vec3d dir = hitPos.subtract(start);
            double d = dir.length();
            if (d <= 0.01) return start;

            Vec3d n = dir.multiply(1.0 / d);
            double safe = Math.max(0.0, d - STOP_BEFORE_BLOCK);
            return start.add(n.multiply(safe));
        }

        return intendedEnd;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("RARE WEAPON").formatted(Formatting.BLUE, Formatting.BOLD));
        tooltip.add(Text.literal("A Nichirin blade honed through countless repetitions.")
                .formatted(Formatting.AQUA, Formatting.ITALIC));
        tooltip.add(Text.literal("Its wielder moves faster than thought,")
                .formatted(Formatting.AQUA, Formatting.ITALIC));
        tooltip.add(Text.literal("striking before fear can take hold.")
                .formatted(Formatting.AQUA, Formatting.ITALIC));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("In a single moment,")
                .formatted(Formatting.LIGHT_PURPLE, Formatting.ITALIC));
        tooltip.add(Text.literal("thunder echoes where the blade once was.")
                .formatted(Formatting.LIGHT_PURPLE, Formatting.ITALIC));
    }
}
