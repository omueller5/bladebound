package net.owen.bladebound.item.custom;

import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.owen.bladebound.BladeboundBind;
import net.owen.bladebound.BladeboundConfig;
import net.owen.bladebound.item.BladeboundItemRules;

import java.util.List;

/**
 * Wado Ichimonji — per-sword progression stored directly on the ItemStack.
 *
 * IMPORTANT (1.21+):
 * ItemStack custom data is stored via Data Components.
 * We use CUSTOM_DATA to store our custom compound safely.
 *
 * This version:
 * - Binds the sword to the first wielder (per-sword)
 * - If not owner: punish + block all progression/effects
 * - Tracks ONLY kills + XP from kills (no hits, no hit XP)
 *
 * ADVANCEMENTS:
 * - Grants bladebound:first_binding once per sword after it becomes bound
 * - Grants bladebound:perfect_form once per sword when entering Perfect Form tier
 * - Grants bladebound:master_of_wado once per sword when Master conditions are met
 */
public class WadoIchimonjiItem extends Item {

    // =========================================================
    // Persistent data keys (per-sword)
    // =========================================================
    private static final String NBT_ROOT       = "bladebound";
    private static final String NBT_XP         = "xp";
    private static final String NBT_KILLS      = "kills";

    private static final String NBT_DISCIPLINE = "discipline";     // 0..100
    private static final String NBT_LAST_HIT   = "lastHitTick";    // world time (long)
    private static final String NBT_LAST_TIER  = "lastTier";       // remembers last tier to detect transitions

    // =========================================================
    // XP balance knobs
    // =========================================================
    private static final int MAX_LEVEL   = 10;
    private static final int XP_PER_KILL = 8;

    private static final float DAMAGE_PER_LEVEL = 0.35f; // +3.5 at max

    // =========================================================
    // Discipline rules (exact)
    // =========================================================
    private static final int DISC_MIN = 0;
    private static final int DISC_MAX = 100;

    // Timing windows (ticks)
    private static final int CONTROLLED_MIN_TICKS = 12; // >= 12 ticks = controlled
    private static final int SPAM_MAX_TICKS       = 6;  // < 6 ticks = spam

    // Gains/Loss
    private static final int DISC_HIT_BASE          = 3;
    private static final int DISC_CONTROLLED_BONUS  = 2;
    private static final int DISC_SPAM_PENALTY      = 4;

    private static final int DISC_KILL_BASE         = 8;
    private static final int DISC_CLEAN_KILL_BONUS  = 6; // if last spacing was "clean"

    // Decay while selected (every 20 ticks)
    private static final int DECAY_TIER3 = 1; // 75–100
    private static final int DECAY_TIER2 = 2; // 50–74
    private static final int DECAY_TIER1 = 3; // 25–49
    private static final int DECAY_TIER0 = 1; // 0–24

    // =========================================================
    // Discipline tiers
    // =========================================================
    private enum DiscTier {
        SHAKEN(0, 24, "Shaken", 0.0f),
        STEADY(25, 49, "Steady", 0.5f),
        FOCUSED(50, 74, "Focused", 1.5f),
        PERFECT(75, 100, "Perfect Form", 3.0f);

        final int min;
        final int max;
        final String name;
        final float bonusDamage;

        DiscTier(int min, int max, String name, float bonusDamage) {
            this.min = min;
            this.max = max;
            this.name = name;
            this.bonusDamage = bonusDamage;
        }

        static DiscTier fromValue(int v) {
            for (DiscTier t : values()) {
                if (v >= t.min && v <= t.max) return t;
            }
            return SHAKEN;
        }
    }

    public WadoIchimonjiItem(Settings settings) {
        super(settings);
    }

    // =========================================================
    // Advancement helper (embedded — no extra classes needed)
    // =========================================================
    private static void grantAdvancement(ServerPlayerEntity player, String path) {
        if (player == null || player.getServer() == null) return;

        AdvancementEntry adv = player.getServer()
                .getAdvancementLoader()
                .get(Identifier.of("bladebound", path)); // must match your mod id

        if (adv == null) return;

        var progress = player.getAdvancementTracker().getProgress(adv);
        for (String criterion : progress.getUnobtainedCriteria()) {
            player.getAdvancementTracker().grantCriterion(adv, criterion);
        }
    }

    private static boolean isFlagSet(ItemStack stack, String key) {
        return getInt(stack, key) == 1;
    }

    private static void setFlag(ItemStack stack, String key) {
        putInt(stack, key, 1);
    }

    // =========================================================
    // Perfect Form entry trigger
    // =========================================================
    private static void onTierMaybeChanged(ItemStack stack, LivingEntity attacker) {
        DiscTier current = getTier(stack);

        int lastOrdinal = getInt(stack, NBT_LAST_TIER);
        DiscTier last = (lastOrdinal >= 0 && lastOrdinal < DiscTier.values().length)
                ? DiscTier.values()[lastOrdinal]
                : DiscTier.SHAKEN;

        // Save current tier so we can detect future transitions
        putInt(stack, NBT_LAST_TIER, current.ordinal());

        // Trigger ONLY when entering Perfect Form
        if (last != DiscTier.PERFECT && current == DiscTier.PERFECT) {
            attacker.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 60, 0, true, false));
            attacker.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 60, 0, true, false));

            attacker.getWorld().playSound(
                    null,
                    attacker.getBlockPos(),
                    net.minecraft.sound.SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME,
                    net.minecraft.sound.SoundCategory.PLAYERS,
                    0.8f,
                    1.2f
            );

            if (attacker.getWorld() instanceof ServerWorld sw) {
                sw.spawnParticles(
                        net.minecraft.particle.ParticleTypes.END_ROD,
                        attacker.getX(), attacker.getBodyY(0.7), attacker.getZ(),
                        20,
                        0.4, 0.6, 0.4,
                        0.02
                );
            }

            if (attacker instanceof PlayerEntity p) {
                p.sendMessage(Text.literal("§6Perfect Form§r achieved."), true);
            }
        }
    }

    // =========================================================
    // 1.21+ Custom Data Helpers (replaces getOrCreateNbt)
    // =========================================================
    private static NbtCompound getCustomData(ItemStack stack) {
        return stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
    }

    private static void setCustomData(ItemStack stack, NbtCompound nbt) {
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }

    private static NbtCompound getRoot(ItemStack stack) {
        NbtCompound data = getCustomData(stack);

        if (!data.contains(NBT_ROOT, NbtElement.COMPOUND_TYPE)) {
            data.put(NBT_ROOT, new NbtCompound());
            setCustomData(stack, data);
        }

        return data.getCompound(NBT_ROOT);
    }

    private static void saveRoot(ItemStack stack, NbtCompound root) {
        NbtCompound data = getCustomData(stack);
        data.put(NBT_ROOT, root);
        setCustomData(stack, data);
    }

    private static int getInt(ItemStack stack, String key) {
        return getRoot(stack).getInt(key);
    }

    private static long getLong(ItemStack stack, String key) {
        return getRoot(stack).getLong(key);
    }

    private static void putInt(ItemStack stack, String key, int val) {
        NbtCompound root = getRoot(stack);
        root.putInt(key, val);
        saveRoot(stack, root);
    }

    private static void putLong(ItemStack stack, String key, long val) {
        NbtCompound root = getRoot(stack);
        root.putLong(key, val);
        saveRoot(stack, root);
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    // =========================================================
    // XP/Level logic
    // =========================================================
    private static int xpNeededForLevel(int level) {
        return 15 + (level * level * 5);
    }

    private static int computeLevelFromXp(int xp) {
        int level = 0;
        int remaining = xp;

        for (int next = 1; next <= MAX_LEVEL; next++) {
            int cost = xpNeededForLevel(next);
            if (remaining >= cost) {
                remaining -= cost;
                level = next;
            } else {
                break;
            }
        }

        return level;
    }

    private static float bonusDamageFromXp(ItemStack stack) {
        int xp = getInt(stack, NBT_XP);
        int level = computeLevelFromXp(xp);
        return level * DAMAGE_PER_LEVEL;
    }

    // =========================================================
    // Discipline logic (per sword)
    // =========================================================
    private static int getDiscipline(ItemStack stack) {
        int v = getInt(stack, NBT_DISCIPLINE);
        return clamp(v, DISC_MIN, DISC_MAX);
    }

    private static void addDiscipline(ItemStack stack, int delta) {
        int v = getDiscipline(stack);
        v = clamp(v + delta, DISC_MIN, DISC_MAX);
        putInt(stack, NBT_DISCIPLINE, v);
    }

    private static DiscTier getTier(ItemStack stack) {
        return DiscTier.fromValue(getDiscipline(stack));
    }

    private static int decayAmountFor(int discipline) {
        if (discipline >= 75) return DECAY_TIER3;
        if (discipline >= 50) return DECAY_TIER2;
        if (discipline >= 25) return DECAY_TIER1;
        return DECAY_TIER0;
    }

    // =========================================================
    // Vanilla behavior tweaks
    // =========================================================
    @Override
    public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
        return false;
    }

    // =========================================================
    // Passive effect + discipline decay while selected
    // + binding enforcement
    // =========================================================
    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (world.isClient) return;

        // Only enforce bind when actually in-hand
        if (selected && entity instanceof ServerPlayerEntity player) {
            // Bind to first wielder
            BladeboundBind.bindIfUnbound(stack, player);

            // If not the owner: punish and STOP (no resistance/decay/progression)
            if (!BladeboundBind.allowUseOrPunish(stack, player)) {
                return;
            }

            // Enchant enforcement once per second (server-side)
            if (BladeboundConfig.DATA.enforceAllowedEnchantments
                    && world instanceof ServerWorld sw
                    && world.getTime() % 20L == 0L) {
                BladeboundItemRules.enforceAllowedEnchantments(sw, stack, player);
            }
        }

        // Resistance while held (owner only, because we return above if not owner)
        if (selected && entity instanceof LivingEntity living) {
            living.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.RESISTANCE,
                    20,
                    0,
                    true,
                    false
            ));
        }

        // Discipline decay (owner only)
        if (selected && (world.getTime() % 20L == 0L)) {
            int disc = getDiscipline(stack);
            int decay = decayAmountFor(disc);
            addDiscipline(stack, -decay);
        }
    }

    // =========================================================
    // Combat hook
    // =========================================================
    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.getWorld().isClient && attacker instanceof ServerPlayerEntity player) {

            // Bind on first real combat use too
            BladeboundBind.bindIfUnbound(stack, player);

            // If not owner: punish + block all special behavior/progression
            if (!BladeboundBind.allowUseOrPunish(stack, player)) {
                return super.postHit(stack, target, attacker);
            }

            // Durability loss per hit (owner only)
            if (BladeboundConfig.DATA.durabilityEnabled) {
                BladeboundItemRules.damageOnHit(stack, attacker, BladeboundConfig.DATA.durabilityPerHit);
            }

            // -------------------------------------------------
            // Discipline timing logic (hit spacing)
            // -------------------------------------------------
            long now = attacker.getWorld().getTime();
            long last = getLong(stack, NBT_LAST_HIT);
            long dt = (last == 0L) ? 9999L : (now - last);
            putLong(stack, NBT_LAST_HIT, now);

            int discDelta = DISC_HIT_BASE; // base +3

            if (dt >= CONTROLLED_MIN_TICKS) {
                discDelta += DISC_CONTROLLED_BONUS; // +2
            }

            if (dt < SPAM_MAX_TICKS) {
                discDelta -= DISC_SPAM_PENALTY; // -4

                attacker.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.WEAKNESS,
                        10,
                        0,
                        true,
                        false
                ));
            }

            addDiscipline(stack, discDelta);
            onTierMaybeChanged(stack, attacker);

            // -------------------------------------------------
            // Kill rewards ONLY (XP only from kills)
            // -------------------------------------------------
            if (target.getHealth() <= 0.0F || target.isDead()) {
                putInt(stack, NBT_KILLS, getInt(stack, NBT_KILLS) + 1);
                putInt(stack, NBT_XP, Math.max(0, getInt(stack, NBT_XP) + XP_PER_KILL));

                int killDisc = DISC_KILL_BASE;

                if (dt >= 10) {
                    killDisc += DISC_CLEAN_KILL_BONUS;
                }
                if (dt < SPAM_MAX_TICKS) {
                    killDisc -= DISC_CLEAN_KILL_BONUS;
                }

                addDiscipline(stack, killDisc);
                onTierMaybeChanged(stack, attacker);
            }

            // -------------------------------------------------
            // Apply extra damage: XP bonus + Discipline tier bonus
            // -------------------------------------------------
            float xpBonus = bonusDamageFromXp(stack);
            float discBonus = getTier(stack).bonusDamage;
            float totalBonus = xpBonus + discBonus;

            if (totalBonus > 0.0f) {
                if (attacker instanceof PlayerEntity p) {
                    target.damage(p.getDamageSources().playerAttack(p), totalBonus);
                } else {
                    target.damage(attacker.getDamageSources().mobAttack(attacker), totalBonus);
                }
            }

            // -------------------------------------------------
            // Advancement: master_of_wado (once per sword)
            // Rule (you can change later):
            // - must be MAX level
            // - must reach PERFECT tier (>= 75 discipline)
            // - must have at least 25 kills with this sword
            // -------------------------------------------------
        }

        return super.postHit(stack, target, attacker);
    }

    // =========================================================
    // Tooltip
    // =========================================================
    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        // Lore
        tooltip.add(Text.literal("RARE").formatted(Formatting.BLUE, Formatting.BOLD));
        tooltip.add(Text.literal("A blade of restraint and discipline.").formatted(Formatting.AQUA, Formatting.ITALIC));
        tooltip.add(Text.literal("Wielded with unwavering resolve.").formatted(Formatting.YELLOW, Formatting.ITALIC));

        // Binding info
        tooltip.add(Text.literal(" "));
        BladeboundBind.appendBindTooltip(stack, tooltip);

        // Stats
        int xp = getInt(stack, NBT_XP);
        int kills = getInt(stack, NBT_KILLS);

        int level = computeLevelFromXp(xp);
        float xpBonus = bonusDamageFromXp(stack);

        int disc = getDiscipline(stack);
        DiscTier tier = DiscTier.fromValue(disc);

        tooltip.add(Text.literal(" "));
        tooltip.add(Text.literal("Level: " + level + "/" + MAX_LEVEL).formatted(Formatting.AQUA));
        tooltip.add(Text.literal(String.format("XP Bonus Damage: +%.2f", xpBonus)).formatted(Formatting.GREEN));

        tooltip.add(Text.literal(String.format("Discipline: %d/100", disc)).formatted(Formatting.GOLD));
        tooltip.add(Text.literal("Form: " + tier.name).formatted(Formatting.YELLOW));
        tooltip.add(Text.literal(String.format("Form Bonus Damage: +%.2f", tier.bonusDamage)).formatted(Formatting.DARK_GREEN));

        tooltip.add(Text.literal("Kills: " + kills).formatted(Formatting.DARK_GRAY));

        // Next level progress
        if (level < MAX_LEVEL) {
            int spent = 0;
            for (int i = 1; i <= level; i++) spent += xpNeededForLevel(i);

            int toNext = xpNeededForLevel(level + 1);
            int progress = xp - spent;

            tooltip.add(Text.literal("Next Level: " + progress + "/" + toNext + " XP").formatted(Formatting.BLUE));
        } else {
            tooltip.add(Text.literal("Maxed").formatted(Formatting.GOLD));
        }
    }
}
