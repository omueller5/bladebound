package net.owen.bladebound.magic.ancient;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.owen.bladebound.item.ModItems;

public final class BlackHoleWardenDropHandler {

    private static final String TAG = "bladebound_black_hole_learned_drop";

    private BlackHoleWardenDropHandler() {}

    public static void init() {
        ServerLivingEntityEvents.AFTER_DEATH.register(BlackHoleWardenDropHandler::afterDeath);
    }

    private static void afterDeath(LivingEntity entity, net.minecraft.entity.damage.DamageSource source) {
        if (!(entity instanceof WardenEntity)) return;

        // Only reward the player who actually killed it
        if (!(source.getAttacker() instanceof ServerPlayerEntity sp)) return;

        // One-time per player (across the whole world)
        if (sp.getCommandTags().contains(TAG)) return;

        sp.addCommandTag(TAG);

        // Drop / give the "learning item"
        // You decide what the learning item is called; example: BLACK_HOLE_SPELL
        sp.giveItemStack(ModItems.BLACK_HOLE_SPELL.getDefaultStack());

        sp.sendMessage(Text.literal("You obtained an Ancient Grimoire: Black Hole"), false);
    }
}
