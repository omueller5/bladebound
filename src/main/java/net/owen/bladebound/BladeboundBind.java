package net.owen.bladebound;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.UUID;

/**
 * Ownership / rejection DISABLED (single-player-friendly),
 * but we KEEP the "first_binding" advancement trigger.
 *
 * - No ownerUuid/ownerName is written anymore.
 * - No one is rejected or punished.
 * - Still grants: bladebound/first_binding (once) after collect_first_blade is earned.
 * - Optionally strips legacy owner data if present.
 */
public final class BladeboundBind {
    private BladeboundBind() {}

    private static final String ROOT = "bladebound";
    private static final String KEY_OWNER = "ownerUuid";
    private static final String KEY_OWNER_NAME = "ownerName";

    // New: store a simple per-stack flag for advancement triggering only
    private static final String KEY_ADV_GRANTED = "firstBindingGranted";

    // ---------- core storage ----------

    private static NbtCompound getCustomData(ItemStack stack) {
        return stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
    }

    private static void setCustomData(ItemStack stack, NbtCompound nbt) {
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }

    private static NbtCompound getOrCreateRoot(ItemStack stack) {
        NbtCompound data = getCustomData(stack);
        if (!data.contains(ROOT, NbtElement.COMPOUND_TYPE)) {
            data.put(ROOT, new NbtCompound());
            setCustomData(stack, data);
        }
        return data.getCompound(ROOT);
    }

    private static void saveRoot(ItemStack stack, NbtCompound root) {
        NbtCompound data = getCustomData(stack);
        data.put(ROOT, root);
        setCustomData(stack, data);
    }

    // ---------- public API ----------

    /** Ownership/binding disabled: always false. */
    public static boolean isBound(ItemStack stack) {
        return false;
    }

    /** Ownership/binding disabled: always null. */
    public static UUID getOwnerUuid(ItemStack stack) {
        return null;
    }

    /** Ownership/binding disabled: always empty. */
    public static String getOwnerName(ItemStack stack) {
        return "";
    }

    /**
     * Called from inventoryTick when selected.
     * No longer binds to a player, but still grants the first_binding advancement once.
     */
    public static void bindIfUnbound(ItemStack stack, ServerPlayerEntity player) {
        // Clean up any legacy owner data (structure-export accidents)
        stripLegacyOwnerData(stack);

        // Advancement trigger (once per stack)
        NbtCompound root = getOrCreateRoot(stack);
        if (root.getBoolean(KEY_ADV_GRANTED)) return;

        // Grant "first_binding" ONLY if they have already collected a blade
        if (BladeboundAdvancements.has(player, "bladebound/collect_first_blade")) {
            BladeboundAdvancements.grant(player, "bladebound/first_binding");
            player.sendMessage(Text.literal("§d[BladeBound]§r Your bond with the blade deepens."), true);
        }

        root.putBoolean(KEY_ADV_GRANTED, true);
        saveRoot(stack, root);
    }

    /** Ownership disabled: always true. */
    public static boolean isOwner(ItemStack stack, ServerPlayerEntity player) {
        return true;
    }

    /**
     * Enforces nothing now: always allow, no punishment.
     * Also strips legacy owner data if present.
     */
    public static boolean allowUseOrPunish(ItemStack stack, ServerPlayerEntity player) {
        stripLegacyOwnerData(stack);
        return true;
    }

    /**
     * Tooltip helper: show that ownership is disabled.
     */
    public static void appendBindTooltip(ItemStack stack, List<Text> tooltip) {
        tooltip.add(Text.literal("Unbound").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
        tooltip.add(Text.literal("Ownership disabled.").formatted(Formatting.GRAY));
    }

    // ---------- legacy cleanup ----------

    private static void stripLegacyOwnerData(ItemStack stack) {
        NbtCompound data = getCustomData(stack);
        if (!data.contains(ROOT, NbtElement.COMPOUND_TYPE)) return;

        NbtCompound root = data.getCompound(ROOT);

        boolean changed = false;

        if (root.containsUuid(KEY_OWNER)) {
            root.remove(KEY_OWNER);
            changed = true;
        }
        if (root.contains(KEY_OWNER_NAME, NbtElement.STRING_TYPE)) {
            root.remove(KEY_OWNER_NAME);
            changed = true;
        }

        // Don't delete the whole root, because we store KEY_ADV_GRANTED there now.
        if (changed) {
            data.put(ROOT, root);
            setCustomData(stack, data);
        }
    }
}
