// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024 George Paton
// SPDX-FileCopyrightText: 2023 Pu-238
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.eve.genlayer;

import com.hbmspace.dim.BiomeGenBaseCelestial;
import com.hbmspace.dim.eve.biome.BiomeGenBaseEve;
import com.hbmspace.dim.legacy.layer.GenLayer;
import com.hbmspace.dim.legacy.layer.IntCache;

public class GenLayerEveBiomes extends GenLayer {

    private static final BiomeGenBaseCelestial[] BIOMES = {
        BiomeGenBaseEve.evePlains, BiomeGenBaseEve.eveOcean, BiomeGenBaseEve.eveMountains
    };
    private static final BiomeGenBaseCelestial[] RARE_BIOMES = {BiomeGenBaseEve.eveSeismicPlains};

    public GenLayerEveBiomes(long seed) {
        super(seed);
    }

    @Override
    public int[] getInts(int x, int z, int width, int depth) {
        int[] dest = IntCache.getIntCache(width * depth);

        for (int dz = 0; dz < depth; dz++) {
            for (int dx = 0; dx < width; dx++) {
                initChunkSeed(dx + x, dz + z);

                if (nextInt(20) == 0) {
                    dest[dx + dz * width] = RARE_BIOMES[nextInt(RARE_BIOMES.length)].biomeID;
                } else {
                    dest[dx + dz * width] = BIOMES[nextInt(BIOMES.length)].biomeID;
                }
            }
        }
        return dest;
    }
}
