package net.owen.bladebound.worldgen.structure;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePiece;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class WadoShrinePiece extends StructurePiece {

    // Worldgen-safe placement flags
    private static final int WG_FLAGS = 2 | 16;

    private final BlockPos origin;

    public WadoShrinePiece(StructureContext context, NbtCompound nbt) {
        super(BladeboundStructures.WADO_SHRINE_PIECE, nbt);
        this.origin = new BlockPos(nbt.getInt("ox"), nbt.getInt("oy"), nbt.getInt("oz"));
        this.boundingBox = new BlockBox(
                origin.getX() - 3, origin.getY() - 2, origin.getZ() - 3,
                origin.getX() + 3, origin.getY() + 4, origin.getZ() + 3
        );
    }

    public WadoShrinePiece(BlockPos origin) {
        super(BladeboundStructures.WADO_SHRINE_PIECE, 0,
                new BlockBox(
                        origin.getX() - 3, origin.getY() - 2, origin.getZ() - 3,
                        origin.getX() + 3, origin.getY() + 4, origin.getZ() + 3
                )
        );
        this.origin = origin;
    }

    @Override
    protected void writeNbt(StructureContext context, NbtCompound nbt) {
        nbt.putInt("ox", origin.getX());
        nbt.putInt("oy", origin.getY());
        nbt.putInt("oz", origin.getZ());
    }

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
        // ✅ Snap to real DRY ground (not water, not plants-as-surface)
        BlockPos base = findDryGround(world, origin);
        if (base == null) return;

        // Update bounding box so chunk culling matches corrected placement
        this.boundingBox = new BlockBox(
                base.getX() - 3, base.getY() - 2, base.getZ() - 3,
                base.getX() + 3, base.getY() + 4, base.getZ() + 3
        );

        // 5x5 calcite floor (one block below base)
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                BlockPos p = base.add(dx, -1, dz);
                if (chunkBox.contains(p)) {
                    world.setBlockState(p, Blocks.CALCITE.getDefaultState(), WG_FLAGS);
                }
            }
        }

        // pedestal
        if (chunkBox.contains(base)) {
            world.setBlockState(base, Blocks.POLISHED_DIORITE.getDefaultState(), WG_FLAGS);
        }
        if (chunkBox.contains(base.up(1))) {
            world.setBlockState(base.up(1), Blocks.POLISHED_DIORITE_SLAB.getDefaultState(), WG_FLAGS);
        }

        // lantern corners
        placeLanternPost(world, chunkBox, base.add(2, 0, 2));
        placeLanternPost(world, chunkBox, base.add(2, 0, -2));
        placeLanternPost(world, chunkBox, base.add(-2, 0, 2));
        placeLanternPost(world, chunkBox, base.add(-2, 0, -2));

        // barrel under center with item
        BlockPos barrelPos = base.down(1);
        if (chunkBox.contains(barrelPos)) {
            placeAndFillBarrel(world, barrelPos);
        }
    }

    private static void placeLanternPost(StructureWorldAccess world, BlockBox box, BlockPos pos) {
        if (box.contains(pos)) world.setBlockState(pos, Blocks.OAK_FENCE.getDefaultState(), WG_FLAGS);
        if (box.contains(pos.up(1))) world.setBlockState(pos.up(1), Blocks.LANTERN.getDefaultState(), WG_FLAGS);
    }

    private static void placeAndFillBarrel(StructureWorldAccess world, BlockPos barrelPos) {
        world.setBlockState(barrelPos, Blocks.BARREL.getDefaultState(), WG_FLAGS);

        Identifier id = Identifier.of("bladebound", "wado-ichimonji");
        if (!Registries.ITEM.containsId(id)) {
            System.out.println("[Bladebound] Wado shrine missing item id: " + id);
            return;
        }

        BlockEntity be = world.getBlockEntity(barrelPos);
        if (!(be instanceof BarrelBlockEntity barrel)) {
            System.out.println("[Bladebound] Wado shrine barrel BE missing at " + barrelPos +
                    " (be=" + (be == null ? "null" : be.getClass().getName()) + ")");
            return;
        }

        barrel.setStack(0, new ItemStack(Registries.ITEM.get(id)));
        barrel.markDirty();
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

                    while (p.getY() > world.getBottomY() + 2) {
                        BlockState at = world.getBlockState(p);
                        BlockState below = world.getBlockState(p.down());

                        boolean atOk = at.isAir() || at.isReplaceable();
                        boolean belowOk = below.isSolidBlock(world, p.down());

                        if (atOk && belowOk) break;
                        p = p.down();
                    }

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
