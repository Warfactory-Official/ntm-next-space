// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024 George Paton
// SPDX-FileCopyrightText: 2024 JamesH_2
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.noise;

import java.util.List;
import java.util.Random;
import net.minecraft.util.Mth;

public final class OctavePerlinNoiseSampler {

    private final PerlinNoiseSampler[] octaveSamplers;
    private final List<Double> amplitudes;
    private final double persistence;
    private final double lacunarity;

    public static OctavePerlinNoiseSampler create(
            Random random, int offset, List<Double> amplitudes) {
        return new OctavePerlinNoiseSampler(random, offset, amplitudes);
    }

    private OctavePerlinNoiseSampler(Random random, int offset, List<Double> amplitudes) {
        this.amplitudes = amplitudes;
        PerlinNoiseSampler perlinNoiseSampler = new PerlinNoiseSampler(random);
        int j = this.amplitudes.size();
        int k = -offset;
        this.octaveSamplers = new PerlinNoiseSampler[j];
        if (k >= 0 && k < j) {
            double d = this.amplitudes.get(k);
            if (d != 0.0D) {
                this.octaveSamplers[k] = perlinNoiseSampler;
            }
        }

        for (int l = k - 1; l >= 0; --l) {
            if (l < j && this.amplitudes.get(l) != 0.0D) {
                this.octaveSamplers[l] = new PerlinNoiseSampler(random);
            }
        }

        if (k < j - 1) {
            throw new IllegalArgumentException("Positive octaves are temporarily disabled");
        } else {
            this.lacunarity = Math.pow(2.0D, (double) (-k));
            this.persistence =
                    Math.pow(2.0D, (double) (j - 1)) / (Math.pow(2.0D, (double) j) - 1.0D);
        }
    }

    public double sample(double x, double y, double z) {
        double d = 0.0D;
        double e = this.lacunarity;
        double f = this.persistence;

        for (int i = 0; i < this.octaveSamplers.length; ++i) {
            PerlinNoiseSampler perlinNoiseSampler = this.octaveSamplers[i];
            if (perlinNoiseSampler != null) {
                double g =
                        perlinNoiseSampler.sample(
                                maintainPrecision(x * e),
                                maintainPrecision(y * e),
                                maintainPrecision(z * e),
                                0.0D,
                                0.0D);
                d += this.amplitudes.get(i) * g * f;
            }

            e *= 2.0D;
            f /= 2.0D;
        }

        return d;
    }

    public static double maintainPrecision(double value) {
        return value - (double) Mth.lfloor(value / 3.3554432E7D + 0.5D) * 3.3554432E7D;
    }
}
