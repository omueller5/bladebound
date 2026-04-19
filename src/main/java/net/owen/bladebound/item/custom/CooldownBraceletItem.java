package net.owen.bladebound.item.custom;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.random.Random;

import java.util.List;
import java.util.function.Consumer;

public class CooldownBraceletItem extends Item {

    public static final String COOLDOWN_ROLL_KEY = "bladebound:cooldown_reduction_pct";

    public CooldownBraceletItem(Settings settings) {
        super(settings);
    }

    /** Gets the stack's custom NBT data (copy), never null. */
    private static NbtCompound getCustomDataCopy(ItemStack stack) {
        NbtComponent comp = stack.get(DataComponentTypes.CUSTOM_DATA);
        return comp != null ? comp.copyNbt() : new NbtCompound();
    }

    /** Writes custom NBT data back onto the stack. */
    private static void setCustomData(ItemStack stack, NbtCompound nbt) {
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }

    /** 10..35 inclusive, stored once per stack. */
    public static int getOrCreateRoll(ItemStack stack, Random random) {
        NbtCompound nbt = getCustomDataCopy(stack);

        if (!nbt.contains(COOLDOWN_ROLL_KEY)) {
            int pct = 10 + random.nextInt(26); // 10..35 inclusive
            nbt.putInt(COOLDOWN_ROLL_KEY, pct);
            setCustomData(stack, nbt); // persist the roll onto the stack
            return pct;
        }

        return nbt.getInt(COOLDOWN_ROLL_KEY, 10);
    }

    /** Multiplier to apply to cooldown ticks. Example: -20% => 0.80f */
    public static float getCooldownMultiplier(ItemStack stack, Random random) {
        int pct = getOrCreateRoll(stack, random);
        return 1.0f - (pct / 100.0f);
    }

    /**
     * Helper you call from your spell cooldown code:
     * baseTicks -> reduced ticks using this bracelet's roll (or fixed percent).
     */
    public static int applyReductionToCooldownTicks(int baseTicks, ItemStack braceletStack, Random random) {
        if (braceletStack == null || braceletStack.isEmpty()) return baseTicks;

        float mult = getCooldownMultiplier(braceletStack, random);
        return Math.max(1, Math.round(baseTicks * mult));
    }

    @Override
    public void inventoryTick(ItemStack stack, net.minecraft.server.world.ServerWorld world, Entity entity, net.minecraft.entity.EquipmentSlot slot) {
        super.inventoryTick(stack, world, entity, slot);

        if (world.isClient()) return;
        if (!(entity instanceof PlayerEntity player)) return;

        // Do NOT roll for fixed bracelet
        if (stack.getItem() instanceof FixedCooldownBraceletItem) return;

        getOrCreateRoll(stack, player.getRandom());
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        List<Text> tooltip = new java.util.ArrayList<>();
        tooltip.add(Text.literal("A faint sigil hums against your wrist.").formatted(Formatting.LIGHT_PURPLE));
        tooltip.add(Text.literal("Reduces spell cooldown.").formatted(Formatting.GRAY));
        tooltip.add(Text.literal(""));

        NbtCompound nbt = getCustomDataCopy(stack);
        if (nbt.contains(COOLDOWN_ROLL_KEY)) {
            int pct = nbt.getInt(COOLDOWN_ROLL_KEY, 10);
            tooltip.add(Text.literal("Cooldown: -" + pct + "%").formatted(Formatting.GOLD, Formatting.BOLD));
        } else {
            tooltip.add(Text.literal("Cooldown: 10%–35%").formatted(Formatting.GOLD, Formatting.BOLD));
        }

        tooltip.forEach(textConsumer);
    }
}
