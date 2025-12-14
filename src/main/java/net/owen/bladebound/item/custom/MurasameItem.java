package net.owen.bladebound.item.custom;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.owen.bladebound.BladeboundBind;
import net.owen.bladebound.BladeboundConfig;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MurasameItem extends SwordItem {

    private static final int POISON_TICKS = 20 * 4;
    private static final int WITHER_TICKS = 20 * 3;

    public MurasameItem(Settings settings) {
        super(ToolMaterials.NETHERITE, settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("LEGENDARY").formatted(Formatting.GOLD, Formatting.BOLD));
        tooltip.add(Text.literal("A cursed edge that drinks the last heartbeat.").formatted(Formatting.DARK_RED, Formatting.ITALIC));
        tooltip.add(Text.literal("Its mark lingers… and the body follows.").formatted(Formatting.GRAY, Formatting.ITALIC));

        tooltip.add(Text.literal(" "));
        BladeboundBind.appendBindTooltip(stack, tooltip);
    }

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

            BladeboundBind.bindIfUnbound(stack, player);

            if (world.getTime() % 20L == 0L) {
                enforceEnchantRules(sw, stack);
            }

            BladeboundBind.allowUseOrPunish(stack, player);
        }

        super.inventoryTick(stack, world, entity, slot, selected);
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {

        if (!attacker.getWorld().isClient() && attacker instanceof ServerPlayerEntity player) {

            BladeboundBind.bindIfUnbound(stack, player);

            // If not owner: block poison/wither + particles + durability logic
            if (!BladeboundBind.allowUseOrPunish(stack, player)) {
                return super.postHit(stack, target, attacker);
            }

            ServerWorld serverWorld = (ServerWorld) attacker.getWorld();

            serverWorld.spawnParticles(
                    new DustParticleEffect(new Vector3f(0.6f, 0.0f, 0.0f), 1.2f),
                    target.getX(),
                    target.getBodyY(0.5),
                    target.getZ(),
                    12,
                    0.2, 0.3, 0.2,
                    0.02
            );

            target.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, POISON_TICKS, 1));
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, WITHER_TICKS, 0));

            if (BladeboundConfig.DATA.durabilityEnabled) {
                stack.damage(BladeboundConfig.DATA.durabilityPerHit, attacker, EquipmentSlot.MAINHAND);
            }
        }

        return super.postHit(stack, target, attacker);
    }
}
