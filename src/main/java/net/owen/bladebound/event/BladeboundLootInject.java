package net.owen.bladebound.event;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.util.Identifier;
import net.owen.bladebound.BladeboundItems;

public final class BladeboundLootInject {

    private static final Identifier WITHER_SKELETON = Identifier.of("minecraft", "entities/wither_skeleton");
    private static final Identifier PIGLIN_BARTERING = Identifier.of("minecraft", "gameplay/piglin_bartering");

    public static void init() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {

            // Wither skeleton: 85% chance to drop 1 shard
            if (WITHER_SKELETON.equals(key.getValue())) {
                LootPool pool = LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1))
                        .conditionally(RandomChanceLootCondition.builder(0.85f))
                        .with(ItemEntry.builder(BladeboundItems.CURSED_KITETSU_SHARD))
                        .build();

                tableBuilder.pool(pool);
            }

            // Piglin bartering: rare chance (2%) to give 1 shard
            if (PIGLIN_BARTERING.equals(key.getValue())) {
                LootPool pool = LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1))
                        .conditionally(RandomChanceLootCondition.builder(0.02f))
                        .with(ItemEntry.builder(BladeboundItems.CURSED_KITETSU_SHARD))
                        .build();

                tableBuilder.pool(pool);
            }
        });
    }

    private BladeboundLootInject() {}
}
