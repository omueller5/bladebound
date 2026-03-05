package net.owen.bladebound.mixin;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import net.owen.bladebound.item.ModItems;
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

    // BASE max mana (saved/synced)
    @Unique private int bladebound$maxMana = 100;

    @Unique private boolean bladebound$infiniteMana = false;

    @Unique private int bladebound$regenTicker = 0;

    // Sync bookkeeping
    @Unique private int bladebound$lastSentMana = Integer.MIN_VALUE;
    @Unique private int bladebound$lastSentBaseMaxMana = Integer.MIN_VALUE;
    @Unique private int bladebound$syncDelayTicks = 40;

    // -------------------
    // Archmage Hat helpers
    // -------------------
    @Unique
    private boolean bladebound$wearingArchmageHat(PlayerEntity player) {
        ItemStack head = player.getEquippedStack(EquipmentSlot.HEAD);
        return !head.isEmpty() && head.isOf(ModItems.ARCHMAGE_HAT);
    }

    @Unique
    private int bladebound$getEffectiveMaxMana(PlayerEntity player) {
        int base = bladebound$maxMana;
        if (base <= 0) return 0;

        if (bladebound$wearingArchmageHat(player)) {
            return (int) Math.floor(base * 1.10f); // +10% max mana
        }
        return base;
    }

    @Unique private int bladebound$bonusRegenSeconds = 0;

    // -------------------
    // ManaHolder impl
    // -------------------
    @Override
    public int bladebound$getMana() {
        return bladebound$mana;
    }

    @Override
    public void bladebound$setMana(int mana) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        int effMax = bladebound$getEffectiveMaxMana(self);
        bladebound$mana = Math.max(0, Math.min(mana, effMax));
    }

    // EFFECTIVE max mana (for HUD/gameplay)
    @Override
    public int bladebound$getMaxMana() {
        PlayerEntity self = (PlayerEntity) (Object) this;
        return bladebound$getEffectiveMaxMana(self);
    }

    // BASE max mana (for save/sync)
    @Override
    public int bladebound$getBaseMaxMana() {
        return bladebound$maxMana;
    }

    // sets BASE max mana
    @Override
    public void bladebound$setMaxMana(int baseMaxMana) {
        bladebound$maxMana = Math.max(1, baseMaxMana);

        PlayerEntity self = (PlayerEntity) (Object) this;
        int effMax = bladebound$getEffectiveMaxMana(self);
        bladebound$mana = Math.min(bladebound$mana, effMax);

        bladebound$lastSentMana = Integer.MIN_VALUE;
        bladebound$lastSentBaseMaxMana = Integer.MIN_VALUE;
    }

    @Override
    public boolean bladebound$hasInfiniteMana() {
        return bladebound$infiniteMana;
    }

    @Override
    public void bladebound$setInfiniteMana(boolean value) {
        bladebound$infiniteMana = value;

        if (value) {
            PlayerEntity self = (PlayerEntity) (Object) this;
            bladebound$mana = bladebound$getEffectiveMaxMana(self);
        }

        bladebound$lastSentMana = Integer.MIN_VALUE;
        bladebound$lastSentBaseMaxMana = Integer.MIN_VALUE;
    }

    // -------------------
    // Save (BASE max mana)
    // -------------------
    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void bladebound$writeMana(NbtCompound nbt, CallbackInfo ci) {
        nbt.putInt("bladebound_mana", bladebound$mana);
        nbt.putInt("bladebound_max_mana", bladebound$maxMana); // BASE
        nbt.putBoolean("bladebound_infinite_mana", bladebound$infiniteMana);
    }

    // -------------------
    // Load (BASE max mana)
    // -------------------
    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void bladebound$readMana(NbtCompound nbt, CallbackInfo ci) {
        int loadedMax = 100;
        int loadedMana = 100;

        if (nbt.contains("bladebound_max_mana", NbtElement.INT_TYPE)) {
            loadedMax = nbt.getInt("bladebound_max_mana");
        }
        if (nbt.contains("bladebound_mana", NbtElement.INT_TYPE)) {
            loadedMana = nbt.getInt("bladebound_mana");
        }

        bladebound$maxMana = Math.max(1, loadedMax);
        bladebound$mana = Math.max(0, loadedMana);

        bladebound$infiniteMana = nbt.contains("bladebound_infinite_mana", NbtElement.BYTE_TYPE)
                && nbt.getBoolean("bladebound_infinite_mana");

        PlayerEntity self = (PlayerEntity) (Object) this;
        int effMax = bladebound$getEffectiveMaxMana(self);

        bladebound$mana = Math.min(bladebound$mana, effMax);
        if (bladebound$infiniteMana) {
            bladebound$mana = effMax;
        }

        bladebound$regenTicker = 0;

        bladebound$syncDelayTicks = 40;
        bladebound$lastSentMana = Integer.MIN_VALUE;
        bladebound$lastSentBaseMaxMana = Integer.MIN_VALUE;
    }

    // -------------------
    // Tick: regen + safe sync
    // -------------------
    @Inject(method = "tick", at = @At("TAIL"))
    private void bladebound$tickMana(CallbackInfo ci) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        World world = self.getWorld();
        if (world.isClient) return;

        int effMax = bladebound$getEffectiveMaxMana(self);

        if (bladebound$infiniteMana) {
            if (bladebound$mana != effMax) {
                bladebound$mana = effMax;
            }
        } else {
            if (bladebound$mana > effMax) {
                bladebound$mana = effMax;
            }

            // Regen once per second
            bladebound$regenTicker++;
            if (bladebound$regenTicker >= 20) {
                bladebound$regenTicker = 0;

                if (bladebound$mana < effMax) {
                    // Base regen: +1 mana/sec
                    bladebound$mana++;

                    // Archmage Hat bonus: +1 extra mana/sec
                    if (bladebound$wearingArchmageHat(self) && bladebound$mana < effMax) {
                        bladebound$mana++;
                    }
                }
            }
        }

        if (bladebound$syncDelayTicks > 0) {
            bladebound$syncDelayTicks--;
            return;
        }

        // Sync BASE max mana (prevents double-10% on the client)
        if (self instanceof ServerPlayerEntity sp && sp.networkHandler != null) {
            if (bladebound$mana != bladebound$lastSentMana || bladebound$maxMana != bladebound$lastSentBaseMaxMana) {
                bladebound$lastSentMana = bladebound$mana;
                bladebound$lastSentBaseMaxMana = bladebound$maxMana;
                ModPackets.sendMana(sp);
            }
        }
    }
}
