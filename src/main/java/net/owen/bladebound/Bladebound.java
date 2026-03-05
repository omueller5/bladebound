package net.owen.bladebound;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.loader.api.FabricLoader;
import net.owen.bladebound.command.BladeboundCommands;
import net.owen.bladebound.command.ManaCommands;
import net.owen.bladebound.compat.AccessoryCompat;
import net.owen.bladebound.discipline.DisciplineEvents;
import net.owen.bladebound.effect.BladeboundEffects;
import net.owen.bladebound.effect.MurasamePoisonHandler;
import net.owen.bladebound.entity.ModEntityAttributes;
import net.owen.bladebound.event.BladeboundJoinGifts;
import net.owen.bladebound.event.BladeboundLootInject;
import net.owen.bladebound.event.BladeboundTrades;
import net.owen.bladebound.event.PlayerRespawnCopy;
import net.owen.bladebound.item.ModItemGroups;
import net.owen.bladebound.item.ModItems;
import net.owen.bladebound.item.ModSpawnEggs;
import net.owen.bladebound.loot.ModLootConditions;
import net.owen.bladebound.magic.ancient.BlackHoleWardenDropHandler;
import net.owen.bladebound.network.ClientPackets;
import net.owen.bladebound.network.Payloads;
import net.owen.bladebound.network.ServerPackets;
import net.owen.bladebound.world.BossRoomSpawner;
import net.owen.bladebound.world.DungeonLootFixer;
import net.owen.bladebound.world.FrierenBossAnchorHandler;
import net.owen.bladebound.worldgen.structure.BladeboundStructures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bladebound implements ModInitializer {
	public static final String MOD_ID = "bladebound";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
    public void onInitialize() {
        // 1) Items
        ModItems.register();
        ModItemGroups.register();
        BladeboundBlocks.init();
        ModEntityAttributes.init();
        ModSpawnEggs.init();
        //BossRoomSpawner.init();
        //ModLootConditions.register();
        //FrierenBossAnchorHandler.register();
        //DungeonLootFixer.init();

        // 2) Systems
        DisciplineEvents.register();
        BladeboundConfig.load(FabricLoader.getInstance().getConfigDir());

        // 3) Commands
        BladeboundCommands.init();
        ManaCommands.register();

        // 4) Worldgen + join gifts
        BladeboundStructures.init();
        BladeboundJoinGifts.init();

        // 5) Loot injection
        BladeboundLootInject.init();
        BladeboundTrades.init();
        AccessoryCompat.init();
        MurasamePoisonHandler.init();
        BladeboundEffects.init();
        BlackHoleWardenDropHandler.init();

        Payloads.register();
        ClientPackets.register();
        ServerPackets.register();
        PlayerRespawnCopy.register();

    }
}