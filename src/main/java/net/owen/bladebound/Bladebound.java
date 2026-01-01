package net.owen.bladebound;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.owen.bladebound.command.BladeboundCommands;
import net.owen.bladebound.command.ManaCommands;
import net.owen.bladebound.compat.AccessoryCompat;
import net.owen.bladebound.discipline.DisciplineEvents;
import net.owen.bladebound.effect.BladeboundEffects;
import net.owen.bladebound.effect.MurasamePoisonHandler;
import net.owen.bladebound.entity.BarrierManager;
import net.owen.bladebound.entity.ModEntities;
import net.owen.bladebound.event.BladeboundJoinGifts;
import net.owen.bladebound.event.BladeboundLootInject;
import net.owen.bladebound.event.BladeboundTrades;
import net.owen.bladebound.event.PlayerRespawnCopy;
import net.owen.bladebound.item.ModItemGroups;
import net.owen.bladebound.item.ModItems;
import net.owen.bladebound.magic.BarrierManaDrain;
import net.owen.bladebound.magic.worldrewrite.WorldRewriteHooks;
import net.owen.bladebound.network.Payloads;
import net.owen.bladebound.network.ServerPackets;
import net.owen.bladebound.worldgen.structure.BladeboundStructures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bladebound implements ModInitializer {
    public static final String MOD_ID = "bladebound";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // ---------- Registries ----------
        ModItems.register();
        ModItemGroups.register();
        BladeboundBlocks.init();

        // Entities should be registered on common, before anything tries to spawn them
        ModEntities.register();

        // ---------- Config / Systems ----------
        BladeboundConfig.load(FabricLoader.getInstance().getConfigDir());
        DisciplineEvents.register();
        AccessoryCompat.init();
        BladeboundEffects.init();
        MurasamePoisonHandler.init();

        // ---------- Commands ----------
        BladeboundCommands.init();
        ManaCommands.register();

        // ---------- Networking ----------
        Payloads.register();
        ServerPackets.register();

        // ---------- Events ----------
        PlayerRespawnCopy.register();
        BladeboundJoinGifts.init();
        BladeboundTrades.init();

        // ---------- Worldgen / Loot ----------
        BladeboundStructures.init();
        BladeboundLootInject.init();

        // ---------- Barrier (server-side logic) ----------
        BarrierManager.register();
        BarrierManaDrain.register();

        // ---------- World Rewrite ----------
        WorldRewriteHooks.register();
    }
}
