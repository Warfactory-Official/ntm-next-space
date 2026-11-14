/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024 George Paton. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only,
 * as published by the Free Software Foundation.
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this code. If not, see <https://www.gnu.org/licenses/>.
 *
 * Under GPLv3 §7(c) and §7(e), these additional terms apply to LGPL-covered
 * material authored by movblock:
 *
 * - the license grants no rights to the name "NTM NEXT: Space";
 * - a modified version must not be named NTM NEXT: Space or presented as
 *   the official release, and must be marked as different from it.
 *
 * Accurately identifying a work as derived from NTM NEXT: Space does not
 * violate these additional terms, provided it is clearly distinguished from
 * the official release and does not imply endorsement. Ports are encouraged
 * to acknowledge NTM NEXT: Space as their upstream project and provide a
 * link to it.
 */

package com.hbmspace.world.gen.density;

import com.hbmspace.dim.BiomeGenBaseCelestial;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import java.util.stream.IntStream;
import net.minecraft.resources.Identifier;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import org.jspecify.annotations.Nullable;

public record CelestialRelief(
        DensityFunction biome, Output output, @Nullable PerlinNoise depthNoise)
        implements SeededDensityFunction {

    public static final MapCodec<CelestialRelief> MAP_CODEC =
            RecordCodecBuilder.mapCodec(
                    i ->
                            i.group(
                                            DensityFunction.CODEC
                                                    .fieldOf("biome")
                                                    .forGetter(CelestialRelief::biome),
                                            Output.CODEC
                                                    .fieldOf("output")
                                                    .forGetter(CelestialRelief::output))
                                    .apply(
                                            i,
                                            (biome, output) ->
                                                    new CelestialRelief(biome, output, null)));

    private static final KeyDispatchDataCodec<CelestialRelief> CODEC =
            KeyDispatchDataCodec.of(MAP_CODEC);
    private static final Identifier DEPTH =
            Identifier.fromNamespaceAndPath("hbmspace", "relief_depth");
    private static final float[] PARABOLIC_FIELD = new float[25];

    static {
        for (int j = -2; j <= 2; ++j) {
            for (int k = -2; k <= 2; ++k) {
                PARABOLIC_FIELD[j + 2 + (k + 2) * 5] =
                        10.0F / (float) Math.sqrt((float) (j * j + k * k) + 0.2F);
            }
        }
    }

    public static float[] parabolicField() {
        return PARABOLIC_FIELD.clone();
    }

    public enum Output implements StringRepresentable {
        OFFSET,
        FACTOR,
        CAVE_CEILING,
        CAVE_GUARD;

        public static final Codec<Output> CODEC = StringRepresentable.fromEnum(Output::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    @Override
    public DensityFunction seeded(PositionalRandomFactory random) {
        BiomeGenBaseCelestial.buildDeclared();
        return new CelestialRelief(
                biome,
                output,
                PerlinNoise.createLegacyForBlendedNoise(
                        random.fromHashOf(DEPTH), IntStream.rangeClosed(-15, 0)));
    }

    @Override
    public double compute(FunctionContext context) {
        if (depthNoise == null)
            throw new IllegalStateException("relief computed before a level wired it");
        int quartX = context.blockX() >> 2;
        int quartZ = context.blockZ() >> 2;

        BiomeGenBaseCelestial centre = biomeAt(quartX, quartZ);
        float scale = 0.0F;
        float depth = 0.0F;
        float weight = 0.0F;
        float lowest = 0.0F;
        for (int j = -2; j <= 2; ++j) {
            for (int k = -2; k <= 2; ++k) {
                BiomeGenBaseCelestial near = biomeAt(quartX + j, quartZ + k);
                float w = PARABOLIC_FIELD[j + 2 + (k + 2) * 5] / (near.rootHeight + 2.0F);
                if (near.rootHeight > centre.rootHeight) w /= 2.0F;
                scale += near.heightVariation * w;
                depth += near.rootHeight * w;
                weight += w;
                lowest = Math.min(lowest, near.rootHeight);
            }
        }

        return switch (output) {
            case OFFSET -> {
                double scaledDepth =
                        ((depth / weight * 4.0F - 1.0F) / 8.0F + depthNoise(quartX, quartZ) * 0.2D)
                                * 8.5D
                                / 8.0D;
                yield (68.0D + scaledDepth * 32.0D) / 128.0D;
            }
            case FACTOR -> 0.75D / (scale / weight * 0.9F + 0.1F);
            case CAVE_CEILING -> 8 * (int) ((56.0D + lowest * 20.0D) / 8.0D);
            case CAVE_GUARD -> (20.0D - lowest * 30.0D) / 128.0D;
        };
    }

    private BiomeGenBaseCelestial biomeAt(int quartX, int quartZ) {
        return BiomeGenBaseCelestial.getBiome(
                (int) biome.compute(new SinglePointContext(quartX << 2, 0, quartZ << 2)));
    }

    private double depthNoise(int quartX, int quartZ) {
        double noise =
                depthNoise.getValue(quartX * 200.0D, 10.0D, quartZ * 200.0D) * 65535.0D / 8000.0D;
        if (noise < 0.0D) noise = -noise * 0.3D;
        noise = noise * 3.0D - 2.0D;
        return noise < 0.0D ? Math.max(noise / 2.0D, -1.0D) / 2.8D : Math.min(noise, 1.0D) / 8.0D;
    }

    @Override
    public void fillArray(double[] output, ContextProvider contextProvider) {
        contextProvider.fillAllDirectly(output, this);
    }

    @Override
    public DensityFunction mapChildren(Visitor visitor) {
        return new CelestialRelief(visitor.apply(biome), output, depthNoise);
    }

    @Override
    public double minValue() {
        return -1000000.0D;
    }

    @Override
    public double maxValue() {
        return 1000000.0D;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }
}
