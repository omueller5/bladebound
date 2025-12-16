package net.owen.bladebound.worldgen.structure;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.item.Item;
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
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.Heightmap;

public class MurasameExecutionSitePiece extends StructurePiece {

    private final BlockPos origin;

    public MurasameExecutionSitePiece(StructureContext context, NbtCompound nbt) {
        super(BladeboundStructures.MURASAME_EXECUTION_SITE_PIECE, nbt);
        this.origin = new BlockPos(nbt.getInt("ox"), nbt.getInt("oy"), nbt.getInt("oz"));
        this.boundingBox = new BlockBox(origin.getX()-5, origin.getY()-3, origin.getZ()-5, origin.getX()+5, origin.getY()+5, origin.getZ()+5);
    }

    public MurasameExecutionSitePiece(BlockPos origin) {
        super(BladeboundStructures.MURASAME_EXECUTION_SITE_PIECE, 0,
                new BlockBox(origin.getX()-5, origin.getY()-3, origin.getZ()-5, origin.getX()+5, origin.getY()+5, origin.getZ()+5));
        this.origin = origin;
    }

    @Override
    protected void writeNbt(StructureContext context, NbtCompound nbt) {
        nbt.putInt("ox", origin.getX());
        nbt.putInt("oy", origin.getY());
        nbt.putInt("oz", origin.getZ());
    }

    @Override
    public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator,
                         Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {

        // ✅ Snap the structure to true ground (ignores dead bushes/grass/etc)
        BlockPos base = snapToGround(world, origin);

        // Update bounding box so chunk culling matches the corrected Y
        this.boundingBox = new BlockBox(base.getX()-5, base.getY()-3, base.getZ()-5, base.getX()+5, base.getY()+5, base.getZ()+5);

        // rough patch (under the surface)
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                BlockPos p = base.add(dx, -1, dz);
                if (!chunkBox.contains(p)) continue;

                if ((dx * dx + dz * dz) <= 10) {
                    world.setBlockState(p, ((dx + dz) & 1) == 0
                            ? Blocks.COARSE_DIRT.getDefaultState()
                            : Blocks.GRAVEL.getDefaultState(), 3);
                }
            }
        }

        // center stone (on the ground)
        if (chunkBox.contains(base)) world.setBlockState(base, Blocks.CRACKED_STONE_BRICKS.getDefaultState(), 3);

        // clear replaceables where props go so nothing floats on dead bushes etc.
        clearReplaceable(world, base.up());
        clearReplaceable(world, base.add(1, 0, 0));
        clearReplaceable(world, base.add(-1, 0, 1));
        clearReplaceable(world, base.add(0, 0, -2));
        clearReplaceable(world, base.add(0, 1, -2));

        // eerie props (place on top of ground where appropriate)
        BlockPos rosePos = base.add(1, 0, 0);
        if (chunkBox.contains(rosePos)) {
            // flowers want air above; place above ground if the ground is occupied by our stone/patch
            BlockPos place = rosePos;
            if (!isReplaceable(world.getBlockState(place))) place = rosePos.up();
            if (chunkBox.contains(place) && isReplaceable(world.getBlockState(place))) {
                world.setBlockState(place, Blocks.WITHER_ROSE.getDefaultState(), 3);
            }
        }

        BlockPos bonePos = base.add(-1, 0, 1);
        if (chunkBox.contains(bonePos)) {
            if (isReplaceable(world.getBlockState(bonePos))) {
                world.setBlockState(bonePos, Blocks.BONE_BLOCK.getDefaultState(), 3);
            } else if (chunkBox.contains(bonePos.up()) && isReplaceable(world.getBlockState(bonePos.up()))) {
                world.setBlockState(bonePos.up(), Blocks.BONE_BLOCK.getDefaultState(), 3);
            }
        }

        BlockPos post = base.add(0, 0, -2);
        if (chunkBox.contains(post)) world.setBlockState(post, Blocks.COBBLESTONE_WALL.getDefaultState(), 3);
        if (chunkBox.contains(post.up(1))) world.setBlockState(post.up(1), Blocks.SOUL_LANTERN.getDefaultState(), 3);

        // barrel hidden holding murasame (buried one block below ground)
        BlockPos barrelPos = base.down(1);
        if (chunkBox.contains(barrelPos)) {
            world.setBlockState(barrelPos, Blocks.BARREL.getDefaultState(), 3);
            putItemInBarrel(world, barrelPos, "bladebound:murasame");
        }
    }

    /**
     * Finds the "real ground" at origin X/Z:
     * - uses MOTION_BLOCKING_NO_LEAVES surface
     * - walks down until below is solid and current is replaceable/air
     */
    private static BlockPos snapToGround(StructureWorldAccess world, BlockPos approx) {
        BlockPos p = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, new BlockPos(approx.getX(), 0, approx.getZ()));

        // Safety clamp if something weird happens
        if (p.getY() < world.getBottomY() + 2) {
            p = new BlockPos(p.getX(), world.getBottomY() + 2, p.getZ());
        }

        while (p.getY() > world.getBottomY() + 2) {
            BlockState at = world.getBlockState(p);
            BlockState below = world.getBlockState(p.down());

            boolean atReplaceable = isReplaceable(at);
            boolean belowSolid = below.isSolidBlock(world, p.down());

            if (atReplaceable && belowSolid) {
                return p; // ✅ place structure "on" this ground
            }

            p = p.down();
        }

        return p;
    }

    private static boolean isReplaceable(BlockState state) {
        return state.isAir() || state.isReplaceable();
    }

    private static void clearReplaceable(StructureWorldAccess world, BlockPos pos) {
        if (isReplaceable(world.getBlockState(pos))) {
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
        }
    }

    private static void putItemInBarrel(StructureWorldAccess world, BlockPos pos, String itemId) {
        Item item = Registries.ITEM.get(Identifier.of(itemId));
        if (world.getBlockEntity(pos) instanceof BarrelBlockEntity barrel) {
            barrel.setStack(0, new ItemStack(item));
            barrel.markDirty();
        }
    }
}
