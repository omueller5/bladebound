package net.owen.bladebound.item.custom;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.function.Consumer;

public class MurasameGauntletsItem extends Item {

    public MurasameGauntletsItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        List<Text> tooltip = new java.util.ArrayList<>();
        // Flavor / lore (protects from the curse)
        tooltip.add(Text.literal("Warding iron forged to resist Murasame’s corruption.").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("A steady weight that keeps the curse at bay.").formatted(Formatting.GRAY));

        tooltip.add(Text.empty());

        tooltip.forEach(textConsumer);
    }
}
