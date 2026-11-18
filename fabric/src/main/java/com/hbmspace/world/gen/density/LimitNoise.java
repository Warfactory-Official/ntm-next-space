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
import net.minecraft.resources.Identifier;
import net.minecraft.util.Interval;
import net.minecraft.world.level.levelgen.densityfunction.*;

public record LimitNoise(double xzScale, double yScale) implements DensityFunction {
    private static final Codec<Double> SCALE = Codec.doubleRange(0.001D, 1000.0D);
    public static final MapCodec<LimitNoise> MAP_CODEC =
            RecordCodecBuilder.mapCodec(
                    i ->
                            i.group(
                                            SCALE.fieldOf("xz_scale")
                                                    .forGetter(LimitNoise::xzScale),
                                            SCALE.fieldOf("y_scale").forGetter(LimitNoise::yScale))
                                    .apply(i, LimitNoise::new));
    private static final Identifier TERRAIN =
            Identifier.fromNamespaceAndPath("hbmspace", "limit_noise");
    public static final double BLENDED_SCALE = 684.412D;
    public static final double OCTAVE_SUM = 65535.0D;

    @Override
    public DensitySampler compileSampler(CompileContext context) {
        return new Sampler(
                BLENDED_SCALE * xzScale,
                BLENDED_SCALE * yScale,
                new TerrainNoise(context.createRandom(TERRAIN)));
    }

    private record Sampler(double xz, double yScale, TerrainNoise noise) implements DensitySampler {
        @Override
        public float sampleValue(SamplerContext context, int x, int y, int z) {
            return (float)
                    (noise.getValue(x * xz, y * yScale, z * xz) * OCTAVE_SUM / 512.0D / 128.0D);
        }

        @Override
        public void sampleVolume(
                SamplerContext context, DensityBuffer output, DensityVolume volume) {
            DensitySampler.sampleVolumeNaive(context, output, volume, this);
        }
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        return this;
    }

    @Override
    public Interval range() {
        return Interval.ofSymmetric(2.0F);
    }

    @Override
    public int domainAxes() {
        return ALL_AXES;
    }

    @Override
    public MapCodec<LimitNoise> codec() {
        return MAP_CODEC;
    }
}
