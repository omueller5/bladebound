package net.owen.bladebound.magic.ancient;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.owen.bladebound.mana.ManaHolder;
import net.owen.bladebound.magic.worldrewrite.WorldRewriteZoneManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class WorldRewriteSpell {

    public static final Identifier ID = Identifier.of("bladebound", "world_rewrite_spell");

    public static final double RADIUS = 7.0;
    public static final int DURATION_TICKS = 20 * 20;

    public static final int MANA_COST = 500;
    public static final int COOLDOWN_TICKS = 15 * 60 * 20;

    private static final Identifier CREATIVE_STAFF_ID = Identifier.of("bladebound", "creative_staff");

    private static final Map<UUID, Long> NEXT_ALLOWED_TICK = new HashMap<>();
    private static final Map<UUID, Long> LAST_SEEN_WORLD_TIME = new HashMap<>();

    private WorldRewriteSpell() {}

    public static boolean cast(ServerWorld world, ServerPlayerEntity caster) {
        boolean exempt = isHoldingCreativeStaff(caster.getMainHandStack());
        return cast(world, caster, exempt);
    }

    public static boolean cast(ServerWorld world, ServerPlayerEntity caster, boolean exemptFromCosts) {
        if (WorldRewriteZoneManager.hasActiveZone(world)) {
            caster.sendMessage(Text.literal("Reality is already being rewritten."), true);
            return false;
        }

        long now = world.getTime();

        // Fix: if world time goes backwards (new world / restart), clear stored cooldown for this player.
        long last = LAST_SEEN_WORLD_TIME.getOrDefault(caster.getUuid(), now);
        if (now < last) {
            NEXT_ALLOWED_TICK.remove(caster.getUuid());
        }
        LAST_SEEN_WORLD_TIME.put(caster.getUuid(), now);

        if (!exemptFromCosts) {
            long next = NEXT_ALLOWED_TICK.getOrDefault(caster.getUuid(), 0L);
            if (now < next) {
                long remainingTicks = next - now;
                int seconds = (int) Math.ceil(remainingTicks / 20.0);

                int mm = seconds / 60;
                int ss = seconds % 60;
                String s = mm > 0 ? (mm + "m " + ss + "s") : (ss + "s");

                caster.sendMessage(Text.literal("World Rewrite on cooldown: " + s), true);
                return false;
            }
        }

        if (!exemptFromCosts) {
            if (!(caster instanceof ManaHolder mana)) return false;

            int currentMana = mana.bladebound$getMana();
            if (currentMana < MANA_COST) {
                caster.sendMessage(Text.literal("Not enough mana (" + currentMana + "/" + MANA_COST + ")."), true);
                return false;
            }

            mana.bladebound$setMana(currentMana - MANA_COST);
        }

        WorldRewriteZoneManager.start(world, caster.getUuid(), new net.minecraft.util.math.Vec3d(caster.getX(), caster.getY(), caster.getZ()), RADIUS, DURATION_TICKS);

        if (!exemptFromCosts) {
            NEXT_ALLOWED_TICK.put(caster.getUuid(), now + COOLDOWN_TICKS);
        }

        caster.sendMessage(Text.literal("World Rewrite activated."), true);
        return true;
    }

    private static boolean isHoldingCreativeStaff(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Identifier id = Registries.ITEM.getId(stack.getItem());
        return CREATIVE_STAFF_ID.equals(id);
    }
}
