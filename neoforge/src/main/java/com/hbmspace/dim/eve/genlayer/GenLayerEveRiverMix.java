// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.eve.genlayer;

import com.hbmspace.dim.eve.biome.BiomeGenBaseEve;
import com.hbmspace.dim.legacy.layer.GenLayer;
import com.hbmspace.dim.legacy.layer.GenLayerRiver;
import com.hbmspace.dim.legacy.layer.IntCache;

public class GenLayerEveRiverMix extends GenLayer {

    private final GenLayer biomePatternGeneratorChain;
    private final GenLayer riverPatternGeneratorChain;

    public GenLayerEveRiverMix(long seed, GenLayer biomes, GenLayer rivers) {
        super(seed);
        this.biomePatternGeneratorChain = biomes;
        this.riverPatternGeneratorChain = rivers;
    }

    @Override
    public void initWorldGenSeed(long seed) {
        this.biomePatternGeneratorChain.initWorldGenSeed(seed);
        this.riverPatternGeneratorChain.initWorldGenSeed(seed);
        super.initWorldGenSeed(seed);
    }

    @Override
    public int[] getInts(int x, int z, int width, int length) {
        int[] inputBiomeIds = this.biomePatternGeneratorChain.getInts(x, z, width, length);
        int[] riverBiomeIds = this.riverPatternGeneratorChain.getInts(x, z, width, length);
        int[] outputBiomeIds = IntCache.getIntCache(width * length);

        for (int i = 0; i < width * length; ++i) {
            if (inputBiomeIds[i] != BiomeGenBaseEve.eveOcean.biomeID
                    && riverBiomeIds[i] == GenLayerRiver.RIVER) {
                outputBiomeIds[i] = BiomeGenBaseEve.eveRiver.biomeID;
            } else {
                outputBiomeIds[i] = inputBiomeIds[i];
            }
        }

        return outputBiomeIds;
    }
}
