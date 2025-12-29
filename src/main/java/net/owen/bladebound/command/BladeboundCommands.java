package net.owen.bladebound.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.gen.chunk.ChunkGenerator;

import net.owen.bladebound.item.ModItems;
import net.owen.bladebound.mana.ManaHolder;
import net.owen.bladebound.worldgen.structure.ExcaliburChurchPiece;
import net.owen.bladebound.worldgen.structure.FrierenTowerPiece;
import net.owen.bladebound.worldgen.structure.MurasameShrinePiece;
import net.owen.bladebound.worldgen.structure.WadoDojoPiece;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class BladeboundCommands {

    public static void init() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> register(dispatcher)
        );
    }

    private static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("bladebound")

                /* ===============================
                   CODEX
                   =============================== */
                .then(literal("codex")
                        .requires(src -> src.hasPermissionLevel(2))
                        .executes(ctx -> {
                            var player = ctx.getSource().getPlayerOrThrow();
                            player.giveItemStack(ModItems.CODEX.getDefaultStack());
                            ctx.getSource().sendFeedback(
                                    () -> Text.literal("Given: Bladebound Codex"),
                                    false
                            );
                            return 1;
                        })
                )

                /* ===============================
                   INFINITE MANA (DEBUG)
                   /bladebound infinitemana [on|off]
                   =============================== */
                .then(literal("infinitemana")
                        .requires(src -> src.hasPermissionLevel(2))
                        // toggle
                        .executes(ctx -> {
                            ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                            ManaHolder mana = (ManaHolder) player;

                            boolean newValue = !mana.bladebound$hasInfiniteMana();
                            mana.bladebound$setInfiniteMana(newValue);

                            ctx.getSource().sendFeedback(
                                    () -> Text.literal("Bladebound Infinite Mana: " + (newValue ? "ON" : "OFF")),
                                    false
                            );
                            return 1;
                        })
                        // explicit on/off
                        .then(argument("state", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    builder.suggest("on");
                                    builder.suggest("off");
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> {
                                    ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                                    ManaHolder mana = (ManaHolder) player;

                                    String state = StringArgumentType.getString(ctx, "state").toLowerCase();
                                    boolean value;
                                    if (state.equals("on")) value = true;
                                    else if (state.equals("off")) value = false;
                                    else {
                                        ctx.getSource().sendError(Text.literal("Usage: /bladebound infinitemana [on|off]"));
                                        return 0;
                                    }

                                    mana.bladebound$setInfiniteMana(value);

                                    ctx.getSource().sendFeedback(
                                            () -> Text.literal("Bladebound Infinite Mana: " + (value ? "ON" : "OFF")),
                                            false
                                    );
                                    return 1;
                                })
                        )
                )

                /* ===============================
                   DEBUG SPAWN
                   =============================== */
                .then(literal("debugspawn")
                        .requires(src -> src.hasPermissionLevel(2))
                        .then(argument("which", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    builder.suggest("excalibur");
                                    builder.suggest("wado");
                                    builder.suggest("murasame");
                                    builder.suggest("frieren");
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> {
                                    ServerCommandSource src = ctx.getSource();
                                    ServerPlayerEntity player = src.getPlayerOrThrow();
                                    World world = player.getWorld();

                                    String which = StringArgumentType
                                            .getString(ctx, "which")
                                            .toLowerCase();

                                    BlockPos origin = player.getBlockPos().add(0, 0, 4);

                                    switch (which) {
                                        case "excalibur" -> spawnExcaliburChurch(world, origin);
                                        case "wado" -> spawnWadoDojo(world, origin);
                                        case "murasame" -> spawnMurasameShrine(world, origin);
                                        case "frieren" -> spawnFrierenTower(world, origin);

                                        default -> {
                                            src.sendError(Text.literal(
                                                    "Unknown option. Use: excalibur, wado, murasame, frieren"
                                            ));
                                            return 0;
                                        }
                                    }

                                    src.sendFeedback(
                                            () -> Text.literal(
                                                    "Spawned debug structure: " + which + " at " + origin
                                            ),
                                            false
                                    );
                                    return 1;
                                })
                        )
                )
        );
    }

    /* =========================================================
       EXCALIBUR — bladebound:excalibur_church
       ========================================================= */
    private static void spawnExcaliburChurch(World world, BlockPos origin) {
        if (!(world instanceof ServerWorld serverWorld)) return;
        generatePiece(serverWorld, new ExcaliburChurchPiece(origin), origin);
    }

    /* =========================================================
       WADO — bladebound:wado_dojo
       ========================================================= */
    private static void spawnWadoDojo(World world, BlockPos origin) {
        if (!(world instanceof ServerWorld serverWorld)) return;
        generatePiece(serverWorld, new WadoDojoPiece(origin), origin);
    }

    /* =========================================================
       MURASAME SHRINE — bladebound:murasame_shrine
       ========================================================= */
    private static void spawnMurasameShrine(World world, BlockPos origin) {
        if (!(world instanceof ServerWorld serverWorld)) return;
        generatePiece(serverWorld, new MurasameShrinePiece(origin), origin);
    }

    /* =========================================================
       FRIEREN TOWER — bladebound:frieren_tower
       ========================================================= */
    private static void spawnFrierenTower(World world, BlockPos origin) {
        if (!(world instanceof ServerWorld serverWorld)) return;
        generatePiece(serverWorld, new FrierenTowerPiece(origin), origin);
    }

    /* =========================================================
       Shared structure debug helper
       ========================================================= */
    private static void generatePiece(
            ServerWorld world,
            net.minecraft.structure.StructurePiece piece,
            BlockPos origin
    ) {
        var structureAccessor = world.getStructureAccessor();
        ChunkGenerator chunkGenerator = world.getChunkManager().getChunkGenerator();
        Random random = Random.create();

        // Oversized box so intersection checks never fail in debug
        BlockBox chunkBox = new BlockBox(
                origin.getX() - 256, origin.getY() - 128, origin.getZ() - 256,
                origin.getX() + 256, origin.getY() + 128, origin.getZ() + 256
        );

        ChunkPos chunkPos = new ChunkPos(origin);

        piece.generate(
                world,
                structureAccessor,
                chunkGenerator,
                random,
                chunkBox,
                chunkPos,
                origin
        );
    }
}
