package net.owen.bladebound.worldgen.structure;

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

public class FrierenTowerPiece extends StructurePiece {

    private static final Identifier TEMPLATE_ID = Identifier.of("bladebound", "frieren_tower");
    private static final int WG_FLAGS = 2 | 16;

    // If your tower sinks/floats, tweak this by ±1 or ±2.
    private static final int Y_OFFSET = 0;

    private final BlockPos origin;

    private boolean entitiesPlaced;

    // Lock base across chunk passes so it never shifts
    private boolean baseLocked;
    private BlockPos lockedBase;

    public FrierenTowerPiece(StructureContext context, NbtCompound nbt) {
        super(BladeboundStructures.FRIEREN_TOWER_PIECE, nbt);

        this.origin = new BlockPos(nbt.getInt("ox"), nbt.getInt("oy"), nbt.getInt("oz"));
        this.entitiesPlaced = nbt.getBoolean("entitiesPlaced");

        this.baseLocked = nbt.getBoolean("baseLocked");
        if (this.baseLocked) {
            this.lockedBase = new BlockPos(nbt.getInt("bx"), nbt.getInt("by"), nbt.getInt("bz"));
        }
    }

    public FrierenTowerPiece(BlockPos origin) {
        super(
                BladeboundStructures.FRIEREN_TOWER_PIECE,
                0,
                // Large initial box so the piece is eligible before template size is known
                new BlockBox(
                        origin.getX() - 64, origin.getY() - 64, origin.getZ() - 64,
                        origin.getX() + 64, origin.getY() + 128, origin.getZ() + 64
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

        // -------------------------
        // Pick a stable SURFACE base
        // -------------------------
        BlockPos base;
        if (baseLocked && lockedBase != null) {
            base = lockedBase;
        } else {
            // For towers, do NOT scan down (can lock caves/holes). Just use the surface heightmap.
            base = world.getTopPosition(
                    Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                    new BlockPos(origin.getX(), 0, origin.getZ())
            );

            lockedBase = base;
            baseLocked = true;
        }

        // Center X/Z, apply Y offset
        BlockPos placePos = base.add(-(size.getX() / 2), Y_OFFSET, -(size.getZ() / 2));

        // -------------------------
        // CLAMP Y BEFORE bbox calc
        // -------------------------
        int minY = world.getBottomY() + 2; // prevent bedrock cut-off
        int topY = world.getTopY() - 1;
        int maxPlaceY = topY - (size.getY() - 1); // ensure the top fits

        int clampedY = placePos.getY();
        if (clampedY < minY) clampedY = minY;
        if (clampedY > maxPlaceY) clampedY = maxPlaceY;

        if (clampedY != placePos.getY()) {
            placePos = new BlockPos(placePos.getX(), clampedY, placePos.getZ());
        }

        // Tight bounding box from FINAL placePos
        this.boundingBox = new BlockBox(
                placePos.getX(),
                placePos.getY(),
                placePos.getZ(),
                placePos.getX() + size.getX() - 1,
                placePos.getY() + size.getY() - 1,
                placePos.getZ() + size.getZ() - 1
        );

        if (!chunkBox.intersects(this.boundingBox)) return;

        // Debugspawn uses a huge chunkBox; force entities in that case
        boolean debugBox =
                (chunkBox.getMaxX() - chunkBox.getMinX()) > 400 ||
                        (chunkBox.getMaxZ() - chunkBox.getMinZ()) > 400;

        // Anchor chunk computed from placePos so entities are deterministic
        BlockPos center = placePos.add(size.getX() / 2, 0, size.getZ() / 2);
        ChunkPos anchorChunk = new ChunkPos(center);

        boolean placeEntitiesThisTime =
                debugBox || (!entitiesPlaced && chunkPos.equals(anchorChunk));

        StructurePlacementData placement = new StructurePlacementData()
                .setBoundingBox(chunkBox)
                .setRandom(random)
                .setIgnoreEntities(false);

        template.place(world, placePos, placePos, placement, random, WG_FLAGS);

        if (placeEntitiesThisTime) {
            entitiesPlaced = true;
        }
    }
}
