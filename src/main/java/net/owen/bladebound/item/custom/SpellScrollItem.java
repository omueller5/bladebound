package net.owen.bladebound.item.custom;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.owen.bladebound.magic.StaffSpell;
import net.owen.bladebound.mana.ManaHolder;

public class SpellScrollItem extends Item {

    private final StaffSpell spell;

    // ------------------------------------------------------------
    // Supports: new SpellScrollItem(settings, StaffSpell.FIREBOLT)
    // ------------------------------------------------------------
    public SpellScrollItem(Settings settings, StaffSpell spell) {
        super(settings);
        this.spell = spell;
    }

    // ------------------------------------------------------------
    // Supports your current calls:
    // new SpellScrollItem(settings, Identifier.of("bladebound","firebolt"))
    // ------------------------------------------------------------
    public SpellScrollItem(Settings settings, Identifier spellId) {
        super(settings);
        this.spell = StaffSpell.fromId(spellId);
    }

    // ------------------------------------------------------------
    // Right click → cast spell → consume scroll
    // ------------------------------------------------------------
    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient() && world instanceof ServerWorld sw) {

            // Mana check (scrolls still consume mana, no cooldown)
            if (spell.manaCost > 0) {
                if (!(user instanceof ManaHolder mana)) {
                    return ActionResult.FAIL;
                }

                int current = mana.bladebound$getMana();
                if (current < spell.manaCost) {
                    user.sendMessage(
                            Text.literal("Not enough mana (" + current + "/" + spell.manaCost + ").")
                                    .formatted(Formatting.RED),
                            true
                    );
                    return ActionResult.FAIL;
                }

                mana.bladebound$setMana(current - spell.manaCost);
            }

            // Cast spell (your StaffSpell already blocks WorldRewrite zones etc.)
            spell.cast(world, user, 1.0);

            // Small generic “scroll used” effect (same for all tiers)
            playScrollUseFx(sw, user);

            // Consume scroll (creative keeps it)
            if (!user.getAbilities().creativeMode) {
                stack.decrement(1);
            }
        }

        return ActionResult.SUCCESS;
    }

    private static void playScrollUseFx(ServerWorld sw, PlayerEntity user) {
        sw.playSound(
                null,
                user.getX(), user.getY(), user.getZ(),
                SoundEvents.ITEM_BOOK_PAGE_TURN,
                SoundCategory.PLAYERS,
                0.9f,
                1.15f
        );

        Vec3d eye = user.getEyePos();
        Vec3d look = user.getRotationVec(1.0f).normalize();
        Vec3d p = eye.add(look.multiply(0.35));

        sw.spawnParticles(
                ParticleTypes.ENCHANT,
                p.x, p.y, p.z,
                16,
                0.20, 0.20, 0.20,
                0.01
        );

        sw.spawnParticles(
                ParticleTypes.END_ROD,
                p.x, p.y, p.z,
                4,
                0.08, 0.08, 0.08,
                0.005
        );
    }

    // ------------------------------------------------------------
    // Colored item name by rarity (NO TooltipContext)
    // ------------------------------------------------------------
    @Override
    public Text getName(ItemStack stack) {
        Formatting color = switch (spell.rarity) {
            case STARTER -> Formatting.GREEN;
            case RARE -> Formatting.AQUA;
            case LEGENDARY -> Formatting.LIGHT_PURPLE;
            case ANCIENT -> Formatting.RED;
        };

        return Text.literal(spell.displayName + " Scroll").formatted(color);
    }
}
