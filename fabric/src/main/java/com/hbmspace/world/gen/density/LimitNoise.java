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

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.IntStream;
import net.minecraft.resources.Identifier;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import org.jspecify.annotations.Nullable;

public record LimitNoise(double xzScale, double yScale, @Nullable PerlinNoise noise)
        implements SeededDensityFunction {

    private static final Codec<Double> SCALE = Codec.doubleRange(0.001D, 1000.0D);

    public static final MapCodec<LimitNoise> MAP_CODEC =
            RecordCodecBuilder.mapCodec(
                    i ->
                            i.group(
                                            SCALE.fieldOf("xz_scale")
                                                    .forGetter(LimitNoise::xzScale),
                                            SCALE.fieldOf("y_scale").forGetter(LimitNoise::yScale))
                                    .apply(
                                            i,
                                            (xzScale, yScale) ->
                                                    new LimitNoise(xzScale, yScale, null)));

    private static final KeyDispatchDataCodec<LimitNoise> CODEC =
            KeyDispatchDataCodec.of(MAP_CODEC);
    private static final Identifier TERRAIN =
            Identifier.fromNamespaceAndPath("hbmspace", "limit_noise");
    public static final double BLENDED_SCALE = 684.412D;
    public static final double OCTAVE_SUM = 65535.0D;

    @Override
    public DensityFunction seeded(PositionalRandomFactory random) {
        return new LimitNoise(
                xzScale,
                yScale,
                PerlinNoise.createLegacyForBlendedNoise(
                        random.fromHashOf(TERRAIN), IntStream.rangeClosed(-15, 0)));
    }

    @Override
    public double compute(FunctionContext context) {
        if (noise == null)
            throw new IllegalStateException("limit noise computed before a level wired it");
        double xz = BLENDED_SCALE * xzScale;
        return noise.getValue(
                        context.blockX() * xz,
                        context.blockY() * BLENDED_SCALE * yScale,
                        context.blockZ() * xz)
                * OCTAVE_SUM
                / 512.0D
                / 128.0D;
    }

    @Override
    public void fillArray(double[] output, ContextProvider contextProvider) {
        contextProvider.fillAllDirectly(output, this);
    }

    @Override
    public DensityFunction mapChildren(Visitor visitor) {
        return this;
    }

    @Override
    public double minValue() {
        return -maxValue();
    }

    @Override
    public double maxValue() {
        return 2.0D;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }
}
