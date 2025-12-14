package net.owen.bladebound.worldgen.structure;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

import java.util.Optional;

public class ExcaliburSwordInStoneStructure extends Structure {

    // ✅ This is the 1.21+ pattern: MapCodec via Structure.createCodec(...)
    public static final MapCodec<ExcaliburSwordInStoneStructure> CODEC =
            Structure.createCodec(ExcaliburSwordInStoneStructure::new);

    public ExcaliburSwordInStoneStructure(Structure.Config config) {
        super(config);
    }

    @Override
    public Optional<StructurePosition> getStructurePosition(Context context) {
        // center of the chunk
        int x = context.chunkPos().getStartX() + 8;
        int z = context.chunkPos().getStartZ() + 8;

        // surface height for worldgen (WG)
        int y = context.chunkGenerator().getHeightInGround(
                x, z,
                net.minecraft.world.Heightmap.Type.WORLD_SURFACE_WG,
                context.world(),
                context.noiseConfig()
        ); // ChunkGenerator has getHeightInGround with Heightmap.Type :contentReference[oaicite:1]{index=1}

        // keep it inside the world's vertical bounds
        int bottom = context.world().getBottomY();
        int top = context.world().getTopY();
        y = Math.max(bottom, Math.min(y, top - 1));

        BlockPos pos = new BlockPos(x, y, z);

        return Optional.of(new StructurePosition(pos, collector -> {
            collector.addPiece(new ExcaliburSwordInStonePiece(pos));
        }));
    }


    @Override
    public StructureType<?> getType() {
        return BladeboundStructures.EXCALIBUR_SWORD_IN_STONE;
    }
}
