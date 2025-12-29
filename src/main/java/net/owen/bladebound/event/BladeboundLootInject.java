package net.owen.bladebound.event;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.util.Identifier;
import net.owen.bladebound.item.ModItems;

public final class BladeboundLootInject {

    private static final Identifier WITHER_SKELETON = Identifier.of("minecraft", "entities/wither_skeleton");
    private static final Identifier PIGLIN_BARTERING = Identifier.of("minecraft", "gameplay/piglin_bartering");

    // Stronghold chests
    private static final Identifier STRONGHOLD_LIBRARY  = Identifier.of("minecraft", "chests/stronghold_library");
    private static final Identifier STRONGHOLD_CORRIDOR = Identifier.of("minecraft", "chests/stronghold_corridor");
    private static final Identifier STRONGHOLD_CROSSING = Identifier.of("minecraft", "chests/stronghold_crossing");

    // Ancient City (ice chest only)
    private static final Identifier ANCIENT_CITY_ICE_BOX = Identifier.of("minecraft", "chests/ancient_city_ice_box");

    public static void init() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {

            // Wither skeleton: 85% chance to drop 1 shard
            if (WITHER_SKELETON.equals(key.getValue())) {
                LootPool pool = LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1))
                        .conditionally(RandomChanceLootCondition.builder(0.85f))
                        .with(ItemEntry.builder(ModItems.CURSED_KITETSU_SHARD))
                        .build();
                tableBuilder.pool(pool);
            }

            // Piglin bartering: rare chance (2%) to give 1 shard
            if (PIGLIN_BARTERING.equals(key.getValue())) {
                LootPool pool = LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1))
                        .conditionally(RandomChanceLootCondition.builder(0.02f))
                        .with(ItemEntry.builder(ModItems.CURSED_KITETSU_SHARD))
                        .build();
                tableBuilder.pool(pool);
            }

            // Stronghold corridor/crossing: cooldown bracelet
            if (STRONGHOLD_CORRIDOR.equals(key.getValue()) || STRONGHOLD_CROSSING.equals(key.getValue())) {
                LootPool pool = LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1))
                        .conditionally(RandomChanceLootCondition.builder(0.10f))
                        .with(ItemEntry.builder(ModItems.COOLDOWN_BRACELET))
                        .build();
                tableBuilder.pool(pool);
            }

            // Stronghold library: Mana Apples common
            if (STRONGHOLD_LIBRARY.equals(key.getValue())) {
                LootPool pool = LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1))
                        .conditionally(RandomChanceLootCondition.builder(0.55f))
                        .with(ItemEntry.builder(ModItems.MANA_APPLE)
                                .apply(SetCountLootFunction.builder(
                                        UniformLootNumberProvider.create(1.0f, 3.0f)
                                )))
                        .build();
                tableBuilder.pool(pool);
            }

            // Ancient City ice box ONLY: exclusive legendary spell roll
            // Total chance = 35% (20% Zoltraak, 15% Perfect Heal)
            if (ANCIENT_CITY_ICE_BOX.equals(key.getValue())) {
                LootPool pool = LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1))
                        .conditionally(RandomChanceLootCondition.builder(0.35f)) // 20% + 15%
                        // Weighted 20:15 so overall matches your desired rates
                        .with(ItemEntry.builder(ModItems.ZOLTRAAK_SPELL).weight(20))
                        .with(ItemEntry.builder(ModItems.PERFECT_HEAL_SPELL).weight(15))
                        .build();

                tableBuilder.pool(pool);
            }
        });
    }

    private BladeboundLootInject() {}
}
