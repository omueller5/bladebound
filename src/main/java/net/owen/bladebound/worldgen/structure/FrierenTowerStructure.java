package net.owen.bladebound.worldgen.structure;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

import java.util.Optional;

public class FrierenTowerStructure extends Structure {

    public static final MapCodec<FrierenTowerStructure> CODEC =
            Structure.createCodec(FrierenTowerStructure::new);

    public FrierenTowerStructure(Structure.Config config) {
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

        // Footprint ~23x22 => half-extents about 12 and 11.
        // Sample-only check keeps /locate from stalling.
        int halfX = 12;
        int halfZ = 11;
        int maxDiff = 3;

        if (!isFlatEnoughSamples(context, origin, halfX, halfZ, maxDiff)) {
            return Optional.empty();
        }

        return Optional.of(
                new StructurePosition(origin, collector ->
                        collector.addPiece(new FrierenTowerPiece(origin))
                )
        );
    }

    private static boolean isFlatEnoughSamples(Context context, BlockPos origin, int halfX, int halfZ, int maxDiff) {
        int baseY = origin.getY();

        // 9-point sample: center, 4 corners, 4 mid-edges
        int[] xs = new int[]{0, -halfX, halfX};
        int[] zs = new int[]{0, -halfZ, halfZ};

        // center + corners
        for (int dx : xs) {
            for (int dz : zs) {
                int yHere = heightAt(context, origin.getX() + dx, origin.getZ() + dz);
                if (Math.abs(yHere - baseY) > maxDiff) return false;
            }
        }

        // mid-edges
        int[][] mids = new int[][]{
                {-halfX, 0}, {halfX, 0}, {0, -halfZ}, {0, halfZ}
        };

        for (int[] m : mids) {
            int yHere = heightAt(context, origin.getX() + m[0], origin.getZ() + m[1]);
            if (Math.abs(yHere - baseY) > maxDiff) return false;
        }

        return true;
    }

    private static int heightAt(Context context, int x, int z) {
        return context.chunkGenerator().getHeightInGround(
                x, z,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                context.world(),
                context.noiseConfig()
        );
    }

    @Override
    public StructureType<?> getType() {
        return BladeboundStructures.FRIEREN_TOWER;
    }
}
