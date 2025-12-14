package net.owen.bladebound.worldgen.structure;

import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlockEntity;
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
        // 5x5 calcite floor
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                BlockPos p = origin.add(dx, -1, dz);
                if (chunkBox.contains(p)) {
                    world.setBlockState(p, Blocks.CALCITE.getDefaultState(), WG_FLAGS);
                }
            }
        }

        // pedestal
        if (chunkBox.contains(origin)) {
            world.setBlockState(origin, Blocks.POLISHED_DIORITE.getDefaultState(), WG_FLAGS);
        }
        if (chunkBox.contains(origin.up(1))) {
            world.setBlockState(origin.up(1), Blocks.POLISHED_DIORITE_SLAB.getDefaultState(), WG_FLAGS);
        }

        // lantern corners
        placeLanternPost(world, chunkBox, origin.add(2, 0, 2));
        placeLanternPost(world, chunkBox, origin.add(2, 0, -2));
        placeLanternPost(world, chunkBox, origin.add(-2, 0, 2));
        placeLanternPost(world, chunkBox, origin.add(-2, 0, -2));

        // barrel under center with item
        BlockPos barrelPos = origin.down(1);
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
}
