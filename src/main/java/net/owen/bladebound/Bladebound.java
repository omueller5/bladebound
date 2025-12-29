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
import net.owen.bladebound.event.BladeboundTrades;
import net.owen.bladebound.item.ModItemGroups;
import net.owen.bladebound.item.ModItems;
import net.owen.bladebound.network.ClientPackets;
import net.owen.bladebound.network.Payloads;
import net.owen.bladebound.network.ServerPackets;
import net.owen.bladebound.worldgen.structure.BladeboundStructures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;


public class Bladebound implements ModInitializer {
	public static final String MOD_ID = "bladebound";



    private static void debugTrinketsTagSafe(Item gauntletsItem) {

        Identifier itemId = Registries.ITEM.getId(gauntletsItem);
        TagKey<Item> handGlove = TagKey.of(RegistryKeys.ITEM, Identifier.of("trinkets", "hand/glove"));

        boolean inTag = Registries.ITEM.getEntry(gauntletsItem).isIn(handGlove);
    }

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
    public void onInitialize() {
        // 1) Items FIRST (prevents the registry crash you had)
        ModItems.register();
        ModItemGroups.register();
        BladeboundBlocks.init();

        // 2) Systems
        DisciplineEvents.register();
        BladeboundConfig.load(FabricLoader.getInstance().getConfigDir());

        // 3) Commands (call the correct method name for your class)
        BladeboundCommands.init();

        // 4) Worldgen + join gifts
        BladeboundStructures.init();
        BladeboundJoinGifts.init();

        // 5) Loot injection LAST
        BladeboundLootInject.init();
        BladeboundTrades.init();
        AccessoryCompat.init();
        MurasamePoisonHandler.init();
        BladeboundEffects.init();

        Payloads.register();
        ClientPackets.register();
        ServerPackets.register();

    }
}