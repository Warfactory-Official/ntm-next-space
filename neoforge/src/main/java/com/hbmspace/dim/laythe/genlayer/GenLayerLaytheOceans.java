// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024 George Paton
// SPDX-FileCopyrightText: 2023 JamesH_2
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.laythe.genlayer;

import com.hbmspace.dim.laythe.biome.BiomeGenBaseLaythe;
import com.hbmspace.dim.legacy.layer.GenLayer;
import com.hbmspace.dim.legacy.layer.IntCache;

public class GenLayerLaytheOceans extends GenLayer {

    private final int targetBiome;

    public GenLayerLaytheOceans(long seed, GenLayer parent, int targetBiome) {
        super(seed);
        this.parent = parent;
        this.targetBiome = targetBiome;
    }

    @Override
    public int[] getInts(int x, int z, int width, int depth) {
        int k = 1 + width + 1;
        int[] aint = this.parent.getInts(x - 1, z - 1, k, 1 + depth + 1);
        int[] aint1 = IntCache.getIntCache(width * depth);

        for (int i1 = 0; i1 < depth; ++i1) {
            for (int j1 = 0; j1 < width; ++j1) {
                this.initChunkSeed(j1 + x, i1 + z);
                int k1 = aint[j1 + 1 + (i1 + 1) * k];

                if (k1 == BiomeGenBaseLaythe.laytheOcean.biomeID) {
                    int l1 = aint[j1 + 1 + i1 * k];
                    int i2 = aint[j1 + 2 + (i1 + 1) * k];
                    int j2 = aint[j1 + (i1 + 1) * k];
                    int k2 = aint[j1 + 1 + (i1 + 2) * k];
                    if (l1 == targetBiome
                            || i2 == targetBiome
                            || j2 == targetBiome
                            || k2 == targetBiome) {
                        k1 = BiomeGenBaseLaythe.laytheCoast.biomeID;
                    }
                }

                aint1[j1 + i1 * width] = k1;
            }
        }

        return aint1;
    }
}
