/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file under the Apache License except in
 * compliance with that License. You may obtain a copy at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Apache License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License for the specific language governing permissions
 * and limitations under the License.
 */

package com.hbmspace.world.gen.density;

import net.minecraft.util.RandomSource;

public final class TerrainNoise {
    private static final int[] GRADIENT = {
        1, 1, 0, -1, 1, 0, 1, -1, 0, -1, -1, 0,
        1, 0, 1, -1, 0, 1, 1, 0, -1, -1, 0, -1,
        0, 1, 1, 0, -1, 1, 0, 1, -1, 0, -1, -1,
        1, 1, 0, 0, -1, 1, -1, 1, 0, 0, -1, -1
    };
    private final byte[] permutations;
    private final double[] offsets;

    TerrainNoise(RandomSource random) {
        this(random, 16);
    }

    private TerrainNoise(RandomSource random, int octaves) {
        permutations = new byte[octaves * 256];
        offsets = new double[octaves * 3];
        for (int octave = octaves - 1; octave >= 0; --octave) {
            int base = octave * 256;
            int offset = octave * 3;
            offsets[offset] = random.nextDouble() * 256.0D;
            offsets[offset + 1] = random.nextDouble() * 256.0D;
            offsets[offset + 2] = random.nextDouble() * 256.0D;
            for (int i = 0; i < 256; ++i) permutations[base + i] = (byte) i;
            for (int i = 0; i < 256; ++i) {
                int j = i + random.nextInt(256 - i);
                byte value = permutations[base + i];
                permutations[base + i] = permutations[base + j];
                permutations[base + j] = value;
            }
        }
    }

    public static TerrainNoise single(RandomSource random) {
        return new TerrainNoise(random, 1);
    }

    public double noise(double x, double y, double z) {
        return sample(0, x, y, z);
    }

    double getValue(double x, double y, double z) {
        double value = 0.0D;
        double frequency = 1.0D / 32768.0D;
        double amplitude = 32768.0D / 65535.0D;
        for (int octave = 0; octave < 16; ++octave) {
            value +=
                    sample(octave, wrap(x * frequency), wrap(y * frequency), wrap(z * frequency))
                            * amplitude;
            frequency *= 2.0D;
            amplitude /= 2.0D;
        }
        return value;
    }

    private double sample(int octave, double x, double y, double z) {
        int base = octave * 256;
        int offset = octave * 3;
        x += offsets[offset];
        y += offsets[offset + 1];
        z += offsets[offset + 2];
        int floorX = floor(x);
        int floorY = floor(y);
        int floorZ = floor(z);
        double xr = x - floorX;
        double yr = y - floorY;
        double zr = z - floorZ;
        int x0 = permute(base, floorX);
        int x1 = permute(base, floorX + 1);
        int xy00 = permute(base, x0 + floorY);
        int xy01 = permute(base, x0 + floorY + 1);
        int xy10 = permute(base, x1 + floorY);
        int xy11 = permute(base, x1 + floorY + 1);
        double d000 = gradient(permute(base, xy00 + floorZ), xr, yr, zr);
        double d100 = gradient(permute(base, xy10 + floorZ), xr - 1.0D, yr, zr);
        double d010 = gradient(permute(base, xy01 + floorZ), xr, yr - 1.0D, zr);
        double d110 = gradient(permute(base, xy11 + floorZ), xr - 1.0D, yr - 1.0D, zr);
        double d001 = gradient(permute(base, xy00 + floorZ + 1), xr, yr, zr - 1.0D);
        double d101 = gradient(permute(base, xy10 + floorZ + 1), xr - 1.0D, yr, zr - 1.0D);
        double d011 = gradient(permute(base, xy01 + floorZ + 1), xr, yr - 1.0D, zr - 1.0D);
        double d111 = gradient(permute(base, xy11 + floorZ + 1), xr - 1.0D, yr - 1.0D, zr - 1.0D);
        double sx = smoothstep(xr);
        double sy = smoothstep(yr);
        double sz = smoothstep(zr);
        return lerp(
                sz,
                lerp(sy, lerp(sx, d000, d100), lerp(sx, d010, d110)),
                lerp(sy, lerp(sx, d001, d101), lerp(sx, d011, d111)));
    }

    private int permute(int base, int value) {
        return permutations[base + (value & 255)] & 255;
    }

    private static double gradient(int hash, double x, double y, double z) {
        int index = (hash & 15) * 3;
        return GRADIENT[index] * x + GRADIENT[index + 1] * y + GRADIENT[index + 2] * z;
    }

    private static double smoothstep(double value) {
        return value * value * value * (value * (value * 6.0D - 15.0D) + 10.0D);
    }

    private static double lerp(double alpha, double a, double b) {
        return a + alpha * (b - a);
    }

    private static int floor(double value) {
        int integer = (int) value;
        return value < integer ? integer - 1 : integer;
    }

    private static double wrap(double value) {
        double scaled = value / 33554432.0D + 0.5D;
        long integer = (long) scaled;
        return value - (scaled < integer ? integer - 1L : integer) * 33554432.0D;
    }
}
