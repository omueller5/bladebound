package net.owen.bladebound.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModEntities {
    private ModEntities() {}

    public static final EntityType<BarrierEntity> BARRIER = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of("bladebound", "mana_barrier"),
            EntityType.Builder.<BarrierEntity>create(BarrierEntity::new, SpawnGroup.MISC)
                    .dimensions(0.01f, 0.01f)     // visual-only
                    .maxTrackingRange(128)        // blocks; enough so other players see it
                    .trackingTickInterval(1)      // updates frequently (it follows the player)
                    .build()
    );

    public static void register() {
        // no-op (static init registers), but keep for consistency
    }
}
