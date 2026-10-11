// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.legacy.layer;

import com.hbmspace.dim.BiomeGenBaseCelestial;

public abstract class GenLayer {
    private static final int OCEAN = 0;
    private static final int DEEP_OCEAN = 24;
    private static final int FROZEN_OCEAN = 10;

    private long worldGenSeed;
    protected GenLayer parent;
    private long chunkSeed;
    protected long baseSeed;

    public GenLayer(long seed) {
        this.baseSeed = seed;
        this.baseSeed *= this.baseSeed * 6364136223846793005L + 1442695040888963407L;
        this.baseSeed += seed;
        this.baseSeed *= this.baseSeed * 6364136223846793005L + 1442695040888963407L;
        this.baseSeed += seed;
        this.baseSeed *= this.baseSeed * 6364136223846793005L + 1442695040888963407L;
        this.baseSeed += seed;
    }

    public void initWorldGenSeed(long seed) {
        this.worldGenSeed = seed;

        if (this.parent != null) {
            this.parent.initWorldGenSeed(seed);
        }

        this.worldGenSeed *= this.worldGenSeed * 6364136223846793005L + 1442695040888963407L;
        this.worldGenSeed += this.baseSeed;
        this.worldGenSeed *= this.worldGenSeed * 6364136223846793005L + 1442695040888963407L;
        this.worldGenSeed += this.baseSeed;
        this.worldGenSeed *= this.worldGenSeed * 6364136223846793005L + 1442695040888963407L;
        this.worldGenSeed += this.baseSeed;
    }

    public void initChunkSeed(long x, long z) {
        this.chunkSeed = this.worldGenSeed;
        this.chunkSeed *= this.chunkSeed * 6364136223846793005L + 1442695040888963407L;
        this.chunkSeed += x;
        this.chunkSeed *= this.chunkSeed * 6364136223846793005L + 1442695040888963407L;
        this.chunkSeed += z;
        this.chunkSeed *= this.chunkSeed * 6364136223846793005L + 1442695040888963407L;
        this.chunkSeed += x;
        this.chunkSeed *= this.chunkSeed * 6364136223846793005L + 1442695040888963407L;
        this.chunkSeed += z;
    }

    protected int nextInt(int bound) {
        int j = (int) ((this.chunkSeed >> 24) % (long) bound);

        if (j < 0) {
            j += bound;
        }

        this.chunkSeed *= this.chunkSeed * 6364136223846793005L + 1442695040888963407L;
        this.chunkSeed += this.worldGenSeed;
        return j;
    }

    protected long nextLong(long bound) {
        long j = (this.chunkSeed >> 24) % bound;

        if (j < 0) {
            j += bound;
        }

        this.chunkSeed *= this.chunkSeed * 6364136223846793005L + 1442695040888963407L;
        this.chunkSeed += this.worldGenSeed;
        return j;
    }

    public abstract int[] getInts(int x, int z, int width, int depth);

    protected static boolean compareBiomesById(int a, int b) {
        if (a == b) {
            return true;
        }

        BiomeGenBaseCelestial biomeA = BiomeGenBaseCelestial.getBiomeOrNull(a);
        BiomeGenBaseCelestial biomeB = BiomeGenBaseCelestial.getBiomeOrNull(b);
        return biomeA != null && biomeB != null && biomeA.getClass() == biomeB.getClass();
    }

    protected static boolean isBiomeOceanic(int id) {
        return id == OCEAN || id == DEEP_OCEAN || id == FROZEN_OCEAN;
    }

    protected int selectRandom(int... choices) {
        return choices[this.nextInt(choices.length)];
    }

    protected int selectModeOrRandom(int a, int b, int c, int d) {
        return b == c && c == d
                ? b
                : (a == b && a == c
                        ? a
                        : (a == b && a == d
                                ? a
                                : (a == c && a == d
                                        ? a
                                        : (a == b && c != d
                                                ? a
                                                : (a == c && b != d
                                                        ? a
                                                        : (a == d && b != c
                                                                ? a
                                                                : (b == c && a != d
                                                                        ? b
                                                                        : (b == d && a != c
                                                                                ? b
                                                                                : (c == d && a != b
                                                                                        ? c
                                                                                        : this
                                                                                                .selectRandom(
                                                                                                        a,
                                                                                                        b,
                                                                                                        c,
                                                                                                        d))))))))));
    }
}
