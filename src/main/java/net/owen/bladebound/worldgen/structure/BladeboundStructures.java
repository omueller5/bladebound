package net.owen.bladebound.worldgen.structure;

import com.mojang.serialization.MapCodec;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.structure.StructureType;

public class BladeboundStructures {
    public static final StructureType<WadoShrineStructure> WADO_SHRINE =
            Registry.register(
                    Registries.STRUCTURE_TYPE,
                    Identifier.of("bladebound", "wado_shrine"),
                    new StructureType<>() {
                        @Override
                        public com.mojang.serialization.MapCodec<WadoShrineStructure> codec() {
                            return WadoShrineStructure.CODEC;
                        }
                    }
            );

    public static final StructureType<MurasameExecutionSiteStructure> MURASAME_EXECUTION_SITE =
            Registry.register(
                    Registries.STRUCTURE_TYPE,
                    Identifier.of("bladebound", "murasame_execution_site"),
                    new StructureType<>() {
                        @Override
                        public com.mojang.serialization.MapCodec<MurasameExecutionSiteStructure> codec() {
                            return MurasameExecutionSiteStructure.CODEC;
                        }
                    }
            );

    public static final StructurePieceType WADO_SHRINE_PIECE =
            Registry.register(
                    Registries.STRUCTURE_PIECE,
                    Identifier.of("bladebound", "wado_shrine_piece"),
                    WadoShrinePiece::new
            );

    public static final StructurePieceType MURASAME_EXECUTION_SITE_PIECE =
            Registry.register(
                    Registries.STRUCTURE_PIECE,
                    Identifier.of("bladebound", "murasame_execution_site_piece"),
                    MurasameExecutionSitePiece::new
            );


    public static final StructureType<ExcaliburSwordInStoneStructure> EXCALIBUR_SWORD_IN_STONE =
            Registry.register(
                    Registries.STRUCTURE_TYPE,
                    Identifier.of("bladebound", "excalibur_sword_in_stone"),
                    new StructureType<>() {
                        @Override
                        public MapCodec<ExcaliburSwordInStoneStructure> codec() {
                            return ExcaliburSwordInStoneStructure.CODEC;
                        }
                    }
            );

    public static final StructurePieceType EXCALIBUR_SWORD_IN_STONE_PIECE =
            Registry.register(
                    Registries.STRUCTURE_PIECE, // ✅ THIS is the correct one on 1.21+
                    Identifier.of("bladebound", "excalibur_sword_in_stone_piece"),
                    ExcaliburSwordInStonePiece::new
            );

    public static void init() {}
}
