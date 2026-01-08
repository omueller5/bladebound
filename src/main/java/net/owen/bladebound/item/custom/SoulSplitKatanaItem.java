package net.owen.bladebound.item.custom;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public class SoulSplitKatanaItem extends SwordItem {

    /* =========================================================
       SOUL SPLIT KATANA — Legendary Effect
       ========================================================= */

    // Bonus "soul cut" damage (2 damage = 1 heart)
    // TUNE HERE:
    private static final float BONUS_DAMAGE = 3.0f; // 1.5 hearts
    private static final float BONUS_MAX = 4.0f;    // 2 hearts cap

    // Prevents multi-hit spam (sweeping / rapid hits)
    // TUNE HERE:
    private static final int SOUL_CUT_COOLDOWN_TICKS = 10; // 0.5s

    // Per-attacker cooldown memory
    private static final Map<UUID, Integer> lastProcTick = new WeakHashMap<>();

    public SoulSplitKatanaItem(ToolMaterial material, int attackDamage, float attackSpeed, Settings settings) {
        super(material, settings.attributeModifiers(SwordItem.createAttributeModifiers(material, attackDamage, attackSpeed)));
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.postHit(stack, target, attacker);

        if (!(attacker instanceof PlayerEntity player)) return result;
        if (player.getWorld().isClient) return result;

        ServerWorld world = (ServerWorld) player.getWorld();

        int now = player.age;
        Integer last = lastProcTick.get(player.getUuid());
        if (last != null && (now - last) < SOUL_CUT_COOLDOWN_TICKS) return result;

        float bonus = Math.min(BONUS_DAMAGE, BONUS_MAX);

        // Armor-bypassing "soul" damage vibe (magic damage)
        DamageSource soul = world.getDamageSources().magic();
        target.damage(soul, bonus);

        spawnSoulSplitEffects(world, target);

        lastProcTick.put(player.getUuid(), now);
        return result;
    }

    /* =========================================================
       Visuals — Vanilla Only (NOT Black Flash looking)
       ========================================================= */

    private static void spawnSoulSplitEffects(ServerWorld world, Entity target) {
        double x = target.getX();
        double y = target.getBodyY(0.6);
        double z = target.getZ();

        // =========================
        // SIZE TUNE:
        // Increase "r" to make it expand farther out.
        // Increase counts to make it denser.
        // =========================
        double r = 0.85;   // <-- CHANGE THIS for bigger radius (try 0.65–1.25)
        double ry = 0.45;  // vertical spread
        double speed = 0.035;

        // Main identity: eerie "soul tear" (teal/void), not punchy sparks.
        // SCULK_SOUL reads very different from BF.
        world.spawnParticles(ParticleTypes.SCULK_SOUL, x, y, z,
                26, r, ry, r, speed);

        world.spawnParticles(ParticleTypes.SOUL, x, y, z,
                18, r * 0.9, ry * 0.8, r * 0.9, speed * 0.85);

        // Void pull / distortion vibe (again: not BF)
        world.spawnParticles(ParticleTypes.REVERSE_PORTAL, x, y, z,
                22, r * 0.95, ry * 0.65, r * 0.95, 0.06);

        // Dark atmosphere (reads “curse” without looking like BF dust)
        world.spawnParticles(ParticleTypes.ASH, x, y, z,
                14, r * 0.75, ry * 0.45, r * 0.75, 0.012);

        // Red accent (subtle) — avoids the BF black/red dust "spark" look.
        // Crimson spores look like a drifting stain, not an impact flash.
        world.spawnParticles(ParticleTypes.CRIMSON_SPORE, x, y, z,
                10, r * 0.7, ry * 0.55, r * 0.7, 0.02);

        // Sound: sharp + eerie (no crit-like pop)
        world.playSound(null, x, y, z,
                SoundEvents.BLOCK_SCULK_SHRIEKER_SHRIEK,
                SoundCategory.PLAYERS,
                0.18f,
                1.85f
        );

        world.playSound(null, x, y, z,
                SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME,
                SoundCategory.PLAYERS,
                0.35f,
                0.55f
        );
    }

    /* =========================================================
       LORE (Tooltip) — Legendary (Gold)
       Matches your item-lore style.
       ========================================================= */

    @Override
    public void appendTooltip(
            ItemStack stack,
            Item.TooltipContext context,
            List<Text> tooltip,
            TooltipType type
    ) {
        tooltip.add(Text.literal("LEGENDARY WEAPON")
                .formatted(Formatting.GOLD, Formatting.BOLD));

        tooltip.add(Text.literal("A blade forged by Mai Zenin.")
                .formatted(Formatting.GRAY, Formatting.ITALIC));
        tooltip.add(Text.literal("It was created at the cost of her life,")
                .formatted(Formatting.GRAY, Formatting.ITALIC));
        tooltip.add(Text.literal("leaving behind a sword that cuts deeper than flesh.")
                .formatted(Formatting.GRAY, Formatting.ITALIC));

        tooltip.add(Text.literal(""));

        tooltip.add(Text.literal("• Soul-Cut: bonus damage that bypasses armor")
                .formatted(Formatting.GOLD));
        tooltip.add(Text.literal("• A cold tear marks the wound")
                .formatted(Formatting.GOLD));
    }
}
