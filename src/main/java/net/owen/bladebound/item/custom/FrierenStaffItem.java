package net.owen.bladebound.item.custom;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.owen.bladebound.BladeboundBind;
import net.owen.bladebound.item.ModItems;
import net.owen.bladebound.mana.ManaHolder;
import net.owen.bladebound.magic.SpellHolder;
import net.owen.bladebound.magic.StaffSpell;
import net.owen.bladebound.network.ModPackets;
import net.owen.bladebound.network.Payloads;

import java.util.List;

public class FrierenStaffItem extends Item {

    private static final int DURABILITY_COST = 1;
    private static final double FIREBALL_SPEED = 0.6;

    public FrierenStaffItem(Settings settings) {
        super(settings);
    }

    private static ItemStack findEquippedCooldownBracelet(PlayerEntity player) {
        return net.owen.bladebound.compat.AccessoryChecks.getEquippedAccessoryStack(
                player,
                ModItems.COOLDOWN_BRACELET
        );
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (user.isSneaking()) {
            if (!world.isClient) {
                user.sendMessage(Text.literal("Use the spell menu to select spells."), true);
                world.playSound(null, user.getBlockPos(), SoundEvents.UI_BUTTON_CLICK.value(),
                        SoundCategory.PLAYERS, 0.6F, 1.2F);
            }
            return TypedActionResult.success(stack, world.isClient);
        }

        if (user.getItemCooldownManager().isCoolingDown(this)) {
            return TypedActionResult.fail(stack);
        }

        if (!world.isClient) {
            boolean creativeStaff = stack.isOf(ModItems.FRIEREN_STAFF_CREATIVE);

            SpellHolder spells = (SpellHolder) user;
            int idx = spells.bladebound$getSelectedSpell();

            int maxIdx = StaffSpell.values().length - 1;
            if (idx < 0) idx = 0;
            if (idx > maxIdx) idx = 0;

            StaffSpell spell = StaffSpell.fromIndex(idx);

            ManaHolder mana = (ManaHolder) user;

            // If holding creative staff: force infinite mana behavior (server-side)
            if (creativeStaff) {
                int maxMana = mana.bladebound$getMaxMana();
                if (maxMana > 0) {
                    mana.bladebound$setMana(maxMana);
                }
            } else {
                // Normal staff: consume mana normally (static-cost spells)
                if (spell.manaCost > 0) {
                    if (!mana.bladebound$tryConsumeMana(spell.manaCost)) {
                        user.sendMessage(Text.literal("Not enough mana!"), true);
                        return TypedActionResult.fail(stack);
                    }
                }
            }

            if (user instanceof ServerPlayerEntity sp) {
                ModPackets.sendMana(sp);
            }

            // Cast (may override cooldown in seconds)
            int cdSecondsOverride = spell.cast(world, user, FIREBALL_SPEED);

            int defaultCdSeconds = spell.cooldownTicks; // your field stores "seconds"
            int cdSeconds = (cdSecondsOverride > 0) ? cdSecondsOverride : defaultCdSeconds;

            // Creative staff: ALWAYS 0 cooldown
            int cooldownTicks = creativeStaff ? 0 : (Math.max(0, cdSeconds) * 20);

            // Normal staff: apply bracelet reduction
            if (!creativeStaff && cooldownTicks > 0) {
                ItemStack braceletStack = findEquippedCooldownBracelet(user);

                if (braceletStack.getItem() instanceof FixedCooldownBraceletItem) {
                    cooldownTicks = FixedCooldownBraceletItem.applyReductionToCooldownTicks(cooldownTicks);
                } else {
                    cooldownTicks = CooldownBraceletItem.applyReductionToCooldownTicks(
                            cooldownTicks,
                            braceletStack,
                            user.getRandom()
                    );
                }

            }

            user.getItemCooldownManager().set(this, cooldownTicks);

            // Send cooldown to client for the HUD countdown
            if (user instanceof ServerPlayerEntity sp) {
                ServerPlayNetworking.send(sp, new Payloads.StaffCooldownS2C(cooldownTicks));
            }

            // If creative staff: re-fill mana after cast (covers Perfect Heal draining it)
            if (creativeStaff) {
                int maxMana = mana.bladebound$getMaxMana();
                if (maxMana > 0) {
                    mana.bladebound$setMana(maxMana);
                }
                if (user instanceof ServerPlayerEntity sp) {
                    ModPackets.sendMana(sp);
                }
            }

            // Durability
            if (!user.getAbilities().creativeMode) {
                EquipmentSlot slot = (hand == Hand.MAIN_HAND) ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                stack.damage(DURABILITY_COST, user, slot);
            }
        }

        return TypedActionResult.success(stack, world.isClient);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("LEGENDARY").formatted(Formatting.GOLD, Formatting.BOLD));

        tooltip.add(Text.literal("A staff belonging to the great mage Frieren.").formatted(Formatting.AQUA, Formatting.ITALIC));
        tooltip.add(Text.literal("Carried through centuries of quiet journeys.").formatted(Formatting.LIGHT_PURPLE, Formatting.ITALIC));
        tooltip.add(Text.literal("Its magic feels calm, ancient, and endlessly patient.").formatted(Formatting.GRAY, Formatting.ITALIC));

        tooltip.add(Text.literal(" "));
        BladeboundBind.appendBindTooltip(stack, tooltip);
    }

}
