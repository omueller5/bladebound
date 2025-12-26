package net.owen.bladebound;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
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
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;


public class Bladebound implements ModInitializer {
	public static final String MOD_ID = "bladebound";



    private static void debugTrinketsTagSafe(Item gauntletsItem) {
        // Don’t crash if Trinkets isn’t installed
        if (!FabricLoader.getInstance().isModLoaded("trinkets")) {
            LOGGER.info("[Bladebound] Trinkets not installed; skipping glove tag debug.");
            return;
        }

        Identifier itemId = Registries.ITEM.getId(gauntletsItem);
        TagKey<Item> handGlove = TagKey.of(RegistryKeys.ITEM, Identifier.of("trinkets", "hand/glove"));

        boolean inTag = Registries.ITEM.getEntry(gauntletsItem).isIn(handGlove);

        LOGGER.info("[Bladebound] Gauntlets id = {}", itemId);
        LOGGER.info("[Bladebound] In trinkets:hand/glove tag? {}", inTag);
    }




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
        AccessoryCompat.init();
        MurasamePoisonHandler.init();
        BladeboundEffects.init();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            debugTrinketsTagSafe(ModItems.MURASAME_GAUNTLETS);
        });


    }

}