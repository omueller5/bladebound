package net.owen.bladebound.worldgen.structure;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class WadoDojoPiece extends StructurePiece {

    private static final Identifier TEMPLATE_ID = Identifier.of("bladebound", "wado_dojo");
    private static final int WG_FLAGS = 2 | 16;

    // If still slightly buried, raise to 2.
    // If floating, lower to 0.
    private static final int WADO_Y_OFFSET = 1;

    private final BlockPos origin;

    private boolean entitiesPlaced;
    private boolean baseLocked;
    private BlockPos lockedBase;

    public WadoDojoPiece(StructureContext context, NbtCompound nbt) {
        super(BladeboundStructures.WADO_DOJO_PIECE, nbt);

        this.origin = new BlockPos(nbt.getInt("ox"), nbt.getInt("oy"), nbt.getInt("oz"));
        this.entitiesPlaced = nbt.getBoolean("entitiesPlaced");
        this.baseLocked = nbt.getBoolean("baseLocked");

        if (this.baseLocked) {
            this.lockedBase = new BlockPos(nbt.getInt("bx"), nbt.getInt("by"), nbt.getInt("bz"));
        }
    }

    public WadoDojoPiece(BlockPos origin) {
        super(
                BladeboundStructures.WADO_DOJO_PIECE,
                0,
                new BlockBox(
                        origin.getX() - 96, origin.getY() - 64, origin.getZ() - 96,
                        origin.getX() + 96, origin.getY() + 96, origin.getZ() + 96
                )
        );
        this.origin = origin;
        this.entitiesPlaced = false;
        this.baseLocked = false;
        this.lockedBase = null;
    }

    @Override
    protected void writeNbt(StructureContext context, NbtCompound nbt) {
        nbt.putInt("ox", origin.getX());
        nbt.putInt("oy", origin.getY());
        nbt.putInt("oz", origin.getZ());

        nbt.putBoolean("entitiesPlaced", entitiesPlaced);
        nbt.putBoolean("baseLocked", baseLocked);

        if (baseLocked && lockedBase != null) {
            nbt.putInt("bx", lockedBase.getX());
            nbt.putInt("by", lockedBase.getY());
            nbt.putInt("bz", lockedBase.getZ());
        }
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
        if (world.getServer() == null) return;

        StructureTemplate template = world.getServer()
                .getStructureTemplateManager()
                .getTemplateOrBlank(TEMPLATE_ID);

        Vec3i size = template.getSize();
        if (size.getX() <= 0 || size.getY() <= 0 || size.getZ() <= 0) return;

        BlockPos base;
        if (baseLocked && lockedBase != null) {
            base = lockedBase;
        } else {
            base = findDryGround(world, origin);
            if (base == null) return;
            lockedBase = base;
            baseLocked = true;
        }

        // ✅ Center X/Z like Excalibur (prevents “half buried” from corner placement)
        BlockPos placePos = base.add(-(size.getX() / 2), WADO_Y_OFFSET, -(size.getZ() / 2));

        this.boundingBox = new BlockBox(
                placePos.getX(),
                placePos.getY(),
                placePos.getZ(),
                placePos.getX() + size.getX() - 1,
                placePos.getY() + size.getY() - 1,
                placePos.getZ() + size.getZ() - 1
        );

        if (!chunkBox.intersects(this.boundingBox)) return;

        boolean debugBox = (chunkBox.getMaxX() - chunkBox.getMinX()) > 400
                || (chunkBox.getMaxZ() - chunkBox.getMinZ()) > 400;

        BlockPos center = placePos.add(size.getX() / 2, 0, size.getZ() / 2);
        boolean placeEntitiesThisTime = debugBox || (!entitiesPlaced && chunkPos.equals(new ChunkPos(center)));

        StructurePlacementData placement = new StructurePlacementData()
                .setIgnoreEntities(!placeEntitiesThisTime);

        template.place(world, placePos, placePos, placement, random, WG_FLAGS);

        if (placeEntitiesThisTime) entitiesPlaced = true;
    }

    private static BlockPos findDryGround(StructureWorldAccess world, BlockPos approx) {
        int radius = 12;

        for (int r = 0; r <= radius; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.abs(dx) != r && Math.abs(dz) != r) continue;

                    BlockPos xz = new BlockPos(approx.getX() + dx, 0, approx.getZ() + dz);
                    BlockPos p = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, xz);

                    while (p.getY() > world.getBottomY() + 2) {
                        BlockState at = world.getBlockState(p);
                        BlockState below = world.getBlockState(p.down());

                        if ((at.isAir() || at.isReplaceable()) && below.isSolidBlock(world, p.down())) break;
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
