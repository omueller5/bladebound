package net.owen.bladebound;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.owen.bladebound.block.custom.*;

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

    public static final Block FRIEREN_BOSS_ANCHOR = Registry.register(
            Registries.BLOCK,
            Identifier.of("bladebound", "frieren_boss_anchor"),
            new FrierenBossAnchorBlock(
                    FabricBlockSettings.copyOf(Blocks.AIR)
                            .strength(-1.0F, 3600000.0F)
            )
    );


    public static final Block FRIEREN_BOSS_ANCHOR_BUILDER = Registry.register(
            Registries.BLOCK,
            Identifier.of("bladebound", "frieren_boss_anchor_builder"),
            new FrierenBossAnchorBuilderBlock(
                    FabricBlockSettings.create()
                            .noCollision()
                            .dropsNothing()
                            .strength(-1.0F, 3600000.0F)
            )
    );

    public static final Item FRIEREN_BOSS_ANCHOR_BUILDER_ITEM = Registry.register(
            Registries.ITEM,
            Identifier.of("bladebound", "frieren_boss_anchor_builder"),
            new BlockItem(FRIEREN_BOSS_ANCHOR_BUILDER, new Item.Settings())
    );

    public static final Block BOSS_LOCK = Registry.register(
            Registries.BLOCK,
            Identifier.of("bladebound", "boss_lock"),
            new BossLockBlock(
                    FabricBlockSettings.create()
                            .strength(-1.0F, 3600000.0F).requiresTool()
                            .dropsNothing()
            )
    );

    public static final Item BOSS_LOCK_ITEM = Registry.register(
            Registries.ITEM,
            Identifier.of("bladebound", "boss_lock"),
            new BlockItem(BOSS_LOCK, new Item.Settings())
    );

    public static final Block DUNGEON_DOOR = Registry.register(
            Registries.BLOCK,
            Identifier.of("bladebound", "dungeon_door"),
            new DungeonDoorBlock(
                    FabricBlockSettings.create()
                            .strength(-1.0F, 3600000.0F) // unbreakable + blast resistant
                            .requiresTool()
            )
    );

    public static final Item DUNGEON_DOOR_ITEM = Registry.register(
            Registries.ITEM,
            Identifier.of("bladebound", "dungeon_door"),
            new BlockItem(DUNGEON_DOOR, new Item.Settings())
    );

    public static final Block DUNGEON_BRICKS = Registry.register(
            Registries.BLOCK,
            Identifier.of("bladebound", "dungeon_bricks"),
            new DungeonBrickBlock(
                    FabricBlockSettings.copyOf(Blocks.STONE_BRICKS)
                            .strength(-1.0F, 3600000.0F)
                            .dropsNothing()
            )
    );

    public static final Item DUNGEON_BRICKS_ITEM = Registry.register(
            Registries.ITEM,
            Identifier.of("bladebound", "dungeon_bricks"),
            new BlockItem(BladeboundBlocks.DUNGEON_BRICKS, new Item.Settings())
    );

    public static void init() {
        // Force class loading so static fields register
    }
}
