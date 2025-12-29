package net.owen.bladebound.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import net.owen.bladebound.mana.ManaHolder;
import net.owen.bladebound.network.ModPackets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityManaMixin implements ManaHolder {

    @Unique private int bladebound$mana = 100;
    @Unique private int bladebound$maxMana = 100;

    @Unique private boolean bladebound$infiniteMana = false;

    @Unique private int bladebound$regenTicker = 0;

    // Sync bookkeeping (so we only send when changed)
    @Unique private int bladebound$lastSentMana = Integer.MIN_VALUE;
    @Unique private int bladebound$lastSentMaxMana = Integer.MIN_VALUE;
    @Unique private int bladebound$syncDelayTicks = 40; // wait ~2s after join/load before first sync

    // -------------------
    // ManaHolder impl
    // -------------------
    @Override
    public int bladebound$getMana() {
        return bladebound$mana;
    }

    @Override
    public void bladebound$setMana(int mana) {
        bladebound$mana = Math.max(0, Math.min(mana, bladebound$maxMana));
    }

    @Override
    public int bladebound$getMaxMana() {
        return bladebound$maxMana;
    }

    @Override
    public void bladebound$setMaxMana(int maxMana) {
        bladebound$maxMana = Math.max(1, maxMana);
        bladebound$mana = Math.min(bladebound$mana, bladebound$maxMana);
    }

    @Override
    public boolean bladebound$hasInfiniteMana() {
        return bladebound$infiniteMana;
    }

    @Override
    public void bladebound$setInfiniteMana(boolean value) {
        bladebound$infiniteMana = value;

        // keep HUD sane when enabling
        if (value) {
            bladebound$setMana(bladebound$maxMana);
        }

        // force a sync soon
        bladebound$lastSentMana = Integer.MIN_VALUE;
        bladebound$lastSentMaxMana = Integer.MIN_VALUE;
    }

    // -------------------
    // Save
    // -------------------
    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void bladebound$writeMana(NbtCompound nbt, CallbackInfo ci) {
        nbt.putInt("bladebound_mana", bladebound$mana);
        nbt.putInt("bladebound_max_mana", bladebound$maxMana);
        nbt.putBoolean("bladebound_infinite_mana", bladebound$infiniteMana);
    }

    // -------------------
    // Load (DO NOT SYNC HERE)
    // -------------------
    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void bladebound$readMana(NbtCompound nbt, CallbackInfo ci) {
        // Defaults (old-world safe)
        int loadedMax = 100;
        int loadedMana = 100;

        if (nbt.contains("bladebound_max_mana", NbtElement.INT_TYPE)) {
            loadedMax = nbt.getInt("bladebound_max_mana");
        }
        if (nbt.contains("bladebound_mana", NbtElement.INT_TYPE)) {
            loadedMana = nbt.getInt("bladebound_mana");
        }

        // clamp + apply
        bladebound$setMaxMana(loadedMax);
        bladebound$setMana(loadedMana);

        // load infinite flag (defaults off)
        bladebound$infiniteMana = nbt.contains("bladebound_infinite_mana", NbtElement.BYTE_TYPE)
                && nbt.getBoolean("bladebound_infinite_mana");

        // if infinite is on, top off immediately
        if (bladebound$infiniteMana) {
            bladebound$mana = bladebound$maxMana;
        }

        // Delay sync a bit after load so the player is fully connected
        bladebound$syncDelayTicks = 40;

        // Force “first sync” to happen later
        bladebound$lastSentMana = Integer.MIN_VALUE;
        bladebound$lastSentMaxMana = Integer.MIN_VALUE;
    }

    // -------------------
    // Tick: regen + safe sync
    // -------------------
    @Inject(method = "tick", at = @At("TAIL"))
    private void bladebound$tickMana(CallbackInfo ci) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        World world = self.getWorld();
        if (world.isClient) return;

        // If infinite mana is enabled, keep it full (no regen needed)
        if (bladebound$infiniteMana) {
            if (bladebound$mana != bladebound$maxMana) {
                bladebound$mana = bladebound$maxMana;
            }
        } else {
            // Regen once per second
            bladebound$regenTicker++;
            if (bladebound$regenTicker >= 20) {
                bladebound$regenTicker = 0;
                if (bladebound$mana < bladebound$maxMana) {
                    bladebound$mana++;
                }
            }
        }

        // Wait a moment after load/join before attempting to sync
        if (bladebound$syncDelayTicks > 0) {
            bladebound$syncDelayTicks--;
            return;
        }

        // Safe sync (only when player is fully networked)
        if (self instanceof ServerPlayerEntity sp && sp.networkHandler != null) {
            if (bladebound$mana != bladebound$lastSentMana || bladebound$maxMana != bladebound$lastSentMaxMana) {
                bladebound$lastSentMana = bladebound$mana;
                bladebound$lastSentMaxMana = bladebound$maxMana;
                ModPackets.sendMana(sp);
            }
        }
    }
}
