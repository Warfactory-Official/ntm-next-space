// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024-2025 George Paton
// SPDX-FileCopyrightText: 2023 JamesH_2
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.laythe;

import com.hbmspace.dim.BiomeGenBaseCelestial;
import com.hbmspace.dim.GenLayerCelestialBiomes;
import com.hbmspace.dim.GenLayerDiversify;
import com.hbmspace.dim.SpaceDimensions;
import com.hbmspace.dim.WorldChunkManagerCelestial.BiomeGenLayers;
import com.hbmspace.dim.WorldProviderCelestial;
import com.hbmspace.dim.laythe.biome.BiomeGenBaseLaythe;
import com.hbmspace.dim.laythe.genlayer.GenLayerLaytheIslands;
import com.hbmspace.dim.laythe.genlayer.GenLayerLaytheOceans;
import com.hbmspace.dim.laythe.genlayer.GenLayerLaythePolar;
import com.hbmspace.dim.legacy.layer.GenLayer;
import com.hbmspace.dim.legacy.layer.GenLayerSmooth;
import com.hbmspace.dim.legacy.layer.GenLayerVoronoiZoom;
import com.hbmspace.dim.legacy.layer.GenLayerZoom;

public class WorldProviderLaythe extends WorldProviderCelestial {

    public WorldProviderLaythe() {
        super(SpaceDimensions.LAYTHE);
    }

    @Override
    public boolean hasLife() {
        return true;
    }

    @Override
    public int getWaterOpacity() {
        return 1;
    }

    public static BiomeGenLayers createBiomeGenerators(long seed) {
        BiomeGenBaseCelestial island = BiomeGenBaseLaythe.laytheIsland;
        BiomeGenBaseCelestial ocean = BiomeGenBaseLaythe.laytheOcean;
        BiomeGenBaseCelestial coast = BiomeGenBaseLaythe.laytheCoast;

        GenLayer biomes = new GenLayerCelestialBiomes(seed, island, ocean, coast);
        biomes = new GenLayerLaythePolar(1000L, biomes);

        biomes = new GenLayerZoom(1040L, biomes);
        biomes = GenLayerZoom.magnify(1000L, biomes, 1);
        biomes = new GenLayerLaythePolar(1300L, biomes);

        biomes = new GenLayerDiversify(1000L, biomes, 2, island, ocean, coast);
        biomes = new GenLayerLaytheOceans(56000L, biomes, island.biomeID);
        biomes = new GenLayerLaytheIslands(200L, biomes);

        biomes = new GenLayerZoom(1200L, biomes);
        biomes = new GenLayerZoom(1001L, biomes);

        biomes = new GenLayerLaytheOceans(4000L, biomes, island.biomeID);
        biomes = new GenLayerLaytheOceans(4001L, biomes, coast.biomeID);

        biomes = new GenLayerZoom(1003L, biomes);
        biomes = new GenLayerLaytheOceans(4342L, biomes, coast.biomeID);
        biomes = new GenLayerSmooth(700L, biomes);

        biomes = new GenLayerZoom(1006L, biomes);

        GenLayer genLayerVoronoiZoom = new GenLayerVoronoiZoom(10L, biomes);

        return new BiomeGenLayers(biomes, genLayerVoronoiZoom, seed);
    }
}
