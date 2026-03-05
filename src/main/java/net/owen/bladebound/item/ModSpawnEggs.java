package net.owen.bladebound.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.owen.bladebound.Bladebound;
import net.owen.bladebound.entity.ModEntities;

public class ModSpawnEggs {

    public static final Item FRIEREN_BOSS_SPAWN_EGG = Registry.register(
            Registries.ITEM,
            Identifier.of(Bladebound.MOD_ID, "frieren_boss_spawn_egg"),
            new SpawnEggItem(ModEntities.FRIEREN_BOSS, 0xD7C7B0, 0x5A6E8A, new Item.Settings())
    );

    public static void init() {
        // If you have a custom item group, add it there instead.
        // Example (uncomment & adjust to your group):
        // ItemGroupEvents.modifyEntriesEvent(ModItemGroups.BLADEBOUND).register(entries -> entries.add(FRIEREN_BOSS_SPAWN_EGG));
    }
}
