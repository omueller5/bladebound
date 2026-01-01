package net.owen.bladebound.mixin;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.owen.bladebound.magic.SpellHolder;
import net.owen.bladebound.magic.StaffSpell;
import net.owen.bladebound.network.ModPackets;
import net.owen.bladebound.network.Payloads;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntitySpellMixin implements SpellHolder {

    // =========================================================
    // New cooldown system (per-spell Identifier -> ticks remaining)
    // =========================================================
    @Unique private final Object2IntOpenHashMap<Identifier> bladebound$spellCooldowns = new Object2IntOpenHashMap<>();
    @Unique private boolean bladebound$cooldownSyncPending = false;

    @Override
    public int bladebound$getSpellCooldown(Identifier spellId) {
        return bladebound$spellCooldowns.getOrDefault(spellId, 0);
    }

    @Override
    public void bladebound$setSpellCooldown(Identifier spellId, int ticks) {
        if (ticks <= 0) bladebound$spellCooldowns.removeInt(spellId);
        else bladebound$spellCooldowns.put(spellId, ticks);
    }

    @Override
    public void bladebound$tickSpellCooldowns() {
        if (bladebound$spellCooldowns.isEmpty()) return;

        var it = bladebound$spellCooldowns.object2IntEntrySet().fastIterator();
        while (it.hasNext()) {
            var e = it.next();
            int next = e.getIntValue() - 1;
            if (next <= 0) it.remove();
            else e.setValue(next);
        }
    }

    // =========================================================
    // Barrier state
    // =========================================================
    @Unique private boolean bladebound$barrierActive = false;

    @Override
    public boolean bladebound$isBarrierActive() {
        return bladebound$barrierActive;
    }

    @Override
    public void bladebound$setBarrierActive(boolean active) {
        bladebound$barrierActive = active;
    }

    // =========================================================
    // SAFE command-tag format (NO colons)
    // =========================================================
    @Unique private static final String SAFE_LEARN_PREFIX = "bb_learn_";
    @Unique private static final String SAFE_SEL_PREFIX   = "bb_sel_";
    @Unique private static final String MIGRATED_SAFE_TAG = "bb_migrated_safe_v1";

    // Old (problematic) colon format you had before (cannot be removed easily via commands)
    @Unique private static final String OLD_LEARN_PREFIX = "bb_learn:";
    @Unique private static final String OLD_SEL_PREFIX   = "bb_sel:";

    // Old NBT keys (from the index/mask era)
    @Unique private static final String OLD_MASK_KEY = "bladebound_spell_mask";
    @Unique private static final String OLD_SEL_KEY  = "bladebound_spell_selected";

    // =========================================================
    // SpellHolder: learned + selected (ID-based via SAFE command tags)
    // =========================================================

    @Override
    public boolean bladebound$hasLearnedSpell(Identifier spellId) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        if (!(self instanceof ServerPlayerEntity sp)) return false;

        bladebound$migrateColonTagsToSafe(sp);

        return bladebound$hasSafeLearnTag(sp, spellId);
    }

    @Override
    public void bladebound$learnSpell(Identifier spellId) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        if (!(self instanceof ServerPlayerEntity sp)) return;

        bladebound$migrateColonTagsToSafe(sp);

        bladebound$addSafeLearnTag(sp, spellId);
    }

    @Override
    public Identifier bladebound$getSelectedSpellId() {
        PlayerEntity self = (PlayerEntity) (Object) this;
        if (!(self instanceof ServerPlayerEntity sp)) return null;

        bladebound$migrateColonTagsToSafe(sp);

        return bladebound$getSelectedFromSafeTags(sp);
    }

    @Override
    public void bladebound$setSelectedSpellId(Identifier spellId) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        if (!(self instanceof ServerPlayerEntity sp)) return;
        if (spellId == null) return;

        bladebound$migrateColonTagsToSafe(sp);

        bladebound$setSelectedSafeTag(sp, spellId);
    }

    // =========================================================
    // Sync bookkeeping (server -> client)
    // =========================================================
    @Unique private int bladebound$syncDelayTicks = 40;
    @Unique private int bladebound$lastSentLearnHash = Integer.MIN_VALUE;
    @Unique private String bladebound$lastSentSelected = null;

    // =========================================================
    // Saving/loading cooldowns only (NBT)
    // =========================================================

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void bladebound$writeCooldowns(NbtCompound nbt, CallbackInfo ci) {
        NbtCompound cds = new NbtCompound();
        for (var e : bladebound$spellCooldowns.object2IntEntrySet()) {
            Identifier id = e.getKey();
            int ticks = e.getIntValue();
            if (ticks > 0 && id != null) cds.putInt(id.toString(), ticks);
        }
        nbt.put("bladebound_spell_cooldowns", cds);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void bladebound$readCooldownsAndMigrate(NbtCompound nbt, CallbackInfo ci) {
        PlayerEntity self = (PlayerEntity) (Object) this;

        // Load cooldowns
        bladebound$spellCooldowns.clear();
        if (nbt.contains("bladebound_spell_cooldowns", NbtElement.COMPOUND_TYPE)) {
            NbtCompound cds = nbt.getCompound("bladebound_spell_cooldowns");
            for (String key : cds.getKeys()) {
                try {
                    Identifier id = Identifier.of(key);
                    int ticks = cds.getInt(key);
                    if (ticks > 0) bladebound$spellCooldowns.put(id, ticks);
                } catch (Exception ignored) {}
            }
        }
        bladebound$cooldownSyncPending = true;

        if (self instanceof ServerPlayerEntity sp) {
            // 1) Always migrate old colon tags -> safe tags
            bladebound$migrateColonTagsToSafe(sp);

            // 2) One-time migrate old index/mask NBT -> safe tags (only if present)
            if (!bladebound$hasCommandTag(sp, MIGRATED_SAFE_TAG)) {
                bladebound$migrateOldIndexDataIfPresent(sp, nbt);
                sp.addCommandTag(MIGRATED_SAFE_TAG);
            }

            // 3) Ensure selection is valid
            Identifier sel = bladebound$getSelectedFromSafeTags(sp);
            if (sel == null || !bladebound$hasSafeLearnTag(sp, sel)) {
                Identifier fallback = bladebound$firstLearnedSpell(sp);
                if (fallback != null) bladebound$setSelectedSafeTag(sp, fallback);
                else bladebound$clearSelectedSafeTags(sp);
            }
        }

        bladebound$syncDelayTicks = 40;
        bladebound$lastSentLearnHash = Integer.MIN_VALUE;
        bladebound$lastSentSelected = null;
    }

    // =========================================================
    // Server tick sync
    // =========================================================

    @Inject(method = "tick", at = @At("TAIL"))
    private void bladebound$tickSync(CallbackInfo ci) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        World world = self.getWorld();
        if (world.isClient) return;

        this.bladebound$tickSpellCooldowns();

        if (bladebound$cooldownSyncPending && self instanceof ServerPlayerEntity sp && sp.networkHandler != null) {
            bladebound$cooldownSyncPending = false;
            for (var e : bladebound$spellCooldowns.object2IntEntrySet()) {
                Identifier id = e.getKey();
                int ticks = e.getIntValue();
                if (ticks > 0 && id != null) {
                    ServerPlayNetworking.send(sp, new Payloads.SpellCooldownS2C(id, ticks));
                }
            }
        }

        if (bladebound$syncDelayTicks > 0) {
            bladebound$syncDelayTicks--;
            return;
        }

        if (self instanceof ServerPlayerEntity sp && sp.networkHandler != null) {
            bladebound$migrateColonTagsToSafe(sp);

            int learnHash = bladebound$hashLearnedSafeTags(sp);
            Identifier sel = bladebound$getSelectedFromSafeTags(sp);
            String selStr = (sel == null) ? null : sel.toString();

            if (learnHash != bladebound$lastSentLearnHash
                    || (selStr == null ? bladebound$lastSentSelected != null : !selStr.equals(bladebound$lastSentSelected))) {

                bladebound$lastSentLearnHash = learnHash;
                bladebound$lastSentSelected = selStr;

                ModPackets.sendSpellState(sp);
            }
        }
    }

    // =========================================================
    // Helpers: SAFE tags
    // =========================================================

    @Unique
    private static String bladebound$encodeId(Identifier id) {
        // No colons. Keep hyphens allowed. Replace any weird chars just in case.
        // Format: namespace__path  (path may include /, we convert to _)
        String ns = id.getNamespace();
        String path = id.getPath().replace('/', '_');
        return ns + "__" + path;
    }

    @Unique
    private static Identifier bladebound$decodeId(String encoded) {
        int sep = encoded.indexOf("__");
        if (sep <= 0) return null;
        String ns = encoded.substring(0, sep);
        String path = encoded.substring(sep + 2);
        if (ns.isBlank() || path.isBlank()) return null;
        try {
            return Identifier.of(ns, path);
        } catch (Exception e) {
            return null;
        }
    }

    @Unique
    private static String bladebound$safeLearnTag(Identifier id) {
        return SAFE_LEARN_PREFIX + bladebound$encodeId(id);
    }

    @Unique
    private static String bladebound$safeSelTag(Identifier id) {
        return SAFE_SEL_PREFIX + bladebound$encodeId(id);
    }

    @Unique
    private static boolean bladebound$hasSafeLearnTag(ServerPlayerEntity sp, Identifier id) {
        return bladebound$hasCommandTag(sp, bladebound$safeLearnTag(id));
    }

    @Unique
    private static void bladebound$addSafeLearnTag(ServerPlayerEntity sp, Identifier id) {
        String tag = bladebound$safeLearnTag(id);
        if (!bladebound$hasCommandTag(sp, tag)) sp.addCommandTag(tag);
    }

    @Unique
    private static Identifier bladebound$getSelectedFromSafeTags(ServerPlayerEntity sp) {
        for (String tag : sp.getCommandTags()) {
            if (!tag.startsWith(SAFE_SEL_PREFIX)) continue;
            String raw = tag.substring(SAFE_SEL_PREFIX.length());
            return bladebound$decodeId(raw);
        }
        return null;
    }

    @Unique
    private static void bladebound$setSelectedSafeTag(ServerPlayerEntity sp, Identifier id) {
        bladebound$clearSelectedSafeTags(sp);
        sp.addCommandTag(bladebound$safeSelTag(id));
    }

    @Unique
    private static void bladebound$clearSelectedSafeTags(ServerPlayerEntity sp) {
        List<String> toRemove = new ArrayList<>();
        for (String t : sp.getCommandTags()) {
            if (t.startsWith(SAFE_SEL_PREFIX) || t.startsWith(OLD_SEL_PREFIX)) toRemove.add(t);
        }
        for (String t : toRemove) sp.removeCommandTag(t);
    }

    @Unique
    private static boolean bladebound$hasCommandTag(ServerPlayerEntity sp, String exact) {
        for (String t : sp.getCommandTags()) {
            if (t.equals(exact)) return true;
        }
        return false;
    }

    // =========================================================
    // Migration: colon tags -> safe tags (prevents old-world “corruption”)
    // =========================================================

    @Unique
    private static void bladebound$migrateColonTagsToSafe(ServerPlayerEntity sp) {
        // If there are no old tags, nothing to do.
        boolean hasOld = false;
        for (String t : sp.getCommandTags()) {
            if (t.startsWith(OLD_LEARN_PREFIX) || t.startsWith(OLD_SEL_PREFIX)) { hasOld = true; break; }
        }
        if (!hasOld) return;

        List<String> learnedIds = new ArrayList<>();
        List<String> selectedIds = new ArrayList<>();

        for (String t : sp.getCommandTags()) {
            if (t.startsWith(OLD_LEARN_PREFIX)) {
                learnedIds.add(t.substring(OLD_LEARN_PREFIX.length()));
            } else if (t.startsWith(OLD_SEL_PREFIX)) {
                selectedIds.add(t.substring(OLD_SEL_PREFIX.length()));
            }
        }

        // Convert learned
        for (String raw : learnedIds) {
            try {
                Identifier id = Identifier.of(raw);
                bladebound$addSafeLearnTag(sp, id);
            } catch (Exception ignored) {}
        }

        // Convert selected (take first valid)
        Identifier sel = null;
        for (String raw : selectedIds) {
            try {
                sel = Identifier.of(raw);
                break;
            } catch (Exception ignored) {}
        }
        if (sel != null) {
            bladebound$setSelectedSafeTag(sp, sel);
        }

        // Remove old colon tags so worlds stop carrying un-removable tags
        for (String raw : learnedIds) sp.removeCommandTag(OLD_LEARN_PREFIX + raw);
        for (String raw : selectedIds) sp.removeCommandTag(OLD_SEL_PREFIX + raw);
    }

    // =========================================================
    // Migration: old NBT mask/index -> safe tags
    // =========================================================

    @Unique
    private static void bladebound$migrateOldIndexDataIfPresent(ServerPlayerEntity sp, NbtCompound nbt) {
        if (!nbt.contains(OLD_MASK_KEY, NbtElement.INT_TYPE)) return;

        int mask = nbt.getInt(OLD_MASK_KEY);
        int selIndex = nbt.contains(OLD_SEL_KEY, NbtElement.INT_TYPE) ? nbt.getInt(OLD_SEL_KEY) : 0;

        int count = StaffSpell.values().length;
        for (int i = 0; i < count; i++) {
            if ((mask & (1 << i)) == 0) continue;
            try {
                StaffSpell s = StaffSpell.fromIndex(i);
                if (s != null && s.id != null) bladebound$addSafeLearnTag(sp, s.id);
            } catch (Exception ignored) {}
        }

        try {
            StaffSpell sel = StaffSpell.fromIndex(selIndex);
            if (sel != null && sel.id != null) bladebound$setSelectedSafeTag(sp, sel.id);
        } catch (Exception ignored) {}
    }

    @Unique
    private static Identifier bladebound$firstLearnedSpell(ServerPlayerEntity sp) {
        for (StaffSpell s : StaffSpell.values()) {
            Identifier id = s.id;
            if (id != null && bladebound$hasSafeLearnTag(sp, id)) return id;
        }
        return null;
    }

    @Unique
    private static int bladebound$hashLearnedSafeTags(ServerPlayerEntity sp) {
        int h = 1;
        for (String t : sp.getCommandTags()) {
            if (!t.startsWith(SAFE_LEARN_PREFIX)) continue;
            h = 31 * h + t.hashCode();
        }
        return h;
    }
}
