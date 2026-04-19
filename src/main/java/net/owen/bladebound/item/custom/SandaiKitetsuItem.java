package net.owen.bladebound.item.custom;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.function.Consumer;

public class SandaiKitetsuItem extends Item {

    public SandaiKitetsuItem(ToolMaterial material, Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        List<Text> tooltip = new java.util.ArrayList<>();
        tooltip.add(Text.literal("RARE").formatted(Formatting.BLUE, Formatting.BOLD));
        tooltip.add(Text.literal(""));

        tooltip.add(Text.literal("• A blade from a cursed lineage").formatted(Formatting.DARK_GRAY));
        tooltip.add(Text.literal("• Unforgiving to the careless").formatted(Formatting.DARK_GRAY));

        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("It is said misfortune follows those who wield it.")
                .formatted(Formatting.GRAY));

        tooltip.forEach(textConsumer);
    }
}
