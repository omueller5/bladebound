package net.owen.bladebound.item.custom;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.owen.bladebound.magic.SpellHolder;
import net.owen.bladebound.magic.StaffSpell;
import net.owen.bladebound.network.ModPackets;

import java.util.List;

public class FrierenStaffCreativeItem extends FrierenStaffItem {

    private static final int ALL_SPELLS_MASK =
            (1 << StaffSpell.values().length) - 1;

    public FrierenStaffCreativeItem(Settings settings) {
        super(settings);
    }
    private static final Identifier CREATIVE_STAFF_ID = Identifier.of("bladebound", "creative_staff");

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

        // Learn every registered spell by ID (no masks/indices)
        boolean changed = false;
        for (StaffSpell s : StaffSpell.values()) {
            Identifier id = s.id; // if your enum uses getter, replace with s.getId()
            if (id == null) continue;

            if (!spells.bladebound$hasLearnedSpell(id)) {
                spells.bladebound$learnSpell(id);
                changed = true;
            }
        }

        // Ensure a valid selection exists
        if (spells.bladebound$getSelectedSpellId() == null) {
            for (StaffSpell s : StaffSpell.values()) {
                Identifier id = s.id;
                if (id == null) continue;
                if (spells.bladebound$hasLearnedSpell(id)) {
                    spells.bladebound$setSelectedSpellId(id);
                    changed = true;
                    break;
                }
            }
        }

        // Sync UI/client if anything changed
        if (changed) {
            ModPackets.sendSpellState(sp);
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
