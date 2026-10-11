// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024-2025 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim;

import com.hbmspace.dim.legacy.layer.GenLayer;
import com.hbmspace.dim.legacy.layer.IntCache;

public class GenLayerDiversify extends GenLayer {

    private final BiomeGenBaseCelestial[] biomes;
    private final int chance;

    public GenLayerDiversify(
            long seed, GenLayer parent, int chance, BiomeGenBaseCelestial... biomes) {
        super(seed);
        this.parent = parent;
        this.biomes = biomes;
        this.chance = chance;
    }

    @Override
    public int[] getInts(int x, int z, int width, int depth) {
        int[] input = this.parent.getInts(x, z, width, depth);
        int[] output = IntCache.getIntCache(width * depth);

        for (int zOut = 0; zOut < depth; zOut++) {
            for (int xOut = 0; xOut < width; xOut++) {
                int i = xOut + zOut * width;
                int center = input[i];
                initChunkSeed(xOut + x, zOut + z);
                if (nextInt(chance) == 0) {
                    output[i] = biomes[nextInt(biomes.length)].biomeID;
                } else {
                    output[i] = center;
                }
            }
        }
        return output;
    }
}
