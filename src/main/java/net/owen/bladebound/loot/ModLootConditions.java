package net.owen.bladebound.loot;

import com.mojang.serialization.MapCodec;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModLootConditions {

    public static final LootConditionType UNUSED_BOSS_LOCK_NEARBY = Registry.register(
            Registries.LOOT_CONDITION_TYPE,
            Identifier.of("bladebound", "unused_boss_lock_nearby"),
            new LootConditionType(UnusedBossLockNearbyCondition.CODEC)
    );

    public static final LootConditionType BOSS_KEY_CLAIM =
            register("boss_key_claim", BossKeyClaimCondition.CODEC);

    private ModLootConditions() {}

    private static LootConditionType register(String id, MapCodec<? extends LootCondition> codec) {
        return Registry.register(
                Registries.LOOT_CONDITION_TYPE,
                Identifier.of("bladebound", id),
                new LootConditionType(codec)
        );
    }

    public static void register() {
        // force class load
    }
}
