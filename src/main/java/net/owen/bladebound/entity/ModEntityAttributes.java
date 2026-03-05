package net.owen.bladebound.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.owen.bladebound.entity.custom.FrierenBossEntity;

public class ModEntityAttributes {

    public static void init() {
        FabricDefaultAttributeRegistry.register(ModEntities.FRIEREN_BOSS, FrierenBossEntity.createAttributes());
    }
}
