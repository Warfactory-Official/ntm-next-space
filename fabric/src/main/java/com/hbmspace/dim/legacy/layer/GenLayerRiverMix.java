// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.legacy.layer;

public class GenLayerRiverMix extends GenLayer {
    private static final int OCEAN = 0;
    private static final int DEEP_OCEAN = 24;
    private static final int ICE_PLAINS = 12;
    private static final int FROZEN_RIVER = 11;
    private static final int MUSHROOM_ISLAND = 14;
    private static final int MUSHROOM_ISLAND_SHORE = 15;

    private final GenLayer biomePatternGeneratorChain;
    private final GenLayer riverPatternGeneratorChain;

    public GenLayerRiverMix(long seed, GenLayer biomes, GenLayer rivers) {
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
    public int[] getInts(int x, int z, int width, int depth) {
        int[] aint = this.biomePatternGeneratorChain.getInts(x, z, width, depth);
        int[] aint1 = this.riverPatternGeneratorChain.getInts(x, z, width, depth);
        int[] aint2 = IntCache.getIntCache(width * depth);

        for (int i1 = 0; i1 < width * depth; ++i1) {
            if (aint[i1] != OCEAN && aint[i1] != DEEP_OCEAN) {
                if (aint1[i1] == GenLayerRiver.RIVER) {
                    if (aint[i1] == ICE_PLAINS) {
                        aint2[i1] = FROZEN_RIVER;
                    } else if (aint[i1] != MUSHROOM_ISLAND && aint[i1] != MUSHROOM_ISLAND_SHORE) {
                        aint2[i1] = aint1[i1] & 255;
                    } else {
                        aint2[i1] = MUSHROOM_ISLAND_SHORE;
                    }
                } else {
                    aint2[i1] = aint[i1];
                }
            } else {
                aint2[i1] = aint[i1];
            }
        }

        return aint2;
    }
}
