package net.owen.bladebound.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.owen.bladebound.accessory.BladeboundAccessoryHolder;
import net.owen.bladebound.item.ModItems;

public class MurasameCurseEffect extends StatusEffect {

    public MurasameCurseEffect() {
        super(StatusEffectCategory.HARMFUL, 0x5B1B7A);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return duration <= 1;
    }

    @Override
    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {
        if (entity instanceof PlayerEntity player && hasMurasameGauntletsEquipped(player)) {
            return true;
        }

        if (isBossImmune(entity)) {
            return true;
        }

        entity.kill(world);
        return true;
    }

    private static boolean hasMurasameGauntletsEquipped(PlayerEntity player) {
        Inventory inv = ((BladeboundAccessoryHolder) player).bladebound$getAccessoryInv();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty() && stack.isOf(ModItems.MURASAME_GAUNTLETS)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isBossImmune(LivingEntity e) {
        return e.getType().toString().contains("wither")
                || e.getType().toString().contains("ender_dragon")
                || e.getType().toString().contains("warden");
    }

    public static final Identifier ID = Identifier.of("bladebound", "murasame_curse");

    public static MurasameCurseEffect register() {
        return Registry.register(Registries.STATUS_EFFECT, ID, new MurasameCurseEffect());
    }
}