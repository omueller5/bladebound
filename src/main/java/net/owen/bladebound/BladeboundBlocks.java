package net.owen.bladebound;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.owen.bladebound.block.custom.*;

public class BladeboundBlocks {

    private static Identifier id(String path) {
        return Identifier.of(Bladebound.MOD_ID, path);
    }

    private static AbstractBlock.Settings blockSettings(String path, AbstractBlock.Settings settings) {
        return settings.registryKey(RegistryKey.of(RegistryKeys.BLOCK, id(path)));
    }

    private static Item.Settings blockItemSettings(String path) {
        return new Item.Settings()
                .useBlockPrefixedTranslationKey()
                .registryKey(RegistryKey.of(RegistryKeys.ITEM, id(path)));
    }

    private static Block registerBlock(String path, Block block) {
        RegistryKey<Block> key = RegistryKey.of(RegistryKeys.BLOCK, id(path));
        return Registry.register(Registries.BLOCK, key, block);
    }

    private static Item registerBlockItem(String path, Block block) {
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id(path));
        return Registry.register(Registries.ITEM, key, new BlockItem(block, blockItemSettings(path)));
    }

    public static final Block SWORD_IN_STONE = registerBlock(
            "sword-in-stone",
            new SwordInStoneBlock(
                    blockSettings("sword-in-stone", AbstractBlock.Settings.copy(Blocks.COBBLESTONE)),
                    true
            )
    );

    public static final Item SWORD_IN_STONE_ITEM = registerBlockItem("sword-in-stone", SWORD_IN_STONE);

    public static final Block FRIEREN_BOSS_ANCHOR = registerBlock(
            "frieren_boss_anchor",
            new FrierenBossAnchorBlock(
                    blockSettings(
                            "frieren_boss_anchor",
                            AbstractBlock.Settings.copy(Blocks.AIR)
                                    .strength(-1.0F, 3600000.0F)
                    )
            )
    );

    public static final Block FRIEREN_BOSS_ANCHOR_BUILDER = registerBlock(
            "frieren_boss_anchor_builder",
            new FrierenBossAnchorBuilderBlock(
                    blockSettings(
                            "frieren_boss_anchor_builder",
                            AbstractBlock.Settings.create()
                                    .noCollision()
                                    .dropsNothing()
                                    .strength(-1.0F, 3600000.0F)
                    )
            )
    );

    public static final Item FRIEREN_BOSS_ANCHOR_BUILDER_ITEM = registerBlockItem("frieren_boss_anchor_builder", FRIEREN_BOSS_ANCHOR_BUILDER);

    public static final Block BOSS_LOCK = registerBlock(
            "boss_lock",
            new BossLockBlock(
                    blockSettings(
                            "boss_lock",
                            AbstractBlock.Settings.create()
                                    .strength(-1.0F, 3600000.0F)
                                    .requiresTool()
                                    .dropsNothing()
                    )
            )
    );

    public static final Item BOSS_LOCK_ITEM = registerBlockItem("boss_lock", BOSS_LOCK);

    public static final Block DUNGEON_DOOR = registerBlock(
            "dungeon_door",
            new DungeonDoorBlock(
                    blockSettings(
                            "dungeon_door",
                            AbstractBlock.Settings.create()
                                    .strength(-1.0F, 3600000.0F)
                                    .requiresTool()
                    )
            )
    );

    public static final Item DUNGEON_DOOR_ITEM = registerBlockItem("dungeon_door", DUNGEON_DOOR);

    public static final Block DUNGEON_BRICKS = registerBlock(
            "dungeon_bricks",
            new DungeonBrickBlock(
                    blockSettings(
                            "dungeon_bricks",
                            AbstractBlock.Settings.copy(Blocks.STONE_BRICKS)
                                    .strength(-1.0F, 3600000.0F)
                                    .dropsNothing()
                    )
            )
    );

    public static final Item DUNGEON_BRICKS_ITEM = registerBlockItem("dungeon_bricks", DUNGEON_BRICKS);

    public static void init() {
        // Force class loading so static fields register
    }
}
