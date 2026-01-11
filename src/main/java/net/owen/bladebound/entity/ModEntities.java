package net.owen.bladebound.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.owen.bladebound.Bladebound;
import net.owen.bladebound.entity.custom.BlackHoleEntity;

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

    public static final EntityType<BlackHoleEntity> BLACK_HOLE = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of("bladebound", "black_hole"),
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, BlackHoleEntity::new)
                    .dimensions(EntityDimensions.fixed(0.1f, 0.1f))
                    .trackRangeBlocks(96)
                    .trackedUpdateRate(1)
                    .build()
    );

    public static void register() {
        // no-op (static init registers), but keep for consistency
    }
}
