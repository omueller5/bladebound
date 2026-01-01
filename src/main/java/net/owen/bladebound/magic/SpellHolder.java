package net.owen.bladebound.magic;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public interface SpellHolder {

    /* =========================================================
       Spells: learned + selected (ID-based)
       ========================================================= */

    boolean bladebound$hasLearnedSpell(Identifier spellId);
    void bladebound$learnSpell(Identifier spellId);

    Identifier bladebound$getSelectedSpellId();
    void bladebound$setSelectedSpellId(Identifier spellId);

    /* =========================================================
       Cooldowns
       ========================================================= */

    int bladebound$getSpellCooldown(Identifier spellId);
    void bladebound$setSpellCooldown(Identifier spellId, int ticks);
    void bladebound$tickSpellCooldowns();

    /* =========================================================
       Barrier
       ========================================================= */

    default void bladebound$setBarrierActiveTicks(int ticks) { }
    boolean bladebound$isBarrierActive();
    void bladebound$setBarrierActive(boolean active);

    /* =========================================================
       Command-tag persistence
       IMPORTANT:
       - OLD format (colon tags): bb_learn:bladebound:world_rewrite   (bad for /tag commands)
       - NEW safe format:         bb_learn_bladebound__world_rewrite  (good for /tag commands)
       ========================================================= */

    // Old prefixes (colon-based)
    String OLD_LEARN_PREFIX = "bb_learn:";
    String OLD_SEL_PREFIX   = "bb_sel:";

    // New prefixes (safe)
    String SAFE_LEARN_PREFIX = "bb_learn_";
    String SAFE_SEL_PREFIX   = "bb_sel_";

    // Separator inside safe payload: namespace__path
    String SAFE_NS_PATH_SEP = "__";

    static String bladebound$encodeSafeId(Identifier id) {
        // Keep it simple and readable for commands:
        // bladebound:world_rewrite -> bladebound__world_rewrite
        return id.getNamespace() + SAFE_NS_PATH_SEP + id.getPath();
    }

    static Identifier bladebound$decodeSafeId(String encoded) {
        int k = encoded.indexOf(SAFE_NS_PATH_SEP);
        if (k <= 0) return null;
        String ns = encoded.substring(0, k);
        String path = encoded.substring(k + SAFE_NS_PATH_SEP.length());
        if (ns.isBlank() || path.isBlank()) return null;

        try {
            return Identifier.of(ns, path);
        } catch (Exception ignored) {
            return null;
        }
    }

    static String bladebound$learnTagSafe(Identifier id) {
        return SAFE_LEARN_PREFIX + bladebound$encodeSafeId(id);
    }

    static String bladebound$selTagSafe(Identifier id) {
        return SAFE_SEL_PREFIX + bladebound$encodeSafeId(id);
    }

    static boolean bladebound$hasLearnTag(ServerPlayerEntity sp, Identifier id) {
        // Accept BOTH formats so old worlds still work immediately
        String safe = bladebound$learnTagSafe(id);
        String old  = OLD_LEARN_PREFIX + id.toString();
        return sp.getCommandTags().contains(safe) || sp.getCommandTags().contains(old);
    }

    static void bladebound$addLearnTag(ServerPlayerEntity sp, Identifier id) {
        // Always write NEW safe tag going forward
        sp.addCommandTag(bladebound$learnTagSafe(id));
    }

    static Identifier bladebound$getSelectedFromTags(ServerPlayerEntity sp) {
        // Prefer safe selection tag if present
        for (String tag : sp.getCommandTags()) {
            if (tag.startsWith(SAFE_SEL_PREFIX)) {
                String raw = tag.substring(SAFE_SEL_PREFIX.length());
                Identifier id = bladebound$decodeSafeId(raw);
                if (id != null) return id;
            }
        }
        // Fallback: old colon selection tag
        for (String tag : sp.getCommandTags()) {
            if (tag.startsWith(OLD_SEL_PREFIX)) {
                String raw = tag.substring(OLD_SEL_PREFIX.length());
                try {
                    return Identifier.of(raw);
                } catch (Exception ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    static void bladebound$setSelectedTag(ServerPlayerEntity sp, Identifier id) {
        // Remove old selection tags safely (no removeIf on getCommandTags())
        List<String> toRemove = new ArrayList<>();
        for (String t : sp.getCommandTags()) {
            if (t.startsWith(SAFE_SEL_PREFIX) || t.startsWith(OLD_SEL_PREFIX)) {
                toRemove.add(t);
            }
        }
        for (String t : toRemove) sp.removeCommandTag(t);

        // Add new safe selection tag
        sp.addCommandTag(bladebound$selTagSafe(id));
    }

    /**
     * One-time (or safe-to-repeat) migration:
     * - Converts old colon tags into new safe tags
     * - Removes the old ones using removeCommandTag(...)
     *
     * Call this before any spell checks/writes on server.
     */
    static void bladebound$migrateColonTagsToSafe(ServerPlayerEntity sp) {
        List<String> add = new ArrayList<>();
        List<String> remove = new ArrayList<>();

        for (String tag : sp.getCommandTags()) {
            // Learned tags: bb_learn:<identifier>
            if (tag.startsWith(OLD_LEARN_PREFIX)) {
                String raw = tag.substring(OLD_LEARN_PREFIX.length());
                try {
                    Identifier id = Identifier.of(raw);
                    add.add(bladebound$learnTagSafe(id));
                    remove.add(tag);
                } catch (Exception ignored) {
                    // leave it alone if it's malformed
                }
            }

            // Selected tags: bb_sel:<identifier>
            if (tag.startsWith(OLD_SEL_PREFIX)) {
                String raw = tag.substring(OLD_SEL_PREFIX.length());
                try {
                    Identifier id = Identifier.of(raw);
                    add.add(bladebound$selTagSafe(id));
                    remove.add(tag);
                } catch (Exception ignored) {
                }
            }
        }

        // Apply changes safely (NO direct mutation of getCommandTags())
        for (String t : remove) sp.removeCommandTag(t);
        for (String t : add) sp.addCommandTag(t);
    }
}
