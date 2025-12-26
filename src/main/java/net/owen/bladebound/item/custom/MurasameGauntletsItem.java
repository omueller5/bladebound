package net.owen.bladebound.item.custom;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class MurasameGauntletsItem extends Item {

    public MurasameGauntletsItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        // Flavor / lore (protects from the curse)
        tooltip.add(Text.literal("Warding iron forged to resist Murasame’s corruption.").formatted(Formatting.GRAY));
        tooltip.add(Text.literal("A steady weight that keeps the curse at bay.").formatted(Formatting.GRAY));

        tooltip.add(Text.empty());

        // Equip guidance
        tooltip.add(Text.literal("Equip: Trinkets → Hand → Glove").formatted(Formatting.DARK_GRAY));
        tooltip.add(Text.literal("Also valid: Trinkets → Offhand → Glove").formatted(Formatting.DARK_GRAY));
        tooltip.add(Text.literal("Grants bonus Armor while equipped (may not show on the vanilla armor bar).")
                .formatted(Formatting.DARK_GRAY));
    }
}
