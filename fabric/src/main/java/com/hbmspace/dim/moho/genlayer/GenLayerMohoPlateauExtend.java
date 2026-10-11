// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2025 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.moho.genlayer;

import com.hbmspace.dim.legacy.layer.GenLayer;
import com.hbmspace.dim.legacy.layer.IntCache;
import com.hbmspace.dim.moho.biome.BiomeGenBaseMoho;

public class GenLayerMohoPlateauExtend extends GenLayer {

    public GenLayerMohoPlateauExtend(long seed, GenLayer genLayer) {
        super(seed);
        this.parent = genLayer;
    }

    @Override
    public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight) {
        int k = 1 + areaWidth + 1;
        int[] aint = this.parent.getInts(areaX - 1, areaY - 1, k, 1 + areaHeight + 1);
        int[] aint1 = IntCache.getIntCache(areaWidth * areaHeight);
        int crag = BiomeGenBaseMoho.mohoCrag.biomeID;
        int plateau = BiomeGenBaseMoho.mohoPlateau.biomeID;

        for (int i1 = 0; i1 < areaHeight; ++i1) {
            for (int j1 = 0; j1 < areaWidth; ++j1) {
                this.initChunkSeed(j1 + areaX, i1 + areaY);
                int k1 = aint[j1 + 1 + (i1 + 1) * k];

                if (k1 == crag) {
                    int l1 = aint[j1 + 1 + (i1 + 1 - 1) * k];
                    int i2 = aint[j1 + 1 + 1 + (i1 + 1) * k];
                    int j2 = aint[j1 + 1 - 1 + (i1 + 1) * k];
                    int k2 = aint[j1 + 1 + (i1 + 1 + 1) * k];
                    boolean flag =
                            (l1 == crag || l1 == plateau)
                                    && (i2 == crag || i2 == plateau)
                                    && (j2 == crag || j2 == plateau)
                                    && (k2 == crag || k2 == plateau);
                    if (flag) {
                        k1 = plateau;
                    }
                }

                aint1[j1 + i1 * areaWidth] = k1;
            }
        }

        return aint1;
    }
}
