package net.owen.bladebound.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import net.owen.bladebound.magic.SpellHolder;
import net.owen.bladebound.magic.StaffSpell;
import net.owen.bladebound.network.ModPackets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntitySpellMixin implements SpellHolder {

    // Starter spells mask (kept for backward compatibility with your saves)
    @Unique private static final int STARTER_MASK = 0b0111; // firebolt + frost + heal

    @Unique private int bladebound$learnedMask = STARTER_MASK;
    @Unique private int bladebound$selectedSpell = 0;

    @Unique private int bladebound$syncDelayTicks = 40;
    @Unique private int bladebound$lastSentMask = Integer.MIN_VALUE;
    @Unique private int bladebound$lastSentSelected = Integer.MIN_VALUE;

    @Override
    public int bladebound$getLearnedMask() {
        return bladebound$learnedMask;
    }

    @Override
    public void bladebound$setLearnedMask(int mask) {
        bladebound$learnedMask = mask;
    }

    @Override
    public int bladebound$getSelectedSpell() {
        return bladebound$selectedSpell;
    }

    @Override
    public void bladebound$setSelectedSpell(int index) {
        // Clamp using current spell count (NO hardcoded 3)
        int max = StaffSpell.values().length - 1;
        if (max < 0) {
            bladebound$selectedSpell = 0;
            return;
        }
        if (index < 0) index = 0;
        if (index > max) index = max;
        bladebound$selectedSpell = index;
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void bladebound$writeSpellState(NbtCompound nbt, CallbackInfo ci) {
        nbt.putInt("bladebound_spell_mask", bladebound$learnedMask);
        nbt.putInt("bladebound_spell_selected", bladebound$selectedSpell);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void bladebound$readSpellState(NbtCompound nbt, CallbackInfo ci) {
        // Load or default
        if (nbt.contains("bladebound_spell_mask", NbtElement.INT_TYPE)) {
            bladebound$learnedMask = nbt.getInt("bladebound_spell_mask");
        } else {
            bladebound$learnedMask = STARTER_MASK;
        }

        if (nbt.contains("bladebound_spell_selected", NbtElement.INT_TYPE)) {
            bladebound$selectedSpell = nbt.getInt("bladebound_spell_selected");
        } else {
            bladebound$selectedSpell = 0;
        }

        // Ensure starter spells are ALWAYS learned even if an older/bad mask was saved
        bladebound$learnedMask |= STARTER_MASK;

        // Clamp selected to current spell count (NO hardcoded 0..3)
        int max = StaffSpell.values().length - 1;
        if (bladebound$selectedSpell < 0) bladebound$selectedSpell = 0;
        if (max < 0) {
            bladebound$selectedSpell = 0;
        } else if (bladebound$selectedSpell > max) {
            bladebound$selectedSpell = 0;
        }

        // If selected spell isn't learned, fall back to the first learned spell (or 0 if none)
        if (!bladebound$hasLearnedSpell(bladebound$selectedSpell)) {
            int fallback = 0;
            int count = StaffSpell.values().length;
            for (int i = 0; i < count; i++) {
                if (bladebound$hasLearnedSpell(i)) {
                    fallback = i;
                    break;
                }
            }
            bladebound$selectedSpell = fallback;
        }

        // Reset sync bookkeeping
        bladebound$syncDelayTicks = 40;
        bladebound$lastSentMask = Integer.MIN_VALUE;
        bladebound$lastSentSelected = Integer.MIN_VALUE;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void bladebound$tickSync(CallbackInfo ci) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        World world = self.getWorld();
        if (world.isClient) return;

        if (bladebound$syncDelayTicks > 0) {
            bladebound$syncDelayTicks--;
            return;
        }

        if (self instanceof ServerPlayerEntity sp && sp.networkHandler != null) {
            if (bladebound$learnedMask != bladebound$lastSentMask
                    || bladebound$selectedSpell != bladebound$lastSentSelected) {

                bladebound$lastSentMask = bladebound$learnedMask;
                bladebound$lastSentSelected = bladebound$selectedSpell;
                ModPackets.sendSpellState(sp);
            }
        }
    }
}
