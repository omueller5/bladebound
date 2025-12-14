package net.owen.bladebound;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.UUID;

/**
 * Per-sword binding system:
 * - First time a player holds (selected) the sword, it binds to their UUID.
 * - If anyone else tries to use it, they get punished and all special effects/progression are blocked.
 *
 * Stored in ItemStack CUSTOM_DATA (1.21+ components).
 */
public final class BladeboundBind {
    private BladeboundBind() {}

    // We reuse the same root used by Wado progression so all blades are consistent.
    private static final String ROOT = "bladebound";
    private static final String KEY_OWNER = "ownerUuid";
    private static final String KEY_OWNER_NAME = "ownerName";

    // ---------- core storage ----------

    private static NbtCompound getCustomData(ItemStack stack) {
        return stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
    }

    private static void setCustomData(ItemStack stack, NbtCompound nbt) {
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }

    private static NbtCompound getRoot(ItemStack stack) {
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

    public static boolean isBound(ItemStack stack) {
        NbtCompound root = getRoot(stack);
        return root.containsUuid(KEY_OWNER);
    }

    public static UUID getOwnerUuid(ItemStack stack) {
        NbtCompound root = getRoot(stack);
        return root.containsUuid(KEY_OWNER) ? root.getUuid(KEY_OWNER) : null;
    }

    public static String getOwnerName(ItemStack stack) {
        NbtCompound root = getRoot(stack);
        return root.contains(KEY_OWNER_NAME, NbtElement.STRING_TYPE) ? root.getString(KEY_OWNER_NAME) : "";
    }

    /**
     * If the sword is unbound, bind it to this player.
     * Call this from inventoryTick when selected.
     */
    public static void bindIfUnbound(ItemStack stack, ServerPlayerEntity player) {
        if (isBound(stack)) return;

        NbtCompound root = getRoot(stack);
        root.putUuid(KEY_OWNER, player.getUuid());
        root.putString(KEY_OWNER_NAME, player.getName().getString());
        saveRoot(stack, root);

        // Grant "first_binding" ONLY if they have already collected a blade
        if (net.owen.bladebound.BladeboundAdvancements.has(player, "bladebound/collect_first_blade")) {
            net.owen.bladebound.BladeboundAdvancements.grant(player, "bladebound/first_binding");
        }

        player.sendMessage(Text.literal("§d[BladeBound]§r This blade has bound itself to you."), true);
    }

    public static boolean isOwner(ItemStack stack, ServerPlayerEntity player) {
        UUID owner = getOwnerUuid(stack);
        return owner != null && owner.equals(player.getUuid());
    }

    /**
     * Enforces ownership:
     * - If not owner, apply punishment and return false.
     * - If owner (or unbound), return true.
     */
    public static boolean allowUseOrPunish(ItemStack stack, ServerPlayerEntity player) {
        if (!isBound(stack)) return true; // will bind elsewhere
        if (isOwner(stack, player)) return true;

        // Punishment: makes it miserable to use stolen blades.
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 60, 1, true, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 1, true, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 60, 0, true, false));

        player.sendMessage(Text.literal("§cThis blade rejects you.§r").formatted(Formatting.RED), true);
        return false;
    }

    /**
     * Tooltip helper (client-safe): shows bound/unbound + owner name if present.
     */
    public static void appendBindTooltip(ItemStack stack, List<Text> tooltip) {
        if (!isBound(stack)) {
            tooltip.add(Text.literal("Unbound").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
            tooltip.add(Text.literal("First wielder binds this blade.").formatted(Formatting.GRAY));
            return;
        }

        String name = getOwnerName(stack);
        if (name == null || name.isBlank()) name = "Unknown";

        tooltip.add(Text.literal("Bound").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD));
        tooltip.add(Text.literal("Owner: " + name).formatted(Formatting.GRAY));
    }
}
