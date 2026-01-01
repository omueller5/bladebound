package net.owen.bladebound.event;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.owen.bladebound.mana.ManaHolder;
import net.owen.bladebound.network.ModPackets;

public final class PlayerRespawnCopy {

    private PlayerRespawnCopy() {}

    public static void register() {
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            if (!(oldPlayer instanceof ManaHolder oldMana)) return;
            if (!(newPlayer instanceof ManaHolder newMana)) return;

            int max = oldMana.bladebound$getMaxMana();
            int cur = oldMana.bladebound$getMana();

            newMana.bladebound$setMaxMana(max);
            newMana.bladebound$setMana(Math.min(cur, max));

            ModPackets.sendMana(newPlayer);
        });
    }
}
