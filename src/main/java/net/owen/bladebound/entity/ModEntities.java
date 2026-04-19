package net.owen.bladebound.entity;

import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.owen.bladebound.Bladebound;
import net.owen.bladebound.entity.custom.BlackHoleEntity;
import net.owen.bladebound.entity.custom.FrierenBossEntity;

public final class ModEntities {
    private ModEntities() {}

    private static Identifier id(String path) {
        return Identifier.of(Bladebound.MOD_ID, path);
    }

    private static <T extends net.minecraft.entity.Entity> EntityType<T> registerEntity(String path, EntityType<T> type) {
        RegistryKey<EntityType<?>> key = RegistryKey.of(RegistryKeys.ENTITY_TYPE, id(path));
        return Registry.register(Registries.ENTITY_TYPE, key, type);
    }

    public static final EntityType<BarrierEntity> BARRIER = registerEntity(
            "mana_barrier",
            EntityType.Builder.<BarrierEntity>create(BarrierEntity::new, SpawnGroup.MISC)
                    .dimensions(0.01f, 0.01f)
                    .maxTrackingRange(128)
                    .trackingTickInterval(1)
                    .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, id("mana_barrier")))
    );

    public static final EntityType<BlackHoleEntity> BLACK_HOLE = registerEntity(
            "black_hole",
            EntityType.Builder.<BlackHoleEntity>create(BlackHoleEntity::new, SpawnGroup.MISC)
                    .dimensions(0.1f, 0.1f)
                    .maxTrackingRange(96)
                    .trackingTickInterval(1)
                    .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, id("black_hole")))
    );

    public static final EntityType<FrierenBossEntity> FRIEREN_BOSS = registerEntity(
            "frieren_boss",
            EntityType.Builder.create(FrierenBossEntity::new, SpawnGroup.MONSTER)
                    .dimensions(0.6f, 1.8f)
                    .maxTrackingRange(64)
                    .trackingTickInterval(3)
                    .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, id("frieren_boss")))
    );

    public static void register() {
        // no-op (static init registers), but keep for consistency
    }
}
