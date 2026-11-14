/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file under the Apache License except in
 * compliance with that License. You may obtain a copy at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Apache License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License for the specific language governing permissions
 * and limitations under the License.
 */

package com.hbmspace.world.gen;

import com.hbm.registration.Registrar;
import com.hbm.registration.RegistryHandle;
import com.hbmspace.world.gen.carver.CelestialCaveCarver;
import com.hbmspace.world.gen.carver.ExperimentalCaveCarver;
import com.hbmspace.world.gen.carver.RavineCarver;
import com.hbmspace.world.gen.carver.RootCarver;
import com.hbmspace.world.gen.density.BedrockFloor;
import com.hbmspace.world.gen.density.CelestialBiomes;
import com.hbmspace.world.gen.density.CelestialRelief;
import com.hbmspace.world.gen.density.LimitNoise;
import com.hbmspace.world.gen.density.NoiseCaves;
import com.hbmspace.world.gen.feature.BasaltSpikesFeature;
import com.hbmspace.world.gen.feature.EveSpikeFeature;
import com.hbmspace.world.gen.feature.MohoVentFeature;
import com.hbmspace.world.gen.feature.PlateauFeature;
import com.hbmspace.world.gen.feature.RubberPlantFeature;
import com.hbmspace.world.gen.feature.SeismicCrackFeature;
import com.hbmspace.world.gen.feature.VoronoiPlatesFeature;
import com.hbmspace.world.gen.feature.WaterCoralFeature;
import com.hbmspace.world.gen.feature.WaterPlantFeature;
import com.hbmspace.world.gen.placement.DescendThroughAirPlacement;
import com.hbmspace.world.gen.structure.ChunkOddsPlacement;
import com.hbmspace.world.gen.structure.TiltedSpirePiece;
import com.hbmspace.world.gen.structure.TiltedSpireStructure;
import com.hbmspace.world.gen.structure.VolcanoPiece;
import com.hbmspace.world.gen.structure.VolcanoStructure;
import com.hbmspace.world.gen.surface.ChanceCondition;
import com.hbmspace.world.gen.surface.FloorAboveCondition;
import com.hbmspace.world.gen.surface.SubstituteRule;
import com.hbmspace.world.gen.surface.SurfaceDepthCondition;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

public final class SpaceWorldgen {

    public static final RegistryHandle<MapCodec<CelestialBiomeSource>> CELESTIAL =
            Registrar.registerBiomeSource("hbmspace:celestial", CelestialBiomeSource.CODEC);

    public static final RegistryHandle<MapCodec<CelestialBiomes>> CELESTIAL_BIOMES =
            Registrar.registerDensityFunctionType(
                    "hbmspace:celestial_biomes", CelestialBiomes.MAP_CODEC);
    public static final RegistryHandle<MapCodec<CelestialRelief>> CELESTIAL_RELIEF =
            Registrar.registerDensityFunctionType(
                    "hbmspace:celestial_relief", CelestialRelief.MAP_CODEC);
    public static final RegistryHandle<MapCodec<LimitNoise>> LIMIT_NOISE =
            Registrar.registerDensityFunctionType("hbmspace:limit_noise", LimitNoise.MAP_CODEC);
    public static final RegistryHandle<MapCodec<BedrockFloor>> BEDROCK_FLOOR =
            Registrar.registerDensityFunctionType("hbmspace:bedrock_floor", BedrockFloor.MAP_CODEC);
    public static final RegistryHandle<MapCodec<NoiseCaves>> NOISE_CAVES =
            Registrar.registerDensityFunctionType("hbmspace:noise_caves", NoiseCaves.MAP_CODEC);

    public static final RegistryHandle<MapCodec<SurfaceDepthCondition>> SURFACE_DEPTH =
            Registrar.registerMaterialCondition(
                    "hbmspace:surface_depth", SurfaceDepthCondition.CODEC);
    public static final RegistryHandle<MapCodec<FloorAboveCondition>> FLOOR_ABOVE =
            Registrar.registerMaterialCondition("hbmspace:floor_above", FloorAboveCondition.CODEC);
    public static final RegistryHandle<MapCodec<ChanceCondition>> CHANCE =
            Registrar.registerMaterialCondition("hbmspace:chance", ChanceCondition.CODEC);
    public static final RegistryHandle<MapCodec<SubstituteRule>> SUBSTITUTE =
            Registrar.registerMaterialRule("hbmspace:substitute", SubstituteRule.CODEC);

    public static final RegistryHandle<CelestialCaveCarver> CAVE =
            Registrar.registerCarver("hbmspace:cave", new CelestialCaveCarver());
    public static final RegistryHandle<ExperimentalCaveCarver> EXPERIMENTAL_CAVE =
            Registrar.registerCarver("hbmspace:experimental_cave", new ExperimentalCaveCarver());
    public static final RegistryHandle<RavineCarver> RAVINE =
            Registrar.registerCarver("hbmspace:ravine", new RavineCarver());
    public static final RegistryHandle<RootCarver> ROOTS =
            Registrar.registerCarver("hbmspace:roots", new RootCarver());

    public static final RegistryHandle<StructureType<VolcanoStructure>> VOLCANO =
            Registrar.registerStructureType("hbmspace:volcano", VolcanoStructure.CODEC);
    public static final RegistryHandle<StructurePieceType> VOLCANO_PIECE =
            Registrar.registerStructurePieceType("hbmspace:volcano", VolcanoPiece::new);
    public static final RegistryHandle<StructureType<TiltedSpireStructure>> TILTED_SPIRE =
            Registrar.registerStructureType("hbmspace:tilted_spire", TiltedSpireStructure.CODEC);
    public static final RegistryHandle<StructurePieceType> TILTED_SPIRE_PIECE =
            Registrar.registerStructurePieceType("hbmspace:tilted_spire", TiltedSpirePiece::new);

    public static final RegistryHandle<StructurePlacementType<ChunkOddsPlacement>> CHUNK_ODDS =
            Registrar.registerStructurePlacementType(
                    "hbmspace:chunk_odds", ChunkOddsPlacement.CODEC);

    public static final RegistryHandle<PlacementModifierType<DescendThroughAirPlacement>>
            DESCEND_THROUGH_AIR =
                    Registrar.registerPlacementModifierType(
                            "hbmspace:descend_through_air", DescendThroughAirPlacement.CODEC);

    public static final RegistryHandle<WaterPlantFeature> WATER_PLANT =
            Registrar.registerFeature("hbmspace:water_plant", new WaterPlantFeature());
    public static final RegistryHandle<WaterCoralFeature> WATER_CORAL =
            Registrar.registerFeature("hbmspace:water_coral", new WaterCoralFeature());
    public static final RegistryHandle<RubberPlantFeature> RUBBER_PLANT =
            Registrar.registerFeature("hbmspace:rubber_plant", new RubberPlantFeature());
    public static final RegistryHandle<EveSpikeFeature> EVE_SPIKE =
            Registrar.registerFeature("hbmspace:eve_spike", new EveSpikeFeature());
    public static final RegistryHandle<MohoVentFeature> MOHO_VENT =
            Registrar.registerFeature("hbmspace:moho_vent", new MohoVentFeature());
    public static final RegistryHandle<VoronoiPlatesFeature> VORONOI_PLATES =
            Registrar.registerFeature("hbmspace:voronoi_plates", new VoronoiPlatesFeature());
    public static final RegistryHandle<SeismicCrackFeature> SEISMIC_CRACK =
            Registrar.registerFeature("hbmspace:seismic_crack", new SeismicCrackFeature());
    public static final RegistryHandle<PlateauFeature> PLATEAU =
            Registrar.registerFeature("hbmspace:plateau", new PlateauFeature());
    public static final RegistryHandle<BasaltSpikesFeature> BASALT_SPIKES =
            Registrar.registerFeature("hbmspace:basalt_spikes", new BasaltSpikesFeature());

    private SpaceWorldgen() {}
}
