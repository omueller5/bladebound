package net.owen.bladebound.item.custom;

import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ArchmageHatItem extends Item {

    public ArchmageHatItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        List<Text> tooltip = new ArrayList<>();
        tooltip.add(Text.literal("LEGENDARY").formatted(Formatting.GOLD, Formatting.BOLD));

        tooltip.add(Text.literal(" "));
        tooltip.add(Text.literal("Magic doesn’t shout. It waits.").formatted(Formatting.LIGHT_PURPLE, Formatting.ITALIC));
        tooltip.add(Text.literal("Sustain is a weapon for those who endure.").formatted(Formatting.LIGHT_PURPLE, Formatting.ITALIC));

        tooltip.add(Text.literal(" "));
        tooltip.add(Text.literal("+10% Maximum Mana").formatted(Formatting.AQUA));
        tooltip.add(Text.literal("Regenerate 2 Mana per Second").formatted(Formatting.AQUA));

        tooltip.forEach(textConsumer);
    }
}
