// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024-2025 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.laythe.genlayer;

import com.hbmspace.dim.laythe.biome.BiomeGenBaseLaythe;
import com.hbmspace.dim.legacy.layer.GenLayer;
import com.hbmspace.dim.legacy.layer.IntCache;

public class GenLayerLaytheIslands extends GenLayer {

    public GenLayerLaytheIslands(long seed, GenLayer parent) {
        super(seed);
        this.parent = parent;
    }

    @Override
    public int[] getInts(int x, int z, int width, int depth) {
        int k = 1 + width + 1;
        int[] aint = this.parent.getInts(x - 1, z - 1, k, 1 + depth + 1);
        int[] aint1 = IntCache.getIntCache(width * depth);
        int island = BiomeGenBaseLaythe.laytheIsland.biomeID;

        for (int i1 = 0; i1 < depth; ++i1) {
            for (int j1 = 0; j1 < width; ++j1) {
                this.initChunkSeed(j1 + x, i1 + z);
                int k1 = aint[j1 + 1 + (i1 + 1) * k];

                if (k1 == BiomeGenBaseLaythe.laytheCoast.biomeID) {
                    int l1 = aint[j1 + 1 + i1 * k];
                    int i2 = aint[j1 + 2 + (i1 + 1) * k];
                    int j2 = aint[j1 + (i1 + 1) * k];
                    int k2 = aint[j1 + 1 + (i1 + 2) * k];
                    boolean beside = l1 == island || i2 == island || j2 == island || k2 == island;

                    if (!(beside && this.nextInt(50) == 0)) {
                        k1 = BiomeGenBaseLaythe.laytheOcean.biomeID;
                    }
                } else if (k1 == island) {
                    k1 = this.nextInt(3) == 0 ? BiomeGenBaseLaythe.laytheCoast.biomeID : k1;
                }

                aint1[j1 + i1 * width] = k1;
            }
        }

        return aint1;
    }
}
