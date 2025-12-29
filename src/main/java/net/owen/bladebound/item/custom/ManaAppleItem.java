package net.owen.bladebound.item.custom;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.owen.bladebound.mana.ManaHolder;

public class ManaAppleItem extends Item {

    private final int manaIncrease;

    public ManaAppleItem(Settings settings, int manaIncrease) {
        super(settings);
        this.manaIncrease = manaIncrease;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient && user instanceof PlayerEntity player) {
            if (player instanceof ManaHolder mana) {

                int newMax = mana.bladebound$getMaxMana() + manaIncrease;
                mana.bladebound$setMaxMana(newMax);

                if (mana.bladebound$getMana() > newMax) {
                    mana.bladebound$setMana(newMax);
                }
            }
        }
        return super.finishUsing(stack, world, user);
    }
}
