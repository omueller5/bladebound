package net.owen.bladebound.event;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.owen.bladebound.item.BladeboundCodexBook;

public final class BladeboundJoinGifts {
    // Use a simple tag string (no spaces). Colons usually work, but keeping it simple avoids edge weirdness.
    private static final String TAG = "bladebound_got_codex";

    public static void init() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;

            // ✅ Yarn 1.21+: command tags
            if (player.getCommandTags().contains(TAG)) return; // :contentReference[oaicite:3]{index=3}

            // Give codex
            player.giveItemStack(BladeboundCodexBook.create());
            player.sendMessage(Text.literal("You received the Bladebound Codex."), false);

            // Grant root advancement (no commands)
            grantAdvancement(server, player, Identifier.of("bladebound", "root"));

            // Mark as done
            player.addCommandTag(TAG); // :contentReference[oaicite:4]{index=4}
        });
    }

    private static void grantAdvancement(MinecraftServer server, ServerPlayerEntity player, Identifier advancementId) {
        AdvancementEntry entry = server.getAdvancementLoader().get(advancementId);
        if (entry == null) {
            System.out.println("[Bladebound] Could not find advancement: " + advancementId);
            return;
        }

        var tracker = player.getAdvancementTracker();
        var advancement = entry.value();

        for (String criterion : advancement.criteria().keySet()) {
            tracker.grantCriterion(entry, criterion);
        }
    }

    private BladeboundJoinGifts() {}
}
