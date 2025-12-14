package net.owen.bladebound.worldgen.structure;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePiece;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.owen.bladebound.BladeboundBlocks;

public class ExcaliburSwordInStonePiece extends StructurePiece {

    private final BlockPos origin;

    // Load-from-NBT constructor (used when the game reloads the structure)
    public ExcaliburSwordInStonePiece(StructureContext context, NbtCompound nbt) {
        super(BladeboundStructures.EXCALIBUR_SWORD_IN_STONE_PIECE, nbt);

        this.origin = new BlockPos(
                nbt.getInt("ox"),
                nbt.getInt("oy"),
                nbt.getInt("oz")
        );

        this.boundingBox = new BlockBox(
                origin.getX() - 2, origin.getY() - 1, origin.getZ() - 2,
                origin.getX() + 2, origin.getY() + 3, origin.getZ() + 2
        );
    }

    // Freshly-created piece constructor (used when structure is first generated)
    public ExcaliburSwordInStonePiece(BlockPos origin) {
        super(
                BladeboundStructures.EXCALIBUR_SWORD_IN_STONE_PIECE,
                0,
                new BlockBox(
                        origin.getX() - 2, origin.getY() - 1, origin.getZ() - 2,
                        origin.getX() + 2, origin.getY() + 3, origin.getZ() + 2
                )
        );
        this.origin = origin;
    }

    @Override
    protected void writeNbt(StructureContext context, NbtCompound nbt) {
        nbt.putInt("ox", origin.getX());
        nbt.putInt("oy", origin.getY());
        nbt.putInt("oz", origin.getZ());
    } // writeNbt(StructureContext, NbtCompound) is correct for modern Yarn :contentReference[oaicite:1]{index=1}

    // ✅ THIS is the one Minecraft will call :contentReference[oaicite:2]{index=2}
    @Override
    public void generate(
            StructureWorldAccess world,
            StructureAccessor structureAccessor,
            ChunkGenerator chunkGenerator,
            Random random,
            BlockBox chunkBox,
            ChunkPos chunkPos,
            BlockPos pivot
    ) {
        // 3x3 stone base (one block below origin)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos p = origin.add(dx, -1, dz);
                if (chunkBox.contains(p)) {
                    world.setBlockState(p, net.minecraft.block.Blocks.STONE.getDefaultState(), 3);
                }
            }
        }

        // sword-in-stone block at origin
        if (chunkBox.contains(origin)) {
            world.setBlockState(origin, BladeboundBlocks.EXCALIBUR_SWORD_IN_STONE.getDefaultState(), 3);
        }
    }
}
