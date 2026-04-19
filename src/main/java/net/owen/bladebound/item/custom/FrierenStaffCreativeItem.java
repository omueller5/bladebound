package net.owen.bladebound.item.custom;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import net.owen.bladebound.magic.SpellHolder;
import net.owen.bladebound.magic.StaffSpell;
import net.owen.bladebound.network.ModPackets;

import java.util.List;
import java.util.function.Consumer;

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
    public void inventoryTick(ItemStack stack, net.minecraft.server.world.ServerWorld world, Entity entity, net.minecraft.entity.EquipmentSlot slot) {
        super.inventoryTick(stack, world, entity, slot);

        if (world.isClient()) return;
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
    public ActionResult use(World world, net.minecraft.entity.player.PlayerEntity user, Hand hand) {
        // Cast exactly like survival staff
        ActionResult result = super.use(world, user, hand);

        // No cooldown
        if (!world.isClient()) {
            user.getItemCooldownManager().remove(
                    user.getItemCooldownManager().getGroup(user.getStackInHand(hand))
            );
        }

        return result;
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        List<Text> tooltip = new java.util.ArrayList<>();
        tooltip.add(Text.literal("Creative version of Frieren's Staff").formatted(Formatting.DARK_AQUA, Formatting.ITALIC));
        tooltip.add(Text.literal("Grants infinite mana when held and has no cooldown cost").formatted(Formatting.DARK_AQUA, Formatting.ITALIC));

        tooltip.forEach(textConsumer);
    }
}
