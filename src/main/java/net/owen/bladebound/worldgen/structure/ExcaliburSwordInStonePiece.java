package net.owen.bladebound.worldgen.structure;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePiece;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.owen.bladebound.BladeboundBlocks;
import net.owen.bladebound.block.custom.SwordInStoneBlock;

public class ExcaliburSwordInStonePiece extends StructurePiece {

    private final BlockPos origin;

    /* ===============================
       Load-from-NBT constructor
       =============================== */
    public ExcaliburSwordInStonePiece(StructureContext context, NbtCompound nbt) {
        super(BladeboundStructures.EXCALIBUR_SWORD_IN_STONE_PIECE, nbt);

        this.origin = new BlockPos(
                nbt.getInt("ox"),
                nbt.getInt("oy"),
                nbt.getInt("oz")
        );
    }

    /* ===============================
       Fresh placement constructor
       =============================== */
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

    /* ===============================
       Save custom data
       =============================== */
    @Override
    protected void writeNbt(StructureContext context, NbtCompound nbt) {
        nbt.putInt("ox", origin.getX());
        nbt.putInt("oy", origin.getY());
        nbt.putInt("oz", origin.getZ());
    }

    /* ===============================
       Structure generation
       =============================== */
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
        // ✅ Find true, dry ground (not water, not plants-as-surface)
        BlockPos base = findDryGround(world, origin);
        if (base == null) return;

        // Update bounding box to match the corrected base position (helps chunk culling)
        this.boundingBox = new BlockBox(
                base.getX() - 2, base.getY() - 1, base.getZ() - 2,
                base.getX() + 2, base.getY() + 3, base.getZ() + 2
        );

        // 3x3 stone base (one block below base)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos pos = base.add(dx, -1, dz);
                if (chunkBox.contains(pos)) {
                    world.setBlockState(pos, Blocks.STONE.getDefaultState(), 3);
                }
            }
        }

        // Sword-in-stone block at base (force sword=true)
        if (chunkBox.contains(base)) {
            world.setBlockState(
                    base,
                    BladeboundBlocks.SWORD_IN_STONE.getDefaultState().with(SwordInStoneBlock.SWORD, true),
                    3
            );
        }
    }

    /**
     * Picks a dry ground placement near origin:
     * - uses surface heightmap
     * - walks down until: current is air/replaceable AND below is solid
     * - rejects water (fluid at current or below)
     */
    private static BlockPos findDryGround(StructureWorldAccess world, BlockPos approx) {
        int radius = 12; // search nearby land if the center is water

        for (int r = 0; r <= radius; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.abs(dx) != r && Math.abs(dz) != r) continue; // perimeter scan

                    BlockPos xz = new BlockPos(approx.getX() + dx, 0, approx.getZ() + dz);
                    BlockPos p = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, xz);

                    // Walk down to solid ground under a replaceable/air block
                    while (p.getY() > world.getBottomY() + 2) {
                        BlockState at = world.getBlockState(p);
                        BlockState below = world.getBlockState(p.down());

                        boolean atOk = at.isAir() || at.isReplaceable();
                        boolean belowOk = below.isSolidBlock(world, p.down());

                        if (atOk && belowOk) break;
                        p = p.down();
                    }

                    // Reject water/fluid
                    FluidState fluidAt = world.getFluidState(p);
                    FluidState fluidBelow = world.getFluidState(p.down());
                    if (!fluidAt.isEmpty() || !fluidBelow.isEmpty()) continue;

                    return p;
                }
            }
        }

        return null;
    }
}
