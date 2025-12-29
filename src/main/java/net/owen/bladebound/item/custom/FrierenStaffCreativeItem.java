package net.owen.bladebound.item.custom;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.owen.bladebound.magic.SpellHolder;
import net.owen.bladebound.magic.StaffSpell;

import java.util.List;

public class FrierenStaffCreativeItem extends FrierenStaffItem {

    private static final int ALL_SPELLS_MASK =
            (1 << StaffSpell.values().length) - 1;

    public FrierenStaffCreativeItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);

        if (world.isClient) return;
        if (!(entity instanceof ServerPlayerEntity sp)) return;

        SpellHolder spells = (SpellHolder) sp;

        // If you only want unlock-all while HOLDING the staff, uncomment:
        // if (!selected) return;

        if (spells.bladebound$getLearnedMask() != ALL_SPELLS_MASK) {
            spells.bladebound$setLearnedMask(ALL_SPELLS_MASK);

            // Optional: if you want to auto-select something valid, set it here
            // (keeping it simple: don't force selection unless you want it)
            // spells.bladebound$setSelectedSpell(0);

            // If your UI needs a sync packet, uncomment:
            // ModPackets.sendSpellState(sp);
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, net.minecraft.entity.player.PlayerEntity user, Hand hand) {
        // Cast exactly like survival staff
        TypedActionResult<ItemStack> result = super.use(world, user, hand);

        // No cooldown
        if (!world.isClient) {
            user.getItemCooldownManager().remove(this);
        }

        return result;
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("Creative version of Frieren's Staff").formatted(Formatting.DARK_AQUA, Formatting.ITALIC));
        tooltip.add(Text.literal("Grants infinite mana when held and has no cooldown cost").formatted(Formatting.DARK_AQUA, Formatting.ITALIC));
    }
}
