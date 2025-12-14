package net.owen.bladebound.item.custom;

import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class SandaiKitetsuItem extends SwordItem {

    public SandaiKitetsuItem(ToolMaterial material, Settings settings) {
        super(material, settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("Rare Sword").formatted(Formatting.DARK_PURPLE));
        tooltip.add(Text.literal(""));

        tooltip.add(Text.literal("• A blade from a cursed lineage").formatted(Formatting.DARK_GRAY));
        tooltip.add(Text.literal("• Unforgiving to the careless").formatted(Formatting.DARK_GRAY));

        tooltip.add(Text.literal(""));
        tooltip.add(Text.literal("It is said misfortune follows those who wield it.")
                .formatted(Formatting.GRAY));
    }
}
