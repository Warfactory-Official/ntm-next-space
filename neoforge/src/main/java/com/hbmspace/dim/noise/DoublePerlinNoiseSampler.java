// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024 George Paton
// SPDX-FileCopyrightText: 2024 JamesH_2
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.dim.noise;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

public final class DoublePerlinNoiseSampler {

    private final double amplitude;
    private final OctavePerlinNoiseSampler firstSampler;
    private final OctavePerlinNoiseSampler secondSampler;

    public static DoublePerlinNoiseSampler create(Random random, int offset, double... octaves) {
        List<Double> list = new ArrayList<>();
        for (double octave : octaves) {
            list.add(octave);
        }

        return new DoublePerlinNoiseSampler(random, offset, list);
    }

    private DoublePerlinNoiseSampler(Random random, int offset, List<Double> octaves) {
        this.firstSampler = OctavePerlinNoiseSampler.create(random, offset, octaves);
        this.secondSampler = OctavePerlinNoiseSampler.create(random, offset, octaves);
        int i = Integer.MAX_VALUE;
        int j = Integer.MIN_VALUE;
        ListIterator<Double> iterator = octaves.listIterator();

        while (iterator.hasNext()) {
            int k = iterator.nextIndex();
            double d = iterator.next();
            if (d != 0.0D) {
                i = Math.min(i, k);
                j = Math.max(j, k);
            }
        }

        this.amplitude = 0.16666666666666666D / createAmplitude(j - i);
    }

    private static double createAmplitude(int octaves) {
        return 0.1D * (1.0D + 1.0D / (double) (octaves + 1));
    }

    public double sample(double x, double y, double z) {
        double d = x * 1.0181268882175227D;
        double e = y * 1.0181268882175227D;
        double f = z * 1.0181268882175227D;
        return (this.firstSampler.sample(x, y, z) + this.secondSampler.sample(d, e, f))
                * this.amplitude;
    }
}
