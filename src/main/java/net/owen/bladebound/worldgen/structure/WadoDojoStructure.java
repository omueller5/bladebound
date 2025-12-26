package net.owen.bladebound.worldgen.structure;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

import java.util.Optional;

public class WadoDojoStructure extends Structure {

    public static final MapCodec<WadoDojoStructure> CODEC =
            Structure.createCodec(WadoDojoStructure::new);

    public WadoDojoStructure(Config config) {
        super(config);
    }

    @Override
    public Optional<StructurePosition> getStructurePosition(Context context) {
        int x = context.chunkPos().getStartX() + 8;
        int z = context.chunkPos().getStartZ() + 8;

        int y = context.chunkGenerator().getHeightInGround(
                x, z,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                context.world(),
                context.noiseConfig()
        );

        // Clamp to world bounds (prevents rare weirdness)
        int bottom = context.world().getBottomY();
        int top = context.world().getTopY();
        y = Math.max(bottom, Math.min(y, top - 1));

        BlockPos origin = new BlockPos(x, y, z);

        // Optional but recommended: avoid steep slopes
        int flatRadius = 6; // 13x13
        int maxDiff = 1;    // gentle slope allowed
        if (!isFlatArea(context, origin, flatRadius, maxDiff)) {
            return Optional.empty();
        }

        return Optional.of(
                new StructurePosition(origin, collector ->
                        collector.addPiece(new WadoDojoPiece(origin))
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
        return BladeboundStructures.WADO_DOJO;
    }
}
