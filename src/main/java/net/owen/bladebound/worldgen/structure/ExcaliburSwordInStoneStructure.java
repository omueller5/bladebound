package net.owen.bladebound.worldgen.structure;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

import java.util.Optional;

public class ExcaliburSwordInStoneStructure extends Structure {

    public static final MapCodec<ExcaliburSwordInStoneStructure> CODEC =
            Structure.createCodec(ExcaliburSwordInStoneStructure::new);

    public ExcaliburSwordInStoneStructure(Structure.Config config) {
        super(config);
    }

    @Override
    public Optional<StructurePosition> getStructurePosition(Context context) {
        // Center of the chunk
        int x = context.chunkPos().getStartX() + 8;
        int z = context.chunkPos().getStartZ() + 8;

        // ✅ Better surface heightmap for structures
        int y = context.chunkGenerator().getHeightInGround(
                x, z,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                context.world(),
                context.noiseConfig()
        );

        // Clamp to world bounds
        int bottom = context.world().getBottomY();
        int top = context.world().getTopY();
        y = Math.max(bottom, Math.min(y, top - 1));

        BlockPos origin = new BlockPos(x, y, z);

        // ✅ Flatness rules (tune these)
        int flatRadius = 4;   // how far out must be flat
        int maxDiff = 0;      // 0 = perfectly flat, 1 = small slope allowed

        if (!isFlatArea(context, origin, flatRadius, maxDiff)) {
            return Optional.empty();
        }

        // Piece will snap to true dry ground using StructureWorldAccess (so no dead bush / water issues)
        return Optional.of(new StructurePosition(origin, collector ->
                collector.addPiece(new ExcaliburSwordInStonePiece(origin))
        ));
    }

    private static boolean isFlatArea(Context context, BlockPos origin, int radius, int maxDiff) {
        int baseY = origin.getY();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int px = origin.getX() + dx;
                int pz = origin.getZ() + dz;

                int yHere = context.chunkGenerator().getHeightInGround(
                        px, pz,
                        Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                        context.world(),
                        context.noiseConfig()
                );

                if (Math.abs(yHere - baseY) > maxDiff) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public StructureType<?> getType() {
        return BladeboundStructures.EXCALIBUR_SWORD_IN_STONE;
    }
}
