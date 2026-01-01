package net.owen.bladebound.entity;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.owen.bladebound.item.ModItems;
import net.owen.bladebound.magic.SpellHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class BarrierManager {
    private BarrierManager() {}

    // data/bladebound/tags/items/staves.json
    private static final TagKey<net.minecraft.item.Item> STAVES_TAG = TagKey.of(
            RegistryKeys.ITEM,
            Identifier.of("bladebound", "staves")
    );

    private static final Map<UUID, BarrierEntity> ACTIVE = new HashMap<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(BarrierManager::tickServer);
    }

    private static void tickServer(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {

            // =========================================================
            // NEW condition: barrier is active (toggle mode)
            // =========================================================
            if (!((Object) player instanceof SpellHolder sh) || !sh.bladebound$isBarrierActive()) {
                despawnFor(player);
                continue;
            }

            // =========================================================
            // Keep your existing "must be holding a staff" rule
            // (If you want barrier to show even without staff, remove this block)
            // =========================================================
            ItemStack main = player.getMainHandStack();
            ItemStack off = player.getOffHandStack();

            boolean holdingStaff =
                    main.isIn(STAVES_TAG) || off.isIn(STAVES_TAG) ||
                            main.isOf(ModItems.FRIEREN_STAFF) || main.isOf(ModItems.FRIEREN_STAFF_CREATIVE) ||
                            off.isOf(ModItems.FRIEREN_STAFF) || off.isOf(ModItems.FRIEREN_STAFF_CREATIVE);

            if (!holdingStaff) {
                despawnFor(player);
                continue;
            }

            // =========================================================
            // Spawn / refresh entity
            // =========================================================
            UUID id = player.getUuid();
            BarrierEntity ent = ACTIVE.get(id);

            if (ent == null || ent.isRemoved()) {
                // NOTE: use your registered type's create path if you have it.
                ent = new BarrierEntity(ModEntities.BARRIER, player.getWorld());
                ent.setOwnerUuid(player.getUuid());

                Vec3d p = player.getEyePos();
                ent.setPos(p.x, p.y, p.z);

                player.getWorld().spawnEntity(ent);
                ACTIVE.put(id, ent);
            }

            // Keep it alive (BarrierEntity tick() will position it in front of player)
            ent.refreshLife();
        }

        // cleanup removed entries
        ACTIVE.entrySet().removeIf(e -> e.getValue() == null || e.getValue().isRemoved());
    }

    /** Call this when barrier breaks (mana hits 0) if you want instant despawn */
    public static void forceDespawn(ServerPlayerEntity player) {
        despawnFor(player);
    }

    private static void despawnFor(ServerPlayerEntity player) {
        UUID id = player.getUuid();
        BarrierEntity ent = ACTIVE.remove(id);
        if (ent != null && !ent.isRemoved()) {
            ent.discard();
        }
    }
}
