// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.legacy;

import java.util.Random;

public final class NoiseGeneratorImproved {

    private final int[] permutations;
    public final double xCoord;
    public final double yCoord;
    public final double zCoord;
    private static final double[] GRAD_X = {
        1.0D, -1.0D, 1.0D, -1.0D, 1.0D, -1.0D, 1.0D, -1.0D, 0.0D, 0.0D, 0.0D, 0.0D, 1.0D, 0.0D,
        -1.0D, 0.0D
    };
    private static final double[] GRAD_Y = {
        1.0D, 1.0D, -1.0D, -1.0D, 0.0D, 0.0D, 0.0D, 0.0D, 1.0D, -1.0D, 1.0D, -1.0D, 1.0D, -1.0D,
        1.0D, -1.0D
    };
    private static final double[] GRAD_Z = {
        0.0D, 0.0D, 0.0D, 0.0D, 1.0D, 1.0D, -1.0D, -1.0D, 1.0D, 1.0D, -1.0D, -1.0D, 0.0D, 1.0D,
        0.0D, -1.0D
    };
    private static final double[] GRAD_2X = {
        1.0D, -1.0D, 1.0D, -1.0D, 1.0D, -1.0D, 1.0D, -1.0D, 0.0D, 0.0D, 0.0D, 0.0D, 1.0D, 0.0D,
        -1.0D, 0.0D
    };
    private static final double[] GRAD_2Z = {
        0.0D, 0.0D, 0.0D, 0.0D, 1.0D, 1.0D, -1.0D, -1.0D, 1.0D, 1.0D, -1.0D, -1.0D, 0.0D, 1.0D,
        0.0D, -1.0D
    };

    public NoiseGeneratorImproved(Random rand) {
        this.permutations = new int[512];
        this.xCoord = rand.nextDouble() * 256.0D;
        this.yCoord = rand.nextDouble() * 256.0D;
        this.zCoord = rand.nextDouble() * 256.0D;

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

    private static double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    private static double grad2(int hash, double x, double z) {
        int j = hash & 15;
        return GRAD_2X[j] * x + GRAD_2Z[j] * z;
    }

    private static double grad(int hash, double x, double y, double z) {
        int j = hash & 15;
        return GRAD_X[j] * x + GRAD_Y[j] * y + GRAD_Z[j] * z;
    }

    public void populateNoiseArray(
            double[] noiseArray,
            double xOffset,
            double yOffset,
            double zOffset,
            int xSize,
            int ySize,
            int zSize,
            double xScale,
            double yScale,
            double zScale,
            double noiseScale) {
        if (ySize == 1) {
            int index = 0;
            double amplitude = 1.0D / noiseScale;

            for (int ix = 0; ix < xSize; ++ix) {
                double x = xOffset + (double) ix * xScale + this.xCoord;
                int xi = (int) x;

                if (x < (double) xi) {
                    --xi;
                }

                int xHash = xi & 255;
                x -= xi;
                double xFade = x * x * x * (x * (x * 6.0D - 15.0D) + 10.0D);

                for (int iz = 0; iz < zSize; ++iz) {
                    double z = zOffset + (double) iz * zScale + this.zCoord;
                    int zi = (int) z;

                    if (z < (double) zi) {
                        --zi;
                    }

                    int zHash = zi & 255;
                    z -= zi;
                    double zFade = z * z * z * (z * (z * 6.0D - 15.0D) + 10.0D);
                    int a = this.permutations[xHash];
                    int aa = this.permutations[a] + zHash;
                    int b = this.permutations[xHash + 1];
                    int ba = this.permutations[b] + zHash;
                    double lower =
                            lerp(
                                    xFade,
                                    grad2(this.permutations[aa], x, z),
                                    grad(this.permutations[ba], x - 1.0D, 0.0D, z));
                    double upper =
                            lerp(
                                    xFade,
                                    grad(this.permutations[aa + 1], x, 0.0D, z - 1.0D),
                                    grad(this.permutations[ba + 1], x - 1.0D, 0.0D, z - 1.0D));
                    noiseArray[index++] += lerp(zFade, lower, upper) * amplitude;
                }
            }
        } else {
            int index = 0;
            double amplitude = 1.0D / noiseScale;
            int lastY = -1;
            double z0y0 = 0.0D;
            double z0y1 = 0.0D;
            double z1y0 = 0.0D;
            double z1y1 = 0.0D;

            for (int ix = 0; ix < xSize; ++ix) {
                double x = xOffset + (double) ix * xScale + this.xCoord;
                int xi = (int) x;

                if (x < (double) xi) {
                    --xi;
                }

                int xHash = xi & 255;
                x -= xi;
                double xFade = x * x * x * (x * (x * 6.0D - 15.0D) + 10.0D);

                for (int iz = 0; iz < zSize; ++iz) {
                    double z = zOffset + (double) iz * zScale + this.zCoord;
                    int zi = (int) z;

                    if (z < (double) zi) {
                        --zi;
                    }

                    int zHash = zi & 255;
                    z -= zi;
                    double zFade = z * z * z * (z * (z * 6.0D - 15.0D) + 10.0D);

                    for (int iy = 0; iy < ySize; ++iy) {
                        double y = yOffset + (double) iy * yScale + this.yCoord;
                        int yi = (int) y;

                        if (y < (double) yi) {
                            --yi;
                        }

                        int yHash = yi & 255;
                        y -= yi;
                        double yFade = y * y * y * (y * (y * 6.0D - 15.0D) + 10.0D);

                        if (iy == 0 || yHash != lastY) {
                            lastY = yHash;
                            int a = this.permutations[xHash] + yHash;
                            int aa = this.permutations[a] + zHash;
                            int ab = this.permutations[a + 1] + zHash;
                            int b = this.permutations[xHash + 1] + yHash;
                            int ba = this.permutations[b] + zHash;
                            int bb = this.permutations[b + 1] + zHash;
                            z0y0 =
                                    lerp(
                                            xFade,
                                            grad(this.permutations[aa], x, y, z),
                                            grad(this.permutations[ba], x - 1.0D, y, z));
                            z0y1 =
                                    lerp(
                                            xFade,
                                            grad(this.permutations[ab], x, y - 1.0D, z),
                                            grad(this.permutations[bb], x - 1.0D, y - 1.0D, z));
                            z1y0 =
                                    lerp(
                                            xFade,
                                            grad(this.permutations[aa + 1], x, y, z - 1.0D),
                                            grad(this.permutations[ba + 1], x - 1.0D, y, z - 1.0D));
                            z1y1 =
                                    lerp(
                                            xFade,
                                            grad(this.permutations[ab + 1], x, y - 1.0D, z - 1.0D),
                                            grad(
                                                    this.permutations[bb + 1],
                                                    x - 1.0D,
                                                    y - 1.0D,
                                                    z - 1.0D));
                        }

                        double lower = lerp(yFade, z0y0, z0y1);
                        double upper = lerp(yFade, z1y0, z1y1);
                        noiseArray[index++] += lerp(zFade, lower, upper) * amplitude;
                    }
                }
            }
        }
    }
}
