package net.owen.bladebound.worldgen.structure;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
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

public class MurasameShrinePiece extends StructurePiece {

    private static final Identifier TEMPLATE_ID = Identifier.of("bladebound", "murasame_shrine");

    // Worldgen-safe placement flags
    private static final int WG_FLAGS = 2 | 16;

    private final BlockPos origin;

    // Entities (frames/paintings) once
    private boolean entitiesPlaced;

    // Lock base across chunk passes (prevents shifting / cut-off)
    private boolean baseLocked;
    private BlockPos lockedBase;

    public MurasameShrinePiece(StructureContext context, NbtCompound nbt) {
        super(BladeboundStructures.MURASAME_SHRINE_PIECE, nbt);

        this.origin = new BlockPos(nbt.getInt("ox"), nbt.getInt("oy"), nbt.getInt("oz"));
        this.entitiesPlaced = nbt.getBoolean("entitiesPlaced");

        this.baseLocked = nbt.getBoolean("baseLocked");
        if (baseLocked) {
            this.lockedBase = new BlockPos(nbt.getInt("bx"), nbt.getInt("by"), nbt.getInt("bz"));
        } else {
            this.lockedBase = null;
        }
    }

    public MurasameShrinePiece(BlockPos origin) {
        super(
                BladeboundStructures.MURASAME_SHRINE_PIECE,
                0,
                // Big conservative box; replaced after template size is known
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

        // Lock base so the structure can't drift between chunk passes
        BlockPos base;
        if (baseLocked && lockedBase != null) {
            base = lockedBase;
        } else {
            base = snapToGround(world, origin);
            lockedBase = base;
            baseLocked = true;
        }

        // ✅ Center X/Z like dojo/church (more stable on slopes)
        BlockPos placePos = base.add(-(size.getX() / 2), 0, -(size.getZ() / 2));

        // Real structure footprint
        BlockBox realBox = new BlockBox(
                placePos.getX(), placePos.getY(), placePos.getZ(),
                placePos.getX() + size.getX() - 1,
                placePos.getY() + size.getY() - 1,
                placePos.getZ() + size.getZ() - 1
        );
        this.boundingBox = realBox;

        if (!chunkBox.intersects(realBox)) return;

        // Carve only inside this chunk pass so terrain doesn't eat walls
        carveToAir(world, chunkBox, realBox);

        // Debugspawn uses huge chunkBox; force entities in that case
        boolean debugBox =
                (chunkBox.getMaxX() - chunkBox.getMinX()) > 400 ||
                        (chunkBox.getMaxZ() - chunkBox.getMinZ()) > 400;

        BlockPos center = placePos.add(size.getX() / 2, 0, size.getZ() / 2);
        ChunkPos anchorChunk = new ChunkPos(center);

        boolean placeEntitiesThisTime =
                debugBox || (!entitiesPlaced && chunkPos.equals(anchorChunk));

        StructurePlacementData placement = new StructurePlacementData()
                .setIgnoreEntities(!placeEntitiesThisTime);

        template.place(world, placePos, placePos, placement, random, WG_FLAGS);

        if (placeEntitiesThisTime) {
            entitiesPlaced = true;
        }

        // ✅ Fix “stacked grass layers”: keep only top-most grass per column
        collapseExtraGrassToDirt(world, realBox);
    }

    /**
     * Clears the structure's volume to air (only where this generate() call intersects).
     * This prevents hillsides from embedding the shrine.
     */
    private static void carveToAir(StructureWorldAccess world, BlockBox chunkBox, BlockBox realBox) {
        int pad = 1;

        int minX = realBox.getMinX() - pad;
        int minY = realBox.getMinY();
        int minZ = realBox.getMinZ() - pad;

        int maxX = realBox.getMaxX() + pad;
        int maxY = realBox.getMaxY() + pad;
        int maxZ = realBox.getMaxZ() + pad;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    BlockPos p = new BlockPos(x, y, z);
                    if (!chunkBox.contains(p)) continue;

                    BlockState state = world.getBlockState(p);
                    if (state.isOf(Blocks.BEDROCK)) continue;

                    if (!state.isAir()) {
                        world.setBlockState(p, Blocks.AIR.getDefaultState(), WG_FLAGS);
                    }
                }
            }
        }
    }

    /**
     * Keeps only the top-most GRASS_BLOCK in each (x,z) column and converts any lower
     * GRASS_BLOCKs inside the structure area to DIRT. This prevents the “layer cake” hillside.
     */
    private static void collapseExtraGrassToDirt(StructureWorldAccess world, BlockBox box) {
        for (int x = box.getMinX(); x <= box.getMaxX(); x++) {
            for (int z = box.getMinZ(); z <= box.getMaxZ(); z++) {
                boolean keptTopGrass = false;

                for (int y = box.getMaxY(); y >= box.getMinY(); y--) {
                    BlockPos p = new BlockPos(x, y, z);
                    BlockState state = world.getBlockState(p);

                    if (state.isOf(Blocks.GRASS_BLOCK)) {
                        if (!keptTopGrass) {
                            keptTopGrass = true; // keep the first one we hit from the top
                        } else {
                            world.setBlockState(p, Blocks.DIRT.getDefaultState(), WG_FLAGS);
                        }
                    }
                }
            }
        }
    }

    /**
     * Surface “air above solid” at approx X/Z.
     */
    private static BlockPos snapToGround(StructureWorldAccess world, BlockPos approx) {
        BlockPos p = world.getTopPosition(
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                new BlockPos(approx.getX(), 0, approx.getZ())
        );

        if (p.getY() < world.getBottomY() + 2) {
            p = new BlockPos(p.getX(), world.getBottomY() + 2, p.getZ());
        }

        while (p.getY() > world.getBottomY() + 2) {
            BlockState at = world.getBlockState(p);
            BlockState below = world.getBlockState(p.down());

            boolean atOk = at.isAir() || at.isReplaceable();
            boolean belowOk = below.isSolidBlock(world, p.down());

            if (atOk && belowOk) return p;
            p = p.down();
        }

        return p;
    }
}
