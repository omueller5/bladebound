package net.owen.bladebound.worldgen.structure;

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

        // rough patch
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                BlockPos p = origin.add(dx, -1, dz);
                if (!chunkBox.contains(p)) continue;
                if ((dx * dx + dz * dz) <= 10) {
                    world.setBlockState(p, ((dx + dz) & 1) == 0 ? Blocks.COARSE_DIRT.getDefaultState() : Blocks.GRAVEL.getDefaultState(), 3);
                }
            }
        }

        // center stone
        if (chunkBox.contains(origin)) world.setBlockState(origin, Blocks.CRACKED_STONE_BRICKS.getDefaultState(), 3);

        // eerie props
        if (chunkBox.contains(origin.add(1, 0, 0))) world.setBlockState(origin.add(1, 0, 0), Blocks.WITHER_ROSE.getDefaultState(), 3);
        if (chunkBox.contains(origin.add(-1, 0, 1))) world.setBlockState(origin.add(-1, 0, 1), Blocks.BONE_BLOCK.getDefaultState(), 3);

        BlockPos post = origin.add(0, 0, -2);
        if (chunkBox.contains(post)) world.setBlockState(post, Blocks.COBBLESTONE_WALL.getDefaultState(), 3);
        if (chunkBox.contains(post.up(1))) world.setBlockState(post.up(1), Blocks.SOUL_LANTERN.getDefaultState(), 3);

        // barrel hidden holding murasame
        BlockPos barrelPos = origin.down(1);
        if (chunkBox.contains(barrelPos)) {
            world.setBlockState(barrelPos, Blocks.BARREL.getDefaultState(), 3);
            putItemInBarrel(world, barrelPos, "bladebound:murasame");
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
