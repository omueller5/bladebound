package net.owen.bladebound.emi;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.plugin.common.displays.DefaultInformationDisplay;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.owen.bladebound.item.ModItems;

import java.util.List;

@Environment(EnvType.CLIENT)
public class BladeboundEmiPlugin implements REIClientPlugin {

    private static final Text DIV = Text.literal("────────────────────────");

    @Override
    public void registerEntries(EntryRegistry registry) {
        registry.removeEntryIf(entry -> {
            if (entry.getType() != VanillaEntryTypes.ITEM) {
                return false;
            }

            ItemStack stack = entry.castValue();
            if (stack.isEmpty()) {
                return false;
            }

            // Hide bad enchanted variants of your custom swords in REI
            boolean isBladeboundSword =
                    stack.isOf(ModItems.WADOICHIMONJI)
                            || stack.isOf(ModItems.EXCALIBUR)
                            || stack.isOf(ModItems.MURASAME);

            if (!isBladeboundSword) {
                return false;
            }

            if (!EnchantmentHelper.hasEnchantments(stack)) {
                return false;
            }

            var enchantments = EnchantmentHelper.getEnchantments(stack);
            for (var enchantment : enchantments.getEnchantments()) {
                var keyOptional = enchantment.getKey();
                if (keyOptional.isEmpty()) {
                    continue;
                }

                RegistryKey<?> key = keyOptional.get();
                Identifier id = key.getValue();
                String path = id.getPath();

                if (!path.equals("unbreaking") && !path.equals("mending")) {
                    return true;
                }
            }

            return false;
        });
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.add(DefaultInformationDisplay.createFromEntries(
                EntryIngredients.of(ModItems.WADOICHIMONJI),
                Text.literal("🟦 Wado Ichimonji")
        ).lines(List.of(
                DIV,
                Text.literal("Discipline Blade (per-sword)"),
                Text.empty(),
                Text.literal("✔ Controlled hits raise Discipline"),
                Text.literal("✘ Spam hits lower Discipline"),
                Text.literal("⏳ Discipline decays while held"),
                Text.empty(),
                Text.literal("✨ Perfect Form at high Discipline"),
                Text.empty(),
                Text.literal("Enchant Rules:"),
                Text.literal("• Allowed: Unbreaking, Mending"),
                Text.literal("• Blocked: Sharpness, Smite, etc.")
        )));

        registry.add(DefaultInformationDisplay.createFromEntries(
                EntryIngredients.of(ModItems.EXCALIBUR),
                Text.literal("🟨 Excalibur")
        ).lines(List.of(
                DIV,
                Text.literal("Legendary Holy Blade"),
                Text.empty(),
                Text.literal("✦ Bonus damage vs undead"),
                Text.literal("🛡 Netherite-tier durability"),
                Text.empty(),
                Text.literal("Enchant Rules:"),
                Text.literal("• Allowed: Unbreaking, Mending"),
                Text.literal("• Blocked: Sharpness, Smite, etc.")
        )));

        registry.add(DefaultInformationDisplay.createFromEntries(
                EntryIngredients.of(ModItems.MURASAME),
                Text.literal("🟥 Murasame")
        ).lines(List.of(
                DIV,
                Text.literal("Legendary Cursed Blade"),
                Text.empty(),
                Text.literal("☠ Poison + Wither on hit"),
                Text.literal("🛡 Netherite-tier durability"),
                Text.empty(),
                Text.literal("Enchant Rules:"),
                Text.literal("• Allowed: Unbreaking, Mending"),
                Text.literal("• Blocked: Sharpness, Smite, etc.")
        )));
    }
}
