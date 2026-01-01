package net.owen.bladebound.item.custom;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.owen.bladebound.magic.SpellHolder;
import net.owen.bladebound.magic.StaffSpell;

import java.util.List;

public class SpellGrantItem extends Item {

    // Stable spell id like bladebound:mana_barrier, bladebound:stone_dart, etc.
    private final Identifier spellId; // nullable (if you want to derive from item id)
    private final Formatting nameColor; // nullable
    private final List<Text> loreLines; // nullable

    public SpellGrantItem(Settings settings, Identifier spellId) {
        this(settings, spellId, null, null);
    }

    public SpellGrantItem(Settings settings, Identifier spellId, Formatting nameColor) {
        this(settings, spellId, nameColor, null);
    }

    public SpellGrantItem(Settings settings, Identifier spellId, Formatting nameColor, List<Text> loreLines) {
        super(settings);
        this.spellId = spellId;
        this.nameColor = nameColor;
        this.loreLines = loreLines;
    }

    @Override
    public Text getName(ItemStack stack) {
        Text base = super.getName(stack);
        return (nameColor == null) ? base : base.copy().formatted(nameColor);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (loreLines != null && !loreLines.isEmpty()) {
            tooltip.addAll(loreLines);
        }

        StaffSpell spell = resolveSpell(stack);

        tooltip.add(Text.literal(" ")); // spacer

        // ---- Mana + Cooldown ----
        if (spell == StaffSpell.PERFECT_HEAL) {
            tooltip.add(Text.literal("Mana: All remaining").formatted(Formatting.DARK_GRAY));
            tooltip.add(Text.literal("Cooldown: 115–160s").formatted(Formatting.DARK_GRAY));
            tooltip.add(Text.literal("(Scales by % mana used)").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
        } else if (spell == StaffSpell.MANA_BARRIER) {
            tooltip.add(Text.literal("Mana: Varies").formatted(Formatting.DARK_GRAY));
            tooltip.add(Text.literal("Cooldown: None").formatted(Formatting.DARK_GRAY));
        } else {
            tooltip.add(Text.literal("Mana: " + spell.manaCost).formatted(Formatting.DARK_GRAY));
            tooltip.add(Text.literal("Cooldown: " + getStaticCooldownSeconds(spell) + "s").formatted(Formatting.DARK_GRAY));
        }
    }

    private StaffSpell resolveSpell(ItemStack stack) {
        if (spellId != null) {
            return StaffSpell.fromId(spellId);
        }

        Identifier itemId = Registries.ITEM.getId(stack.getItem());
        String path = itemId.getPath();
        if (path.endsWith("_spell")) path = path.substring(0, path.length() - "_spell".length());
        Identifier derivedSpellId = Identifier.of(itemId.getNamespace(), path);
        return StaffSpell.fromId(derivedSpellId);
    }

    private static int getStaticCooldownSeconds(StaffSpell spell) {
        return switch (spell) {
            case FIREBOLT -> 10;
            case FROST_RAY -> 15;
            case HEAL -> 25;
            case STONE_DART -> 10;
            case LIGHTNING_STRIKE -> 35;
            case MANA_BARRIER -> 0;
            case ZOLTRAAK -> 65;
            case PERFECT_HEAL -> 160;
            case WORLD_REWRITE -> 900;
        };
    }

    private static Identifier getSpellIdForTags(ItemStack stack, Identifier constructorId) {
        if (constructorId != null) return constructorId;

        Identifier itemId = Registries.ITEM.getId(stack.getItem());
        String path = itemId.getPath();
        if (path.endsWith("_spell")) path = path.substring(0, path.length() - "_spell".length());
        return Identifier.of(itemId.getNamespace(), path);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (world.isClient) {
            return TypedActionResult.success(stack, true);
        }

        if (!(user instanceof ServerPlayerEntity sp)) {
            return TypedActionResult.success(stack);
        }

        Identifier idForTags = getSpellIdForTags(stack, this.spellId);

        // IMPORTANT: ensure any old colon tags get migrated before checks/writes
        SpellHolder.bladebound$migrateColonTagsToSafe(sp);

        // Already learned (safe-tag system)
        if (SpellHolder.bladebound$hasLearnTag(sp, idForTags)) {
            sp.sendMessage(Text.literal("You already learned that spell.").formatted(Formatting.GRAY), true);

            world.playSound(
                    null,
                    sp.getBlockPos(),
                    SoundEvents.ENTITY_VILLAGER_NO,
                    SoundCategory.PLAYERS,
                    0.6f,
                    1.0f
            );

            return TypedActionResult.success(stack);
        }

        // Learn + select (safe-tag system)
        SpellHolder.bladebound$addLearnTag(sp, idForTags);
        SpellHolder.bladebound$setSelectedTag(sp, idForTags);

        sp.sendMessage(Text.literal("Learned spell!").formatted(Formatting.GREEN), true);

        world.playSound(
                null,
                sp.getBlockPos(),
                SoundEvents.ENTITY_PLAYER_LEVELUP,
                SoundCategory.PLAYERS,
                0.7f,
                1.4f
        );

        if (!sp.getAbilities().creativeMode) {
            stack.decrement(1);
        }

        return TypedActionResult.success(stack);
    }
}
