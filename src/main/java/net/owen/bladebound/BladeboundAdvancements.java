package net.owen.bladebound;

import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * Helper for granting BladeBound advancements safely (MC/Fabric 1.21.1).
 */
public final class BladeboundAdvancements {
    private BladeboundAdvancements() {}

    private static AdvancementEntry getEntry(ServerPlayerEntity player, String path) {
        MinecraftServer server = player.getServer();
        if (server == null) return null;

        // In 1.21.x the loader returns AdvancementEntry
        return server.getAdvancementLoader().get(Identifier.of("bladebound", path));
    }

    public static boolean has(ServerPlayerEntity player, String path) {
        AdvancementEntry entry = getEntry(player, path);
        if (entry == null) return false;

        return player.getAdvancementTracker()
                .getProgress(entry)
                .isDone();
    }

    public static void grant(ServerPlayerEntity player, String path) {
        AdvancementEntry entry = getEntry(player, path);
        if (entry == null) return;

        var tracker = player.getAdvancementTracker();
        var progress = tracker.getProgress(entry);

        if (progress.isDone()) return;

        for (String criterion : progress.getUnobtainedCriteria()) {
            tracker.grantCriterion(entry, criterion);
        }
    }
}
