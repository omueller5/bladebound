package net.owen.bladebound;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.owen.bladebound.block.custom.SwordInStoneBlock;
import net.owen.bladebound.item.ModItems;

public class BladeboundBlocks {

    public static final Block EMPTY_SWORD_STONE = registerBlock(
            "empty_sword_stone",
            new Block(FabricBlockSettings.create().strength(3.0f).requiresTool())
    );

    public static final Block EXCALIBUR_SWORD_IN_STONE = registerBlock(
            "excalibur_sword_in_stone",
            new SwordInStoneBlock(
                    FabricBlockSettings.create().strength(3.0f).requiresTool().nonOpaque(),
                    new ItemStack(ModItems.EXCALIBUR),
                    EMPTY_SWORD_STONE.getDefaultState()
            )
    );

    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, Identifier.of("bladebound", name), block);
    }

    private static void registerBlockItem(String name, Block block) {
        Registry.register(
                Registries.ITEM,
                Identifier.of("bladebound", name),
                new BlockItem(block, new Item.Settings())
        );
    }

    public static void init() {
        // call this from your mod initializer to ensure class loads
    }
}
