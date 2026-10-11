// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.legacy;

import java.util.Random;

public final class NoiseGeneratorPerlin {

    private final NoiseGeneratorSimplex[] octaves;
    private final int octaveCount;

    public NoiseGeneratorPerlin(Random rand, int octaveCount) {
        this.octaveCount = octaveCount;
        this.octaves = new NoiseGeneratorSimplex[octaveCount];

        for (int j = 0; j < octaveCount; ++j) {
            this.octaves[j] = new NoiseGeneratorSimplex(rand);
        }
    }

    public double getValue(double x, double z) {
        double value = 0.0D;
        double scale = 1.0D;

        for (int i = 0; i < this.octaveCount; ++i) {
            value += this.octaves[i].getValue(x * scale, z * scale) / scale;
            scale /= 2.0D;
        }

        return value;
    }

    public double[] getRegion(
            double[] noiseArray,
            double xOffset,
            double zOffset,
            int xSize,
            int zSize,
            double xScale,
            double zScale,
            double frequencyFactor) {
        return getRegion(
                noiseArray, xOffset, zOffset, xSize, zSize, xScale, zScale, frequencyFactor, 0.5D);
    }

    public double[] getRegion(
            double[] noiseArray,
            double xOffset,
            double zOffset,
            int xSize,
            int zSize,
            double xScale,
            double zScale,
            double frequencyFactor,
            double amplitudeFactor) {
        if (noiseArray != null && noiseArray.length >= xSize * zSize) {
            for (int k = 0; k < noiseArray.length; ++k) {
                noiseArray[k] = 0.0D;
            }
        } else {
            noiseArray = new double[xSize * zSize];
        }

        double amplitude = 1.0D;
        double frequency = 1.0D;

        for (int l = 0; l < this.octaveCount; ++l) {
            this.octaves[l].add(
                    noiseArray,
                    xOffset,
                    zOffset,
                    xSize,
                    zSize,
                    xScale * frequency * amplitude,
                    zScale * frequency * amplitude,
                    0.55D / amplitude);
            frequency *= frequencyFactor;
            amplitude *= amplitudeFactor;
        }

        return noiseArray;
    }
}
