// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2025 George Paton
// SPDX-FileCopyrightText: 2024 JamesH_2
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.laythe.genlayer;

import com.hbmspace.dim.laythe.biome.BiomeGenBaseLaythe;
import com.hbmspace.dim.legacy.layer.GenLayer;
import com.hbmspace.dim.legacy.layer.IntCache;

public class GenLayerLaythePolar extends GenLayer {

    public GenLayerLaythePolar(long seed, GenLayer parent) {
        super(seed);
        this.parent = parent;
    }

    @Override
    public int[] getInts(int x, int z, int width, int depth) {
        int i1 = x - 1;
        int j1 = z - 1;
        int k1 = width + 2;
        int l1 = depth + 2;
        int[] aint = this.parent.getInts(i1, j1, k1, l1);
        int[] aint1 = IntCache.getIntCache(width * depth);
        int ocean = BiomeGenBaseLaythe.laytheOcean.biomeID;

        for (int i2 = 0; i2 < depth; ++i2) {
            for (int j2 = 0; j2 < width; ++j2) {
                int k2 = aint[j2 + i2 * k1];
                int l2 = aint[j2 + 2 + i2 * k1];
                int i3 = aint[j2 + (i2 + 2) * k1];
                int j3 = aint[j2 + 2 + (i2 + 2) * k1];
                int k3 = aint[j2 + 1 + (i2 + 1) * k1];
                this.initChunkSeed(j2 + x, i2 + z);
                if (k3 == ocean && k2 == ocean && l2 == ocean && i3 == ocean && j3 == ocean) {
                    aint1[j2 + i2 * width] = BiomeGenBaseLaythe.laythePolar.biomeID;
                } else {
                    aint1[j2 + i2 * width] = k3;
                }
            }
        }

        return aint1;
    }
}
