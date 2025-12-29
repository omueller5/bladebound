package net.owen.bladebound.worldgen.structure;

import com.mojang.serialization.MapCodec;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.structure.StructureType;

public class BladeboundStructures {

    /* =========================================================
       WADO DOJO
       ========================================================= */

    public static final StructureType<WadoDojoStructure> WADO_DOJO =
            Registry.register(
                    Registries.STRUCTURE_TYPE,
                    Identifier.of("bladebound", "wado_dojo"),
                    new StructureType<>() {
                        @Override
                        public MapCodec<WadoDojoStructure> codec() {
                            return WadoDojoStructure.CODEC;
                        }
                    }
            );

    public static final StructurePieceType WADO_DOJO_PIECE =
            Registry.register(
                    Registries.STRUCTURE_PIECE,
                    Identifier.of("bladebound", "wado_dojo_piece"),
                    WadoDojoPiece::new
            );

    /* =========================================================
       MURASAME EXECUTION SITE
       ========================================================= */

    public static final StructureType<MurasameShrineStructure> MURASAME_SHRINE =
            Registry.register(
                    Registries.STRUCTURE_TYPE,
                    Identifier.of("bladebound", "murasame_shrine"),
                    new StructureType<>() {
                        @Override
                        public MapCodec<MurasameShrineStructure> codec() {
                            return MurasameShrineStructure.CODEC;
                        }
                    }
            );

    public static final StructurePieceType MURASAME_SHRINE_PIECE =
            Registry.register(
                    Registries.STRUCTURE_PIECE,
                    Identifier.of("bladebound", "murasame_shrine_piece"),
                    MurasameShrinePiece::new
            );

    /* =========================================================
       EXCALIBUR CHURCH
       ========================================================= */

    public static final StructureType<ExcaliburChurchStructure> EXCALIBUR_CHURCH =
            Registry.register(
                    Registries.STRUCTURE_TYPE,
                    Identifier.of("bladebound", "excalibur_church"),
                    new StructureType<>() {
                        @Override
                        public MapCodec<ExcaliburChurchStructure> codec() {
                            return ExcaliburChurchStructure.CODEC;
                        }
                    }
            );

    public static final StructurePieceType EXCALIBUR_CHURCH_PIECE =
            Registry.register(
                    Registries.STRUCTURE_PIECE,
                    Identifier.of("bladebound", "excalibur_church_piece"),
                    ExcaliburChurchPiece::new
            );

     /* =========================================================
       FRIEREN TOWER
       ========================================================= */
     public static final StructureType<FrierenTowerStructure> FRIEREN_TOWER =
             Registry.register(
                     Registries.STRUCTURE_TYPE,
                     Identifier.of("bladebound", "frieren_tower"),
                     new StructureType<>() {
                         @Override
                         public com.mojang.serialization.MapCodec<FrierenTowerStructure> codec() {
                             return FrierenTowerStructure.CODEC;
                         }
                     }
             );

    public static final StructurePieceType FRIEREN_TOWER_PIECE =
            Registry.register(
                    Registries.STRUCTURE_PIECE,
                    Identifier.of("bladebound", "frieren_tower_piece"),
                    FrierenTowerPiece::new
            );


    public static void init() {}
}
