// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024 JamesH_2
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.tekto.genlayer;

import com.hbmspace.dim.legacy.layer.GenLayer;
import com.hbmspace.dim.legacy.layer.GenLayerRiver;
import com.hbmspace.dim.legacy.layer.IntCache;
import com.hbmspace.dim.tekto.biome.BiomeGenBaseTekto;

public class GenLayerTektoRiverMix extends GenLayer {

    private final GenLayer biomePatternGeneratorChain;
    private final GenLayer riverPatternGeneratorChain;

    public GenLayerTektoRiverMix(long seed, GenLayer biomes, GenLayer rivers) {
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
        int plains = BiomeGenBaseTekto.polyvinylPlains.biomeID;
        int river = BiomeGenBaseTekto.tetrachloricRiver.biomeID;

        for (int i1 = 0; i1 < width * length; ++i1) {
            outputBiomeIds[i1] =
                    inputBiomeIds[i1] != plains && riverBiomeIds[i1] == GenLayerRiver.RIVER
                            ? river
                            : inputBiomeIds[i1];
        }

        return outputBiomeIds;
    }
}
