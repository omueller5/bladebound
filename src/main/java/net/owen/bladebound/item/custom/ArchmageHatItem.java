package net.owen.bladebound.item.custom;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class ArchmageHatItem extends ArmorItem {

    public ArchmageHatItem(RegistryEntry<ArmorMaterial> material, Type type, Settings settings) {
        super(material, type, settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("LEGENDARY").formatted(Formatting.GOLD, Formatting.BOLD));

        tooltip.add(Text.literal(" "));
        tooltip.add(Text.literal("Magic doesn’t shout. It waits.").formatted(Formatting.LIGHT_PURPLE, Formatting.ITALIC));
        tooltip.add(Text.literal("Sustain is a weapon for those who endure.").formatted(Formatting.LIGHT_PURPLE, Formatting.ITALIC));

        tooltip.add(Text.literal(" "));
        tooltip.add(Text.literal("+10% Maximum Mana").formatted(Formatting.AQUA));
        tooltip.add(Text.literal("Regenerate 2 Mana per Second").formatted(Formatting.AQUA));
    }
}
