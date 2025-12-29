package net.owen.bladebound.item.custom;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.owen.bladebound.magic.SpellHolder;
import net.owen.bladebound.magic.StaffSpell;
import net.owen.bladebound.network.ModPackets;

import java.util.List;

public class SpellGrantItem extends Item {

    private final int spellIndex;
    private final Formatting nameColor; // nullable
    private final List<Text> loreLines; // nullable

    public SpellGrantItem(Settings settings, int spellIndex) {
        this(settings, spellIndex, null, null);
    }

    public SpellGrantItem(Settings settings, int spellIndex, Formatting nameColor) {
        this(settings, spellIndex, nameColor, null);
    }

    public SpellGrantItem(Settings settings, int spellIndex, Formatting nameColor, List<Text> loreLines) {
        super(settings);
        this.spellIndex = spellIndex;
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

        StaffSpell spell = StaffSpell.fromIndex(spellIndex);

        tooltip.add(Text.literal(" ")); // spacer

        // ---- Mana + Cooldown (safe, no enum field access) ----
        if (spell == StaffSpell.PERFECT_HEAL) {
            tooltip.add(Text.literal("Mana: All remaining").formatted(Formatting.DARK_GRAY));
            tooltip.add(Text.literal("Cooldown: 115–160s").formatted(Formatting.DARK_GRAY));
            tooltip.add(Text.literal("(Scales by % mana used)")
                    .formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
        } else {
            tooltip.add(Text.literal("Mana: " + spell.manaCost).formatted(Formatting.DARK_GRAY));
            tooltip.add(Text.literal("Cooldown: " + getStaticCooldownSeconds(spell) + "s")
                    .formatted(Formatting.DARK_GRAY));
        }
    }

    private static int getStaticCooldownSeconds(StaffSpell spell) {
        // Matches your StaffSpell enum values (SECONDS)
        return switch (spell) {
            case FIREBOLT -> 10;
            case FROST_RAY -> 15;
            case HEAL -> 25;
            case LIGHTNING_STRIKE -> 35;
            case ZOLTRAAK -> 65;
            case PERFECT_HEAL -> 160; // shown above as range anyway
        };
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (world.isClient) {
            user.sendMessage(Text.literal("Using spell scroll..."), true);
            return TypedActionResult.success(stack, true);
        }

        if (!(user instanceof ServerPlayerEntity sp)) {
            return TypedActionResult.success(stack);
        }

        SpellHolder spells = (SpellHolder) sp;

        // Already learned
        if (spells.bladebound$hasLearnedSpell(spellIndex)) {
            sp.sendMessage(Text.literal("You already learned that spell."), true);

            world.playSound(
                    null,
                    sp.getBlockPos(),
                    SoundEvents.ENTITY_VILLAGER_NO,
                    SoundCategory.PLAYERS,
                    0.6f,
                    1.0f
            );

            ModPackets.sendSpellState(sp);
            return TypedActionResult.success(stack);
        }

        // Learn + select
        spells.bladebound$learnSpell(spellIndex);
        spells.bladebound$setSelectedSpell(spellIndex);

        ModPackets.sendSpellState(sp);

        sp.sendMessage(Text.literal("Learned spell!"), true);

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
