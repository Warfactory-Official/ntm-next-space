// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.legacy;

import java.util.Random;
import net.minecraft.util.Mth;

public final class NoiseGeneratorOctaves {

    private final NoiseGeneratorImproved[] generatorCollection;
    private final int octaves;

    public NoiseGeneratorOctaves(Random rand, int octaves) {
        this.octaves = octaves;
        this.generatorCollection = new NoiseGeneratorImproved[octaves];

        for (int j = 0; j < octaves; ++j) {
            this.generatorCollection[j] = new NoiseGeneratorImproved(rand);
        }
    }

    public double[] generateNoiseOctaves(
            double[] noiseArray,
            int xOffset,
            int yOffset,
            int zOffset,
            int xSize,
            int ySize,
            int zSize,
            double xScale,
            double yScale,
            double zScale) {
        if (noiseArray == null) {
            noiseArray = new double[xSize * ySize * zSize];
        } else {
            for (int k1 = 0; k1 < noiseArray.length; ++k1) {
                noiseArray[k1] = 0.0D;
            }
        }

        double scale = 1.0D;

        for (int octave = 0; octave < this.octaves; ++octave) {
            double x = (double) xOffset * scale * xScale;
            double y = (double) yOffset * scale * yScale;
            double z = (double) zOffset * scale * zScale;
            long xi = Mth.lfloor(x);
            long zi = Mth.lfloor(z);
            x -= (double) xi;
            z -= (double) zi;
            xi %= 16777216L;
            zi %= 16777216L;
            x += (double) xi;
            z += (double) zi;
            this.generatorCollection[octave].populateNoiseArray(
                    noiseArray,
                    x,
                    y,
                    z,
                    xSize,
                    ySize,
                    zSize,
                    xScale * scale,
                    yScale * scale,
                    zScale * scale,
                    scale);
            scale /= 2.0D;
        }

        return noiseArray;
    }

    public double[] generateNoiseOctaves(
            double[] noiseArray,
            int xOffset,
            int zOffset,
            int xSize,
            int zSize,
            double xScale,
            double zScale,
            double unused) {
        return this.generateNoiseOctaves(
                noiseArray, xOffset, 10, zOffset, xSize, 1, zSize, xScale, 1.0D, zScale);
    }
}
