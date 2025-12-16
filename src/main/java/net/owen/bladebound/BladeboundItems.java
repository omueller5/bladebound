package net.owen.bladebound;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.owen.bladebound.item.BladeboundCodexItem;

public class BladeboundItems {

    public static final Item BLADEBOUND_CODEX = Registry.register(
            Registries.ITEM,
            Identifier.of("bladebound", "bladebound_codex"),
            new BladeboundCodexItem(new Item.Settings().maxCount(1))
    );


    public static final Item CURSED_KITETSU_SHARD =
            Registry.register(
                    Registries.ITEM,
                    Identifier.of("bladebound", "cursed-kitetsu-shard"),
                    new Item(new Item.Settings().rarity(Rarity.UNCOMMON))
            );

    public static void init() {
        // Forces class loading so static registration runs early
    }
}
