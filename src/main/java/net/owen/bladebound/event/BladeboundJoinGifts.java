package net.owen.bladebound.event;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.owen.bladebound.item.ModItems; // <-- use BladeboundItems if that's your class

public final class BladeboundJoinGifts {
    // One-time join marker
    private static final String TAG = "bladebound_got_codex";

    public static void init() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;

            // Already received codex
            if (player.getCommandTags().contains(TAG)) return;

            // ✅ Give NEW codex item (opens CodexScreen)
            player.giveItemStack(new ItemStack(ModItems.CODEX)); // <-- adjust field name if needed
            player.sendMessage(Text.literal("You received the Bladebound Codex."), false);

            // Grant root advancement (no commands)
            grantAdvancement(server, player, Identifier.of("bladebound", "root"));

            // Mark as done
            player.addCommandTag(TAG);
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
