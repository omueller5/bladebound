package net.owen.bladebound.item.custom;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.owen.bladebound.BladeboundBind;
import net.owen.bladebound.BladeboundConfig;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExcaliburItem extends SwordItem {

    private static final float UNDEAD_BONUS_DAMAGE = 6.0f;

    public ExcaliburItem(Settings settings) {
        super(ToolMaterials.NETHERITE, settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("LEGENDARY").formatted(Formatting.DARK_PURPLE, Formatting.BOLD));
        tooltip.add(Text.literal("Blessed: Deals bonus damage to undead.").formatted(Formatting.AQUA));
        tooltip.add(Text.literal("A holy blade that answers only the worthy.").formatted(Formatting.YELLOW, Formatting.ITALIC));
        tooltip.add(Text.literal("Its light cuts through shadow and doubt.").formatted(Formatting.GRAY, Formatting.ITALIC));

        tooltip.add(Text.literal(" "));
        BladeboundBind.appendBindTooltip(stack, tooltip);
    }

    // Only Unbreaking + Mending allowed
    private static void enforceEnchantRules(ServerWorld world, ItemStack stack) {
        if (!BladeboundConfig.DATA.enforceAllowedEnchantments) return;

        var enchantReg = world.getRegistryManager().get(RegistryKeys.ENCHANTMENT);
        RegistryEntry<?> unbreaking = enchantReg.entryOf(Enchantments.UNBREAKING);
        RegistryEntry<?> mending = enchantReg.entryOf(Enchantments.MENDING);

        Set<RegistryEntry<?>> allowed = new HashSet<>();
        allowed.add((RegistryEntry<?>) unbreaking);
        allowed.add((RegistryEntry<?>) mending);

        EnchantmentHelper.apply(stack, builder ->
                builder.remove(entry -> !allowed.contains(entry))
        );
    }

    @Override
    public void inventoryTick(ItemStack stack, net.minecraft.world.World world,
                              net.minecraft.entity.Entity entity, int slot, boolean selected) {

        if (!world.isClient
                && selected
                && entity instanceof ServerPlayerEntity player
                && world instanceof ServerWorld sw) {

            // Bind on first “real use” (selected in hand)
            BladeboundBind.bindIfUnbound(stack, player);

            // Strip illegal enchants (once per second)
            if (world.getTime() % 20L == 0L) {
                enforceEnchantRules(sw, stack);
            }

            // If not owner, punish (prevents comfy stolen use)
            BladeboundBind.allowUseOrPunish(stack, player);
        }

        super.inventoryTick(stack, world, entity, slot, selected);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {

        if (!attacker.getWorld().isClient() && attacker instanceof ServerPlayerEntity player) {

            // Bind on first combat use too (in case)
            BladeboundBind.bindIfUnbound(stack, player);

            // If not owner, block all special behavior (but vanilla hit already happened)
            if (!BladeboundBind.allowUseOrPunish(stack, player)) {
                return super.postHit(stack, target, attacker);
            }

            // Your special undead bonus
            if (target.getType().isIn(EntityTypeTags.UNDEAD)) {
                target.damage(attacker.getDamageSources().magic(), UNDEAD_BONUS_DAMAGE);
            }

            // Durability loss
            if (BladeboundConfig.DATA.durabilityEnabled) {
                stack.damage(BladeboundConfig.DATA.durabilityPerHit, attacker, EquipmentSlot.MAINHAND);
            }
        }

        return super.postHit(stack, target, attacker);
    }
}
