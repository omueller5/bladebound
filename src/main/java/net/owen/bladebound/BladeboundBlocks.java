package net.owen.bladebound;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.owen.bladebound.block.custom.SwordInStoneBlock;

public class BladeboundBlocks {

    public static final Block SWORD_IN_STONE = Registry.register(
            Registries.BLOCK,
            Identifier.of("bladebound", "sword-in-stone"),
            new SwordInStoneBlock(
                    FabricBlockSettings.copyOf(Blocks.COBBLESTONE),
                    true
            )
    );

    public static final Item SWORD_IN_STONE_ITEM = Registry.register(
            Registries.ITEM,
            Identifier.of("bladebound", "sword-in-stone"),
            new BlockItem(SWORD_IN_STONE, new Item.Settings())
    );

    public static void init() {
        // Force class loading so static fields register
    }
}
