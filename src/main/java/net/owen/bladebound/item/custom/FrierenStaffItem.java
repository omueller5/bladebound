package net.owen.bladebound.item.custom;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.owen.bladebound.BladeboundBind;
import net.owen.bladebound.item.ModItems;
import net.owen.bladebound.magic.SpellCooldowns;
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
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        // Toggle-mode barrier: no shield animation
        return UseAction.NONE;
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        super.usageTick(world, user, stack, remainingUseTicks);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        // =========================================================
        // Barrier (Mana Barrier): CLICK TO TOGGLE ON/OFF (ID-based)
        // =========================================================
        if (user instanceof SpellHolder sh) {
            Identifier selId = sh.bladebound$getSelectedSpellId();
            StaffSpell selSpell = (selId == null) ? null : StaffSpell.fromId(selId);

            if (selSpell == StaffSpell.MANA_BARRIER) {
                if (!world.isClient) {
                    boolean creativeStaff = stack.isOf(ModItems.FRIEREN_STAFF_CREATIVE);

                    // Normal staff: require mana to turn ON (but always allow turning OFF)
                    if (!creativeStaff) {
                        if (!(user instanceof ManaHolder mh)) {
                            return TypedActionResult.fail(stack);
                        }

                        boolean turningOn = !sh.bladebound$isBarrierActive();
                        if (turningOn && mh.bladebound$getMana() <= 0) {
                            user.sendMessage(Text.literal("Not enough mana!"), true);
                            return TypedActionResult.fail(stack);
                        }
                    }

                    boolean nowActive = !sh.bladebound$isBarrierActive();
                    sh.bladebound$setBarrierActive(nowActive);

                    world.playSound(
                            null,
                            user.getBlockPos(),
                            SoundEvents.UI_BUTTON_CLICK.value(),
                            SoundCategory.PLAYERS,
                            0.6f,
                            nowActive ? 1.2f : 0.9f
                    );

                    if (user instanceof ServerPlayerEntity sp && user instanceof ManaHolder) {
                        ModPackets.sendMana(sp);
                    }
                }

                return TypedActionResult.success(stack, world.isClient);
            }
        }

        // =========================================================
        // Existing click-to-cast behavior for other spells
        // =========================================================
        if (user.isSneaking()) {
            if (!world.isClient) {
                user.sendMessage(Text.literal("Use the spell menu to select spells."), true);
                world.playSound(null, user.getBlockPos(), SoundEvents.UI_BUTTON_CLICK.value(),
                        SoundCategory.PLAYERS, 0.6F, 1.2F);
            }
            return TypedActionResult.success(stack, world.isClient);
        }

        if (!world.isClient) {
            boolean creativeStaff = stack.isOf(ModItems.FRIEREN_STAFF_CREATIVE);

            if (!(user instanceof SpellHolder spells)) {
                return TypedActionResult.fail(stack);
            }

            if (!(user instanceof ManaHolder mana)) {
                return TypedActionResult.fail(stack);
            }

            // Selected spell (ID-based)
            Identifier selectedId = spells.bladebound$getSelectedSpellId();
            if (selectedId == null) {
                user.sendMessage(Text.literal("No spell selected."), true);
                return TypedActionResult.fail(stack);
            }

            StaffSpell spell = StaffSpell.fromId(selectedId);
            if (spell == null) {
                user.sendMessage(Text.literal("Unknown spell selected."), true);
                return TypedActionResult.fail(stack);
            }

            // =========================================================
            // Spell-based cooldowns (key = StaffSpell.id)
            // =========================================================
            Identifier spellId = spell.id;

            // Creative staff bypasses cooldowns
            if (!creativeStaff && spells.bladebound$getSpellCooldown(spellId) > 0) {
                return TypedActionResult.fail(stack);
            }

            // =========================================================
            // Mana rules
            // =========================================================

            // Creative staff: force "infinite" mana server-side
            if (creativeStaff) {
                int maxMana = mana.bladebound$getMaxMana();
                if (maxMana > 0) mana.bladebound$setMana(maxMana);
            } else {
                // Normal staff: consume static-cost spells
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

            // =========================================================
            // Cast (may override cooldown in seconds)
            // =========================================================
            int cdSecondsOverride = spell.cast(world, user, FIREBALL_SPEED);

            int defaultCdSeconds = SpellCooldowns.getBaseCooldownSeconds(spell.id);
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

            // Apply spell cooldown (per-spell id), not item cooldown
            spells.bladebound$setSpellCooldown(spellId, cooldownTicks);

            // Send cooldown to client for the HUD countdown
            if (user instanceof ServerPlayerEntity sp) {
                ServerPlayNetworking.send(sp, new Payloads.SpellCooldownS2C(spell.id, cooldownTicks));
            }

            // Creative staff: refill mana after cast (covers Perfect Heal draining it)
            if (creativeStaff) {
                int maxMana = mana.bladebound$getMaxMana();
                if (maxMana > 0) mana.bladebound$setMana(maxMana);

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

    // === DO NOT REMOVE: Frieren Staff Lore ===
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
