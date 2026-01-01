package net.owen.bladebound.item.custom;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.owen.bladebound.BladeboundBind;

import java.util.List;

public class StarkAxeItem extends AxeItem {

    // =========================================================
    // Tuning knobs
    // =========================================================
    private static final double SWEEP_RADIUS = 1.55;      // 1.3–1.8 feels good for an axe
    private static final float  SWEEP_DAMAGE_MULT = 0.45f; // fraction of player attack damage
    private static final float  SWEEP_KNOCKBACK = 0.30f;   // small shove

    public StarkAxeItem(ToolMaterial material, float attackDamage, float attackSpeed, Settings settings) {
        super(material, settings.attributeModifiers(AxeItem.createAttributeModifiers(material, attackDamage, attackSpeed)));
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean ok = super.postHit(stack, target, attacker);

        World world = target.getWorld();
        if (world.isClient) return ok;
        if (!(world instanceof ServerWorld sw)) return ok;
        if (!(attacker instanceof PlayerEntity player)) return ok;
        if (!target.isAlive()) return ok;

        // Base attack damage (good enough for consistent tuning)
        float base = (float) player.getAttributeValue(net.minecraft.entity.attribute.EntityAttributes.GENERIC_ATTACK_DAMAGE);
        float sweepDamage = Math.max(0.0f, base * SWEEP_DAMAGE_MULT);

        // Nearby entities around the primary target
        Box box = target.getBoundingBox().expand(SWEEP_RADIUS, 0.25, SWEEP_RADIUS);

        List<LivingEntity> near = world.getEntitiesByClass(LivingEntity.class, box, e ->
                e.isAlive()
                        && e != player
                        && e != target
                        && !(e instanceof ArmorStandEntity)
                        && !e.isTeammate(player)
        );

        if (near.isEmpty() || sweepDamage <= 0.0f) return ok;

        DamageSource src = world.getDamageSources().playerAttack(player);

        Vec3d look = player.getRotationVec(1.0f).normalize();

        boolean hitAny = false;

        for (LivingEntity e : near) {
            // keep it tight and fair
            if (e.squaredDistanceTo(target) > (SWEEP_RADIUS * SWEEP_RADIUS)) continue;

            // Don't sweep through walls
            if (!player.canSee(e)) continue;

            e.damage(src, sweepDamage);

            if (SWEEP_KNOCKBACK > 0.0f) {
                e.takeKnockback(SWEEP_KNOCKBACK, -look.x, -look.z);
            }

            hitAny = true;
        }

        if (hitAny) {
            // Sweep visual + sound (every hit that actually splashed something)
            sw.spawnParticles(
                    ParticleTypes.SWEEP_ATTACK,
                    player.getX() + look.x * 0.6,
                    player.getBodyY(0.6),
                    player.getZ() + look.z * 0.6,
                    1,
                    0.0, 0.0, 0.0,
                    0.0
            );

            world.playSound(
                    null,
                    player.getBlockPos(),
                    SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
                    SoundCategory.PLAYERS,
                    0.9f,
                    0.95f
            );
        }

        return ok;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("RARE WEAPON").formatted(Formatting.BLUE, Formatting.BOLD));
        tooltip.add(Text.literal("A colossal axe wielded by warriors of immense strength.")
                .formatted(Formatting.AQUA, Formatting.ITALIC));
        tooltip.add(Text.literal("Each swing carries weight enough to shatter armor.")
                .formatted(Formatting.AQUA, Formatting.ITALIC));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("Technique is unnecessary.")
                .formatted(Formatting.LIGHT_PURPLE, Formatting.ITALIC));
        tooltip.add(Text.literal("Overwhelming force decides the outcome.")
                .formatted(Formatting.LIGHT_PURPLE, Formatting.ITALIC));
    }
}
