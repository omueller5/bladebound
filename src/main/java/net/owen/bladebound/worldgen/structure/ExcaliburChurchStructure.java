package net.owen.bladebound.worldgen.structure;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

import java.util.Optional;

public class ExcaliburChurchStructure extends Structure {

    public static final MapCodec<ExcaliburChurchStructure> CODEC =
            Structure.createCodec(ExcaliburChurchStructure::new);

    public ExcaliburChurchStructure(Structure.Config config) {
        super(config);
    }

    @Override
    public Optional<StructurePosition> getStructurePosition(Context context) {
        // Center of the chunk
        int x = context.chunkPos().getStartX() + 8;
        int z = context.chunkPos().getStartZ() + 8;

        // Surface height
        int y = context.chunkGenerator().getHeightInGround(
                x, z,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                context.world(),
                context.noiseConfig()
        );

        int bottom = context.world().getBottomY();
        int top = context.world().getTopY();
        y = Math.max(bottom, Math.min(y, top - 1));

        BlockPos origin = new BlockPos(x, y, z);

        // Looser, realistic flatness rules
        int flatRadius = 6; // 13x13 area
        int maxDiff = 1;    // allow gentle slope

        if (!isFlatArea(context, origin, flatRadius, maxDiff)) {
            return Optional.empty();
        }

        return Optional.of(
                new StructurePosition(origin, collector ->
                        collector.addPiece(
                                new ExcaliburChurchPiece(origin)
                        )
                )
        );
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
        return BladeboundStructures.EXCALIBUR_CHURCH;
    }
}
