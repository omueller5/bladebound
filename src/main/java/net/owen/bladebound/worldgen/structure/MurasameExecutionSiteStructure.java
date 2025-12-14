package net.owen.bladebound.worldgen.structure;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

import java.util.Optional;

public class MurasameExecutionSiteStructure extends Structure {

    public static final MapCodec<MurasameExecutionSiteStructure> CODEC =
            Structure.createCodec(MurasameExecutionSiteStructure::new);

    public MurasameExecutionSiteStructure(Config config) {
        super(config);
    }

    @Override
    public Optional<StructurePosition> getStructurePosition(Context context) {
        int x = context.chunkPos().getStartX() + 8;
        int z = context.chunkPos().getStartZ() + 8;

        int y = context.chunkGenerator().getHeightInGround(
                x, z,
                Heightmap.Type.WORLD_SURFACE_WG,
                context.world(),
                context.noiseConfig()
        );

        BlockPos pos = new BlockPos(x, y, z);

        return Optional.of(new StructurePosition(pos, collector -> {
            collector.addPiece(new MurasameExecutionSitePiece(pos));
        }));
    }

    @Override
    public StructureType<?> getType() {
        return BladeboundStructures.MURASAME_EXECUTION_SITE;
    }
}
