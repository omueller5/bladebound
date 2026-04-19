package net.owen.bladebound.item.custom;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.function.Consumer;

public class FixedCooldownBraceletItem extends Item {

    // fixed 3% reduction
    private static final int FIXED_PCT = 3;

    public FixedCooldownBraceletItem(Settings settings) {
        super(settings);
    }

    /** Self-contained reduction logic (no NBT, no random). */
    public static int applyReductionToCooldownTicks(int baseTicks) {
        float mult = 1.0f - (FIXED_PCT / 100.0f);
        return Math.max(1, Math.round(baseTicks * mult));
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        List<Text> tooltip = new java.util.ArrayList<>();
        tooltip.add(Text.literal("A faint sigil hums against your wrist.").formatted(Formatting.LIGHT_PURPLE));
        tooltip.add(Text.literal("Reduces spell cooldown.").formatted(Formatting.GRAY));
        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("Cooldown: -" + FIXED_PCT + "%").formatted(Formatting.GOLD, Formatting.BOLD));

        tooltip.forEach(textConsumer);
    }
}
