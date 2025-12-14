package net.owen.bladebound.emi;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiInfoRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.owen.bladebound.item.ModItems;

import java.util.List;

public class BladeboundEmiPlugin implements EmiPlugin {

    private static final Text DIV = Text.literal("────────────────────────");

    @Override
    public void register(EmiRegistry registry) {
        registry.removeEmiStacks(stack ->
                stack.getItemStack().isOf(null)
                  //      || stack.getItemStack().isOf(ModItems.EXCALIBUR)
                    //    || stack.getItemStack().isOf(ModItems.MURASAME)
        );


        // =========================
        // Wado Ichimonji
        // =========================
        registry.addRecipe(new EmiInfoRecipe(
                List.<EmiIngredient>of(EmiStack.of(ModItems.WADOICHIMONJI)),
                List.of(
                        Text.literal("🟦 Wado Ichimonji"),
                        DIV,
                        Text.literal("Discipline Blade (per-sword)"),
                        Text.literal(""),
                        Text.literal("✔ Controlled hits raise Discipline"),
                        Text.literal("✘ Spam hits lower Discipline"),
                        Text.literal("⏳ Discipline decays while held"),
                        Text.literal(""),
                        Text.literal("✨ Perfect Form at high Discipline"),
                        Text.literal(""),
                        Text.literal("Enchant Rules:"),
                        Text.literal("• Allowed: Unbreaking, Mending"),
                        Text.literal("• Blocked: Sharpness, Smite, etc.")
                ),
                Identifier.of("bladebound", "emi/info_wado")
        ));

        // =========================
        // Excalibur
        // =========================
        registry.addRecipe(new EmiInfoRecipe(
                List.<EmiIngredient>of(EmiStack.of(ModItems.EXCALIBUR)),
                List.of(
                        Text.literal("🟨 Excalibur"),
                        DIV,
                        Text.literal("Legendary Holy Blade"),
                        Text.literal(""),
                        Text.literal("✦ Bonus damage vs undead"),
                        Text.literal("🛡 Netherite-tier durability"),
                        Text.literal(""),
                        Text.literal("Enchant Rules:"),
                        Text.literal("• Allowed: Unbreaking, Mending"),
                        Text.literal("• Blocked: Sharpness, Smite, etc.")
                ),
                Identifier.of("bladebound", "emi/info_excalibur")
        ));

        // =========================
        // Murasame
        // =========================
        registry.addRecipe(new EmiInfoRecipe(
                List.<EmiIngredient>of(EmiStack.of(ModItems.MURASAME)),
                List.of(
                        Text.literal("🟥 Murasame"),
                        DIV,
                        Text.literal("Legendary Cursed Blade"),
                        Text.literal(""),
                        Text.literal("☠ Poison + Wither on hit"),
                        Text.literal("🛡 Netherite-tier durability"),
                        Text.literal(""),
                        Text.literal("Enchant Rules:"),
                        Text.literal("• Allowed: Unbreaking, Mending"),
                        Text.literal("• Blocked: Sharpness, Smite, etc.")
                ),
                Identifier.of("bladebound", "emi/info_murasame")
        ));

        // =========================
        // Optional: hide “bad enchanted” variants of YOUR swords in EMI
        // (does NOT hide enchantments globally)
        // =========================
        registry.removeEmiStacks(emiStack -> {
            var stack = emiStack.getItemStack();
            if (stack == null) return false;

            boolean isBladeboundSword =
                    stack.isOf(ModItems.WADOICHIMONJI)
                            || stack.isOf(ModItems.EXCALIBUR)
                            || stack.isOf(ModItems.MURASAME);

            if (!isBladeboundSword) return false;

            var enchants = net.minecraft.enchantment.EnchantmentHelper.getEnchantments(stack);
            for (var entry : enchants.getEnchantmentEntries()) {
                String id = entry.getKey().getIdAsString();
                if (!id.endsWith("unbreaking") && !id.endsWith("mending")) {
                    return true; // hide this enchanted stack
                }
            }
            return false;
        });
    }
}
