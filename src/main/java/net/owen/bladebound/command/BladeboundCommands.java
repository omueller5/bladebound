package net.owen.bladebound.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.gen.chunk.ChunkGenerator;

import net.owen.bladebound.item.ModItems;
import net.owen.bladebound.worldgen.structure.ExcaliburSwordInStonePiece;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class BladeboundCommands {

    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> register(dispatcher));
    }

    private static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("bladebound")
                .then(literal("codex")
                        .requires(src -> src.hasPermissionLevel(2))
                        .executes(ctx -> {
                            var player = ctx.getSource().getPlayerOrThrow();
                            player.giveItemStack(new ItemStack(ModItems.CODEX)); // rename to your actual item constant
                            ctx.getSource().sendFeedback(() -> Text.literal("Given: Bladebound Codex"), false);
                            return 1;
                        })
                )
                .then(literal("debugspawn")
                        .requires(src -> src.hasPermissionLevel(2))
                        .then(argument("which", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    builder.suggest("excalibur");
                                    builder.suggest("wado");
                                    builder.suggest("murasame");
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> {
                                    ServerCommandSource src = ctx.getSource();
                                    ServerPlayerEntity player = src.getPlayerOrThrow();
                                    World world = player.getWorld();

                                    String which = StringArgumentType.getString(ctx, "which").toLowerCase();
                                    BlockPos base = player.getBlockPos().add(0, 0, 3);

                                    switch (which) {
                                        case "excalibur" -> spawnExcaliburNewStructure(world, base);
                                        case "wado" -> spawnWadoShrine(world, base);
                                        case "murasame" -> spawnMurasameSite(world, base);
                                        default -> {
                                            src.sendError(Text.literal("Unknown option. Use: excalibur, wado, murasame"));
                                            return 0;
                                        }
                                    }

                                    src.sendFeedback(() -> Text.literal("Spawned debug structure: " + which + " at " + base), false);
                                    return 1;
                                })
                        )
                )
        );
    }

    /* =========================================================
       Shared helper
       ========================================================= */
    private static void putItemInBarrel(World world, BlockPos pos, String itemId) {
        Item item = Registries.ITEM.get(Identifier.of(itemId));
        if (item == Registries.ITEM.get(Identifier.of("minecraft:air"))) return;

        if (world.getBlockEntity(pos) instanceof BarrelBlockEntity barrel) {
            barrel.setStack(0, new ItemStack(item));
            barrel.markDirty();
        }
    }

    /* =========================================================
       EXCALIBUR (NEW) - calls your actual StructurePiece generate()
       ========================================================= */
    private static void spawnExcaliburNewStructure(World world, BlockPos origin) {
        if (!(world instanceof ServerWorld serverWorld)) return;

        ExcaliburSwordInStonePiece piece = new ExcaliburSwordInStonePiece(origin);

        // Real-worldgen-ish arguments
        var structureAccessor = serverWorld.getStructureAccessor();
        ChunkGenerator chunkGenerator = serverWorld.getChunkManager().getChunkGenerator();
        Random random = Random.create();

        // Make the chunkBox huge so your chunkBox.contains(...) checks always pass in debug
        BlockBox chunkBox = new BlockBox(
                origin.getX() - 128, origin.getY() - 64, origin.getZ() - 128,
                origin.getX() + 128, origin.getY() + 64, origin.getZ() + 128
        );

        ChunkPos chunkPos = new ChunkPos(origin);

        piece.generate(
                serverWorld,          // StructureWorldAccess
                structureAccessor,
                chunkGenerator,
                random,
                chunkBox,
                chunkPos,
                origin
        );
    }

    /* =========================================================
       WADO (unchanged)
       ========================================================= */
    private static void spawnWadoShrine(World world, BlockPos origin) {
        // 5x5 calcite floor
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                world.setBlockState(origin.add(dx, -1, dz), Blocks.CALCITE.getDefaultState(), 3);
            }
        }

        // pedestal
        world.setBlockState(origin, Blocks.POLISHED_DIORITE.getDefaultState(), 3);
        world.setBlockState(origin.up(1), Blocks.POLISHED_DIORITE_SLAB.getDefaultState(), 3);

        // lantern corners
        placeLanternPost(world, origin.add(2, 0, 2));
        placeLanternPost(world, origin.add(2, 0, -2));
        placeLanternPost(world, origin.add(-2, 0, 2));
        placeLanternPost(world, origin.add(-2, 0, -2));

        // barrel “offering cache” under pedestal
        BlockPos barrelPos = origin.down(1);
        world.setBlockState(barrelPos, Blocks.BARREL.getDefaultState(), 3);
        putItemInBarrel(world, barrelPos, "bladebound:wado_ichimonji");
    }

    private static void placeLanternPost(World world, BlockPos pos) {
        world.setBlockState(pos, Blocks.OAK_FENCE.getDefaultState(), 3);
        world.setBlockState(pos.up(1), Blocks.LANTERN.getDefaultState(), 3);
    }

    /* =========================================================
       MURASAME (unchanged)
       ========================================================= */
    private static void spawnMurasameSite(World world, BlockPos origin) {
        // rough 7x7 patch (coarse dirt + gravel)
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                BlockPos p = origin.add(dx, -1, dz);
                if ((dx * dx + dz * dz) <= 10) {
                    world.setBlockState(p,
                            (dx + dz) % 2 == 0 ? Blocks.COARSE_DIRT.getDefaultState() : Blocks.GRAVEL.getDefaultState(),
                            3
                    );
                }
            }
        }

        // center “execution stone”
        world.setBlockState(origin, Blocks.CRACKED_STONE_BRICKS.getDefaultState(), 3);

        // wither rose + bones vibe
        world.setBlockState(origin.add(1, 0, 0), Blocks.WITHER_ROSE.getDefaultState(), 3);
        world.setBlockState(origin.add(-1, 0, 1), Blocks.BONE_BLOCK.getDefaultState(), 3);

        // soul lantern
        world.setBlockState(origin.add(0, 0, -2), Blocks.COBBLESTONE_WALL.getDefaultState(), 3);
        world.setBlockState(origin.add(0, 1, -2), Blocks.SOUL_LANTERN.getDefaultState(), 3);

        // barrel hidden in ground containing murasame
        BlockPos barrelPos = origin.down(1);
        world.setBlockState(barrelPos, Blocks.BARREL.getDefaultState(), 3);
        putItemInBarrel(world, barrelPos, "bladebound:murasame");
    }
}
