// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024-2025 George Paton
// SPDX-FileCopyrightText: 2023 Pu-238
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.minmus;

import com.hbmspace.blocks.ModBlocksSpace;
import com.hbmspace.dim.BiomeGenBaseCelestial;
import com.hbmspace.dim.GenLayerBorder;
import com.hbmspace.dim.GenLayerCelestialBiomes;
import com.hbmspace.dim.GenLayerDiversify;
import com.hbmspace.dim.SpaceDimensions;
import com.hbmspace.dim.WorldChunkManagerCelestial.BiomeGenLayers;
import com.hbmspace.dim.WorldProviderCelestial;
import com.hbmspace.dim.legacy.layer.GenLayer;
import com.hbmspace.dim.legacy.layer.GenLayerFuzzyZoom;
import com.hbmspace.dim.legacy.layer.GenLayerSmooth;
import com.hbmspace.dim.legacy.layer.GenLayerVoronoiZoom;
import com.hbmspace.dim.legacy.layer.GenLayerZoom;
import com.hbmspace.dim.minmus.biome.BiomeGenBaseMinmus;
import net.minecraft.world.level.block.state.BlockState;

public class WorldProviderMinmus extends WorldProviderCelestial {

    public WorldProviderMinmus() {
        super(SpaceDimensions.MINMUS);
    }

    @Override
    public BlockState getStone() {
        return ModBlocksSpace.MINMUS_STONE.get().defaultBlockState();
    }

    public static BiomeGenLayers createBiomeGenerators(long seed) {
        BiomeGenBaseCelestial canyon = BiomeGenBaseMinmus.minmusCanyon;
        BiomeGenBaseCelestial plains = BiomeGenBaseMinmus.minmusPlains;
        GenLayer biomes = new GenLayerCelestialBiomes(seed, canyon, plains);

        biomes = new GenLayerFuzzyZoom(2000L, biomes);
        biomes = new GenLayerZoom(2001L, biomes);
        biomes = new GenLayerDiversify(1000L, biomes, 2, canyon, plains);
        biomes = new GenLayerZoom(1000L, biomes);
        biomes = new GenLayerDiversify(1001L, biomes, 2, canyon, plains);
        biomes = new GenLayerZoom(1001L, biomes);
        biomes = new GenLayerBorder(3000L, biomes, canyon, plains, canyon);
        biomes = new GenLayerZoom(1003L, biomes);
        biomes = new GenLayerSmooth(700L, biomes);
        biomes = new GenLayerBorder(200L, biomes, plains, canyon, plains);
        biomes = new GenLayerZoom(1006L, biomes);

        GenLayer genLayerVoronoiZoom = new GenLayerVoronoiZoom(10L, biomes);

        return new BiomeGenLayers(biomes, genLayerVoronoiZoom, seed);
    }
}
