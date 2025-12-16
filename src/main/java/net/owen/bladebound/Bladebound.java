package net.owen.bladebound;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.loader.api.FabricLoader;
import net.owen.bladebound.command.BladeboundCommands;
import net.owen.bladebound.compat.AccessoryCompat;
import net.owen.bladebound.discipline.DisciplineEvents;
import net.owen.bladebound.effect.BladeboundEffects;
import net.owen.bladebound.effect.MurasamePoisonHandler;
import net.owen.bladebound.event.BladeboundJoinGifts;
import net.owen.bladebound.event.BladeboundLootInject;
import net.owen.bladebound.item.ModItemGroups;
import net.owen.bladebound.item.ModItems;
import net.owen.bladebound.worldgen.structure.BladeboundStructures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Bladebound implements ModInitializer {
	public static final String MOD_ID = "bladebound";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
    public void onInitialize() {
        // 1) Items FIRST (prevents the registry crash you had)
        BladeboundItems.init();
        ModItems.register();
        ModItemGroups.register();
        BladeboundBlocks.init();

        // 2) Systems
        DisciplineEvents.register();
        BladeboundConfig.load(FabricLoader.getInstance().getConfigDir());

        // 3) Commands (call the correct method name for your class)
        BladeboundCommands.init(); // <-- change this to whatever your BladeboundCommands actually has

        // 4) Worldgen + join gifts
        BladeboundStructures.init();
        BladeboundJoinGifts.init();

        // 5) Loot injection LAST
        BladeboundLootInject.init();
        AccessoryCompat.init();            // important
        MurasamePoisonHandler.init();      // poison logic
        BladeboundEffects.init();
    }

}