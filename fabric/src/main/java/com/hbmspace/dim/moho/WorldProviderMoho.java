// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024-2025 George Paton
// SPDX-FileCopyrightText: 2023 Pu-238
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.moho;

import com.hbmspace.blocks.ModBlocksSpace;
import com.hbmspace.dim.GenLayerCelestialBiomes;
import com.hbmspace.dim.GenLayerDiversify;
import com.hbmspace.dim.SpaceDimensions;
import com.hbmspace.dim.WorldChunkManagerCelestial.BiomeGenLayers;
import com.hbmspace.dim.WorldProviderCelestial;
import com.hbmspace.dim.legacy.layer.GenLayer;
import com.hbmspace.dim.legacy.layer.GenLayerFuzzyZoom;
import com.hbmspace.dim.legacy.layer.GenLayerRiver;
import com.hbmspace.dim.legacy.layer.GenLayerRiverMix;
import com.hbmspace.dim.legacy.layer.GenLayerSmooth;
import com.hbmspace.dim.legacy.layer.GenLayerVoronoiZoom;
import com.hbmspace.dim.legacy.layer.GenLayerZoom;
import com.hbmspace.dim.moho.biome.BiomeGenBaseMoho;
import com.hbmspace.dim.moho.genlayer.GenLayerMohoPlateauExtend;
import net.minecraft.world.level.block.state.BlockState;

public class WorldProviderMoho extends WorldProviderCelestial {

    public WorldProviderMoho() {
        super(SpaceDimensions.MOHO);
    }

    @Override
    public BlockState getStone() {
        return ModBlocksSpace.MOHO_STONE.get().defaultBlockState();
    }

    public static BiomeGenLayers createBiomeGenerators(long seed) {
        GenLayer biomes =
                new GenLayerCelestialBiomes(
                        seed,
                        BiomeGenBaseMoho.mohoCrag,
                        BiomeGenBaseMoho.mohoCrag,
                        BiomeGenBaseMoho.mohoLavaSea);

        biomes = new GenLayerFuzzyZoom(2000L, biomes);
        biomes = new GenLayerDiversify(1234L, biomes, 6, BiomeGenBaseMoho.mohoBasalt);
        biomes = new GenLayerZoom(2001L, biomes);
        biomes = new GenLayerMohoPlateauExtend(9944L, biomes);
        biomes = new GenLayerZoom(1006L, biomes);
        biomes = new GenLayerSmooth(700L, biomes);
        biomes = new GenLayerZoom(1000L, biomes);
        biomes = new GenLayerZoom(107L, biomes);

        GenLayer genlayerVoronoiZoom = new GenLayerVoronoiZoom(10L, biomes);

        GenLayer genlayerRiver = new GenLayerRiver(1004L, biomes);
        genlayerRiver = new GenLayerZoom(105L, genlayerRiver);
        genlayerRiver = new GenLayerZoom(106L, genlayerRiver);

        GenLayer genlayerRiverMix = new GenLayerRiverMix(100L, biomes, genlayerRiver);

        return new BiomeGenLayers(genlayerRiverMix, genlayerVoronoiZoom, seed);
    }
}
