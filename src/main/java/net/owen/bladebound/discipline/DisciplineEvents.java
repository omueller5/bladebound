package net.owen.bladebound.discipline;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

import java.util.UUID;

public final class DisciplineEvents {
    private DisciplineEvents() {}

    // Cooldowns (ms)
    private static final long HIT_COOLDOWN_MS = 1000;   // 1 second
    private static final long HURT_COOLDOWN_MS = 2000;  // 2 seconds

    public static void register() {

        // Hit entity (+1, cooldown)
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient()) return ActionResult.PASS;
            if (!(player instanceof ServerPlayerEntity sp)) return ActionResult.PASS;
            if (hand != Hand.MAIN_HAND) return ActionResult.PASS;

            ItemStack stack = sp.getMainHandStack();
            if (stack.isEmpty()) return ActionResult.PASS;

            // Only apply to your BladeBound swords:
            if (!isBladeboundSword(stack)) return ActionResult.PASS;

            bindIfNeeded(sp, stack);

            if (!isOwner(sp, stack)) return ActionResult.PASS;

            long now = System.currentTimeMillis();
            long last = DisciplineData.getLastHitMs(stack);
            if (now - last < HIT_COOLDOWN_MS) return ActionResult.PASS;

            DisciplineData.setLastHitMs(stack, now);
            addDisciplineAndMaybeNotify(sp, stack, 1);

            return ActionResult.PASS;
        });

        // Kill entity (+5)
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(damageSource.getAttacker() instanceof ServerPlayerEntity sp)) return;
            ItemStack stack = sp.getMainHandStack();
            if (stack.isEmpty()) return;

            if (!isBladeboundSword(stack)) return;

            bindIfNeeded(sp, stack);
            if (!isOwner(sp, stack)) return;

            // Only count living kills that are not the player
            if (!(entity instanceof LivingEntity)) return;

            addDisciplineAndMaybeNotify(sp, stack, 5);
        });

        // Take damage while holding sword (+1, cooldown)
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (!(entity instanceof ServerPlayerEntity sp)) return true;

            ItemStack stack = sp.getMainHandStack();
            if (stack.isEmpty()) return true;
            if (!isBladeboundSword(stack)) return true;

            bindIfNeeded(sp, stack);
            if (!isOwner(sp, stack)) return true;

            long now = System.currentTimeMillis();
            long last = DisciplineData.getLastHurtMs(stack);
            if (now - last < HURT_COOLDOWN_MS) return true;

            DisciplineData.setLastHurtMs(stack, now);
            addDisciplineAndMaybeNotify(sp, stack, 1);

            return true;
        });
    }

    private static void bindIfNeeded(ServerPlayerEntity player, ItemStack stack) {
        if (!DisciplineData.isBound(stack)) {
            DisciplineData.bindTo(stack, player.getUuid());
            player.sendMessage(Text.literal("§d[BladeBound]§r This blade binds to you."), true);
        }
    }

    private static boolean isOwner(ServerPlayerEntity player, ItemStack stack) {
        UUID owner = DisciplineData.getOwner(stack);
        return owner != null && owner.equals(player.getUuid());
    }

    private static void addDisciplineAndMaybeNotify(ServerPlayerEntity player, ItemStack stack, int amount) {
        int beforeRank = DisciplineData.getRank(stack);
        DisciplineData.addPoints(stack, amount);
        int afterRank = DisciplineData.getRank(stack);

        if (afterRank > beforeRank) {
            player.sendMessage(Text.literal(
                    "§d[BladeBound]§r Discipline Rank Up: §f" + DisciplineData.getRankName(afterRank) +
                            "§7 (Rank " + afterRank + ")"
            ), true);

            // IMPORTANT: You’ll apply the actual bonuses in your sword item class (below)
            // or via attribute modifiers.
        }
    }

    /**
     * Replace this with your real check.
     * Best option: check stack.getItem() == ModItems.WADO_ICHIMONJI, etc.
     */
    private static boolean isBladeboundSword(ItemStack stack) {
        String id = stack.getItem().toString().toLowerCase();
        return id.contains("wado") || id.contains("bladebound"); // <-- TEMP fallback
    }
}
