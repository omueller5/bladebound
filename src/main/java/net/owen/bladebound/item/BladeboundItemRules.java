package net.owen.bladebound.item;

import net.minecraft.enchantment.Enchantments;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.HashSet;
import java.util.Set;

public final class BladeboundItemRules {
    private BladeboundItemRules() {}

    /**
     * Removes any enchants that are NOT allowed.
     * This blocks Sharpness/Smite/etc even if added via anvil/books.
     *
     * Allowed: Unbreaking, Mending
     */
    public static void enforceAllowedEnchantments(ServerWorld world, ItemStack stack, ServerPlayerEntity player) {
        // Build allowed set from the registry (1.21 uses RegistryEntry)
        var enchReg = world.getRegistryManager().get(RegistryKeys.ENCHANTMENT);

        RegistryEntry<?> unbreaking = enchReg.entryOf(Enchantments.UNBREAKING);
        RegistryEntry<?> mending    = enchReg.entryOf(Enchantments.MENDING);

        Set<RegistryEntry<?>> allowed = new HashSet<>();
        allowed.add((RegistryEntry<?>) unbreaking);
        allowed.add((RegistryEntry<?>) mending);

        // If it has banned enchants, strip them
        boolean[] removedAny = {false};

        EnchantmentHelper.apply(stack, builder -> {
            int before = builder.getEnchantments().size();
            builder.remove(e -> !allowed.contains(e));
            int after = builder.getEnchantments().size();
            removedAny[0] = (after != before);
        });

        if (removedAny[0]) {
            player.sendMessage(Text.literal("§d[BladeBound]§r This blade rejects power enchants. (Only Unbreaking/Mending allowed)"), true);
        }
    }

    /**
     * Damages the item on hit (since your swords extend Item, not SwordItem).
     * Uses the 1.21 ItemStack.damage(int, LivingEntity, EquipmentSlot) signature.
     */
    public static void damageOnHit(ItemStack stack, LivingEntity attacker, int amount) {
        if (amount <= 0) return;
        stack.damage(amount, attacker, EquipmentSlot.MAINHAND);
    }
}
