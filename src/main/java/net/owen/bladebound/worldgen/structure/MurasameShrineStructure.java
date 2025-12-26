package net.owen.bladebound.worldgen.structure;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

import java.util.Optional;

public class MurasameShrineStructure extends Structure {

    public static final MapCodec<MurasameShrineStructure> CODEC =
            Structure.createCodec(MurasameShrineStructure::new);

    public MurasameShrineStructure(Config config) {
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

        int bottom = context.world().getBottomY();
        int top = context.world().getTopY();
        y = Math.max(bottom, Math.min(y, top - 1));

        BlockPos origin = new BlockPos(x, y, z);

        // Much cheaper than scanning a whole square (prevents /locate lag)
        int sampleRadius = 10; // roughly footprint-ish; adjust if needed
        int maxDiff = 2;       // allow gentle slope; lower to 1 if you still see floating
        if (!isFlatEnoughSampled(context, origin, sampleRadius, maxDiff)) {
            return Optional.empty();
        }

        return Optional.of(new StructurePosition(origin, collector ->
                collector.addPiece(new MurasameShrinePiece(origin))
        ));
    }

    private static boolean isFlatEnoughSampled(Context context, BlockPos origin, int r, int maxDiff) {
        int baseY = origin.getY();

        // 9-point sample: center, N/S/E/W, and 4 corners
        int[] dx = { 0,  r, -r,  0,  0,  r,  r, -r, -r };
        int[] dz = { 0,  0,  0,  r, -r,  r, -r,  r, -r };

        for (int i = 0; i < dx.length; i++) {
            int px = origin.getX() + dx[i];
            int pz = origin.getZ() + dz[i];

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
        return true;
    }

    @Override
    public StructureType<?> getType() {
        return BladeboundStructures.MURASAME_SHRINE;
    }
}
