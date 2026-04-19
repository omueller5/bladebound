package net.owen.bladebound.item;

import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.owen.bladebound.Bladebound;
import net.owen.bladebound.entity.ModEntities;

public class ModSpawnEggs {

    public static final Item FRIEREN_BOSS_SPAWN_EGG = Registry.register(
            Registries.ITEM,
            RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Bladebound.MOD_ID, "frieren_boss_spawn_egg")),
            new SpawnEggItem(
                    new Item.Settings()
                            .registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Bladebound.MOD_ID, "frieren_boss_spawn_egg")))
                            .spawnEgg(ModEntities.FRIEREN_BOSS)
            )
    );

    public static void init() {
    }
}
