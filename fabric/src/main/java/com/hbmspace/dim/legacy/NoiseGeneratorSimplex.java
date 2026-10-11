// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024 George Paton
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.legacy;

import java.util.Random;

public final class NoiseGeneratorSimplex {

    private static final int[][] GRADIENTS = {
        {1, 1, 0},
        {-1, 1, 0},
        {1, -1, 0},
        {-1, -1, 0},
        {1, 0, 1},
        {-1, 0, 1},
        {1, 0, -1},
        {-1, 0, -1},
        {0, 1, 1},
        {0, -1, 1},
        {0, 1, -1},
        {0, -1, -1}
    };
    private static final double SQRT_3 = Math.sqrt(3.0D);
    private static final double F2 = 0.5D * (SQRT_3 - 1.0D);
    private static final double G2 = (3.0D - SQRT_3) / 6.0D;

    private final int[] permutations;
    public final double xo;
    public final double yo;
    public final double zo;

    public NoiseGeneratorSimplex(Random rand) {
        this.permutations = new int[512];
        this.xo = rand.nextDouble() * 256.0D;
        this.yo = rand.nextDouble() * 256.0D;
        this.zo = rand.nextDouble() * 256.0D;

        for (int i = 0; i < 256; i++) {
            this.permutations[i] = i;
        }

        for (int i = 0; i < 256; ++i) {
            int j = rand.nextInt(256 - i) + i;
            int k = this.permutations[i];
            this.permutations[i] = this.permutations[j];
            this.permutations[j] = k;
            this.permutations[i + 256] = this.permutations[i];
        }
    }

    private static int fastFloor(double value) {
        return value > 0.0D ? (int) value : (int) value - 1;
    }

    private static double dot(int[] gradient, double x, double y) {
        return (double) gradient[0] * x + (double) gradient[1] * y;
    }

    public double getValue(double x, double y) {
        double s = (x + y) * F2;
        int i = fastFloor(x + s);
        int j = fastFloor(y + s);
        double t = (double) (i + j) * G2;
        double x0 = x - ((double) i - t);
        double y0 = y - ((double) j - t);
        int i1;
        int j1;

        if (x0 > y0) {
            i1 = 1;
            j1 = 0;
        } else {
            i1 = 0;
            j1 = 1;
        }

        double x1 = x0 - (double) i1 + G2;
        double y1 = y0 - (double) j1 + G2;
        double x2 = x0 - 1.0D + 2.0D * G2;
        double y2 = y0 - 1.0D + 2.0D * G2;
        int ii = i & 255;
        int jj = j & 255;
        int gi0 = this.permutations[ii + this.permutations[jj]] % 12;
        int gi1 = this.permutations[ii + i1 + this.permutations[jj + j1]] % 12;
        int gi2 = this.permutations[ii + 1 + this.permutations[jj + 1]] % 12;

        return 70.0D * (corner(gi0, x0, y0) + corner(gi1, x1, y1) + corner(gi2, x2, y2));
    }

    private static double corner(int gradient, double x, double y) {
        double t = 0.5D - x * x - y * y;
        if (t < 0.0D) return 0.0D;
        t *= t;
        return t * t * dot(GRADIENTS[gradient], x, y);
    }

    public void add(
            double[] noiseArray,
            double xOffset,
            double yOffset,
            int xSize,
            int ySize,
            double xScale,
            double yScale,
            double amplitude) {
        int index = 0;

        for (int iy = 0; iy < ySize; ++iy) {
            double y = (yOffset + (double) iy) * yScale + this.yo;

            for (int ix = 0; ix < xSize; ++ix) {
                double x = (xOffset + (double) ix) * xScale + this.xo;
                noiseArray[index++] += getValue(x, y) * amplitude;
            }
        }
    }
}
