package net.owen.bladebound.util;

import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class Advancements {
    private Advancements() {}

    public static void grant(ServerPlayerEntity player, String path) {
        if (player.getEntityWorld().getServer() == null) return;

        AdvancementEntry adv = player.getEntityWorld().getServer()
                .getAdvancementLoader()
                .get(Identifier.of("bladebound", path));

        if (adv == null) return;

        var progress = player.getAdvancementTracker().getProgress(adv);
        for (String criterion : progress.getUnobtainedCriteria()) {
            player.getAdvancementTracker().grantCriterion(adv, criterion);
        }
    }
}
