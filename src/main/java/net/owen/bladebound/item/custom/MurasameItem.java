package net.owen.bladebound.item.custom;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.owen.bladebound.BladeboundBind;
import net.owen.bladebound.BladeboundConfig;
import net.owen.bladebound.effect.BladeboundEffects;
import net.owen.bladebound.item.ModItems;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.Set;

public class MurasameItem extends Item {

    // Replaces Poison/Wither with your custom curse.
    // Make this whatever pacing feels right.
    private static final int CURSE_TICKS_BASE = 20 * 5; // ~5s for “normal” mobs

    public MurasameItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        List<Text> tooltip = new java.util.ArrayList<>();
        tooltip.add(Text.literal("LEGENDARY").formatted(Formatting.GOLD, Formatting.BOLD));
        tooltip.add(Text.literal("A cursed edge that drinks the last heartbeat.").formatted(Formatting.DARK_RED, Formatting.ITALIC));
        tooltip.add(Text.literal("Its mark lingers… and the body follows.").formatted(Formatting.GRAY, Formatting.ITALIC));

        tooltip.add(Text.literal(" "));
        BladeboundBind.appendBindTooltip(stack, tooltip);

        tooltip.forEach(textConsumer);
    }

    private static void enforceEnchantRules(ServerWorld world, ItemStack stack) {
        if (!BladeboundConfig.DATA.enforceAllowedEnchantments) return;

        var enchantReg = world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
        RegistryEntry<?> unbreaking = enchantReg.getEntry(enchantReg.getValueOrThrow(Enchantments.UNBREAKING));
        RegistryEntry<?> mending = enchantReg.getEntry(enchantReg.getValueOrThrow(Enchantments.MENDING));

        Set<RegistryEntry<?>> allowed = new HashSet<>();
        allowed.add((RegistryEntry<?>) unbreaking);
        allowed.add((RegistryEntry<?>) mending);

        EnchantmentHelper.apply(stack, builder ->
                builder.remove(entry -> !allowed.contains(entry))
        );
    }

    @Override
    public void inventoryTick(ItemStack stack, net.minecraft.server.world.ServerWorld world,
                              net.minecraft.entity.Entity entity, net.minecraft.entity.EquipmentSlot slot) {
        boolean selected = slot == EquipmentSlot.MAINHAND;

        if (!world.isClient()
                && selected
                && entity instanceof ServerPlayerEntity player
                && world instanceof ServerWorld sw) {

            BladeboundBind.bindIfUnbound(stack, player);

            if (world.getTime() % 20L == 0L) {
                enforceEnchantRules(sw, stack);
            }

            BladeboundBind.allowUseOrPunish(stack, player);
        }

        super.inventoryTick(stack, world, entity, slot);
    }

    @Override
    public void postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {

        if (!attacker.getEntityWorld().isClient() && attacker instanceof ServerPlayerEntity player) {

            BladeboundBind.bindIfUnbound(stack, player);

            // If not owner: do nothing special
            if (!BladeboundBind.allowUseOrPunish(stack, player)) {
                super.postHit(stack, target, attacker);
                return;
            }

            ServerWorld serverWorld = (ServerWorld) attacker.getEntityWorld();

            // Particles (keep)
            serverWorld.spawnParticles(
                    new DustParticleEffect(0x990000, 1.2f),
                    target.getX(),
                    target.getBodyY(0.5),
                    target.getZ(),
                    12,
                    0.2, 0.3, 0.2,
                    0.02
            );

            // --- Apply Murasame Curse instead of poison/wither ---
            if (!isBossImmune(target) && !isGauntletImmune(target)) {
                int duration = curseDurationFor(target);
                target.addStatusEffect(new StatusEffectInstance(
                        BladeboundEffects.MURASAME_CURSE,
                        duration,
                        0,
                        true,
                        false
                ));
            }

            if (BladeboundConfig.DATA.durabilityEnabled) {
                stack.damage(BladeboundConfig.DATA.durabilityPerHit, attacker, EquipmentSlot.MAINHAND);
            }
        }

        super.postHit(stack, target, attacker);
    }

    private static boolean isGauntletImmune(LivingEntity entity) {
        if (!(entity instanceof ServerPlayerEntity player)) return false;

        return !net.owen.bladebound.compat.AccessoryChecks.getEquippedAccessoryStack(player, ModItems.MURASAME_GAUNTLETS).isEmpty();
    }

    private static boolean isBossImmune(LivingEntity e) {
        // Keep your existing “boss immunity” idea.
        // If you want exact EntityType checks, tell me your exact MC version/mappings.
        String t = e.getType().toString();
        return t.contains("wither") || t.contains("ender_dragon") || t.contains("warden");
    }

    private static int curseDurationFor(LivingEntity e) {
        // Simple scaling: stronger mobs take longer to die
        float maxHp = e.getMaxHealth();

        if (maxHp <= 30.0f) return 20 * 5;     // ~5s
        if (maxHp <= 60.0f) return 20 * 30;    // ~30s
        return 20 * 60;                        // ~60s (still not bosses)
    }
}
