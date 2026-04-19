package net.owen.bladebound.discipline;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import java.util.UUID;

public final class DisciplineData {
    private DisciplineData() {}

    private static final String ROOT = "bladebound_discipline";
    private static final String OWNER_MOST = "OwnerMost";
    private static final String OWNER_LEAST = "OwnerLeast";
    private static final String POINTS = "Points";
    private static final String LAST_HIT_MS = "LastHitMs";
    private static final String LAST_HURT_MS = "LastHurtMs";

    /** Returns the root compound stored inside CUSTOM_DATA, creating it if missing. */
    public static NbtCompound root(ItemStack stack) {
        NbtCompound full = getCustomData(stack);

        if (!full.contains(ROOT)) {
            full.put(ROOT, new NbtCompound());
            setCustomData(stack, full);
        }

        return full.getCompoundOrEmpty(ROOT);
    }

    /** Call this after you modify the compound returned by root(). */
    private static void saveRoot(ItemStack stack, NbtCompound root) {
        NbtCompound full = getCustomData(stack);
        full.put(ROOT, root);
        setCustomData(stack, full);
    }

    private static NbtCompound getCustomData(ItemStack stack) {
        // copyNbt() gives you a mutable copy you can edit safely
        NbtComponent comp = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        return comp.copyNbt();
    }

    private static void setCustomData(ItemStack stack, NbtCompound nbt) {
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }

    public static boolean isBound(ItemStack stack) {
        NbtCompound r = root(stack);
        return r.contains(OWNER_MOST) && r.contains(OWNER_LEAST);
    }

    public static UUID getOwner(ItemStack stack) {
        NbtCompound r = root(stack);
        if (!(r.contains(OWNER_MOST) && r.contains(OWNER_LEAST))) {
            return null;
        }

        long most = r.getLong(OWNER_MOST).orElse(0L);
        long least = r.getLong(OWNER_LEAST).orElse(0L);
        return new UUID(most, least);
    }

    public static void bindTo(ItemStack stack, UUID owner) {
        NbtCompound r = root(stack);
        r.putLong(OWNER_MOST, owner.getMostSignificantBits());
        r.putLong(OWNER_LEAST, owner.getLeastSignificantBits());
        saveRoot(stack, r);
    }

    public static int getPoints(ItemStack stack) {
        return root(stack).getInt(POINTS, 0);
    }

    public static void addPoints(ItemStack stack, int amount) {
        NbtCompound r = root(stack);
        int now = Math.max(0, r.getInt(POINTS, 0) + amount);
        r.putInt(POINTS, now);
        saveRoot(stack, r);
    }

    public static long getLastHitMs(ItemStack stack) {
        return root(stack).getLong(LAST_HIT_MS).orElse(0L);
    }

    public static void setLastHitMs(ItemStack stack, long ms) {
        NbtCompound r = root(stack);
        r.putLong(LAST_HIT_MS, ms);
        saveRoot(stack, r);
    }

    public static long getLastHurtMs(ItemStack stack) {
        return root(stack).getLong(LAST_HURT_MS).orElse(0L);
    }

    public static void setLastHurtMs(ItemStack stack, long ms) {
        NbtCompound r = root(stack);
        r.putLong(LAST_HURT_MS, ms);
        saveRoot(stack, r);
    }

    // Rank thresholds
    public static int getRank(ItemStack stack) {
        int p = getPoints(stack);
        if (p >= 500) return 5;
        if (p >= 300) return 4;
        if (p >= 150) return 3;
        if (p >= 75)  return 2;
        if (p >= 25)  return 1;
        return 0;
    }

    public static String getRankName(int rank) {
        return switch (rank) {
            case 1 -> "Initiate";
            case 2 -> "Adept";
            case 3 -> "Disciplined";
            case 4 -> "Master";
            case 5 -> "Bladebound";
            default -> "Untrained";
        };
    }
}
