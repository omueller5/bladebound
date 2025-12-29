package net.owen.bladebound.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.owen.bladebound.mana.ManaHolder;
import net.owen.bladebound.network.ModPackets;

public final class ManaCommands {

    private ManaCommands() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register(ManaCommands::registerCommands);
    }

    private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher,
                                         CommandRegistryAccess registryAccess,
                                         CommandManager.RegistrationEnvironment env) {

        dispatcher.register(CommandManager.literal("bladebound")
                .then(CommandManager.literal("setmaxmana")
                        .requires(src -> src.hasPermissionLevel(2))
                        .then(CommandManager.argument("amount", IntegerArgumentType.integer(0, 1_000_000))
                                .executes(ctx -> {
                                    ServerCommandSource src = ctx.getSource();
                                    ServerPlayerEntity player = src.getPlayerOrThrow();
                                    int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                    return setMaxMana(src, player, amount);
                                })
                                .then(CommandManager.argument("player", net.minecraft.command.argument.EntityArgumentType.player())
                                        .executes(ctx -> {
                                            ServerCommandSource src = ctx.getSource();
                                            ServerPlayerEntity player = net.minecraft.command.argument.EntityArgumentType.getPlayer(ctx, "player");
                                            int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                            return setMaxMana(src, player, amount);
                                        })
                                )
                        )
                )
                .then(CommandManager.literal("setmana")
                        .requires(src -> src.hasPermissionLevel(2))
                        .then(CommandManager.argument("amount", IntegerArgumentType.integer(0, 1_000_000))
                                .executes(ctx -> {
                                    ServerCommandSource src = ctx.getSource();
                                    ServerPlayerEntity player = src.getPlayerOrThrow();
                                    int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                    return setMana(src, player, amount);
                                })
                                .then(CommandManager.argument("player", net.minecraft.command.argument.EntityArgumentType.player())
                                        .executes(ctx -> {
                                            ServerCommandSource src = ctx.getSource();
                                            ServerPlayerEntity player = net.minecraft.command.argument.EntityArgumentType.getPlayer(ctx, "player");
                                            int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                            return setMana(src, player, amount);
                                        })
                                )
                        )
                )
        );
    }

    private static int setMaxMana(ServerCommandSource src, ServerPlayerEntity player, int amount) {
        if (!(player instanceof ManaHolder mana)) {
            src.sendError(Text.literal("Player does not implement ManaHolder."));
            return 0;
        }

        mana.bladebound$setMaxMana(amount);

        // Optional: also refill to new max for convenience in testing
        mana.bladebound$setMana(amount);

        ModPackets.sendMana(player);

        src.sendFeedback(() -> Text.literal("Set max mana to " + amount + " for " + player.getName().getString()), true);
        return 1;
    }

    private static int setMana(ServerCommandSource src, ServerPlayerEntity player, int amount) {
        if (!(player instanceof ManaHolder mana)) {
            src.sendError(Text.literal("Player does not implement ManaHolder."));
            return 0;
        }

        int max = mana.bladebound$getMaxMana();
        int clamped = Math.min(amount, Math.max(0, max));

        mana.bladebound$setMana(clamped);
        ModPackets.sendMana(player);

        src.sendFeedback(() -> Text.literal("Set mana to " + clamped + " for " + player.getName().getString()), true);
        return 1;
    }
}
