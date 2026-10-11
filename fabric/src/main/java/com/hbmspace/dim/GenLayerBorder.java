// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2025 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim;

import com.hbmspace.dim.legacy.layer.GenLayer;
import com.hbmspace.dim.legacy.layer.IntCache;

public class GenLayerBorder extends GenLayer {

    private final BiomeGenBaseCelestial from;
    private final BiomeGenBaseCelestial beside;
    private final BiomeGenBaseCelestial to;

    public GenLayerBorder(
            long seed,
            GenLayer parent,
            BiomeGenBaseCelestial from,
            BiomeGenBaseCelestial beside,
            BiomeGenBaseCelestial to) {
        super(seed);
        this.parent = parent;
        this.from = from;
        this.beside = beside;
        this.to = to;
    }

    @Override
    public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight) {
        int k = 1 + areaWidth + 1;
        int[] aint = this.parent.getInts(areaX - 1, areaY - 1, k, 1 + areaHeight + 1);
        int[] aint1 = IntCache.getIntCache(areaWidth * areaHeight);

        for (int i1 = 0; i1 < areaHeight; ++i1) {
            for (int j1 = 0; j1 < areaWidth; ++j1) {
                this.initChunkSeed(j1 + areaX, i1 + areaY);
                int k1 = aint[j1 + 1 + (i1 + 1) * k];

                if (k1 == from.biomeID) {
                    int l1 = aint[j1 + 1 + (i1 + 1 - 1) * k];
                    int i2 = aint[j1 + 1 + 1 + (i1 + 1) * k];
                    int j2 = aint[j1 + 1 - 1 + (i1 + 1) * k];
                    int k2 = aint[j1 + 1 + (i1 + 1 + 1) * k];
                    if (l1 == beside.biomeID
                            || i2 == beside.biomeID
                            || j2 == beside.biomeID
                            || k2 == beside.biomeID) {
                        k1 = to.biomeID;
                    }
                }

                aint1[j1 + i1 * areaWidth] = k1;
            }
        }

        return aint1;
    }
}
