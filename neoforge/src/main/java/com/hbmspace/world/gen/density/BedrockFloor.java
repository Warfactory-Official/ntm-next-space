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
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.densityfunction.*;

public record BedrockFloor(Identifier randomName, int trueAtAndBelow, int falseAtAndAbove)
        implements DensityFunction {
    public static final MapCodec<BedrockFloor> MAP_CODEC =
            RecordCodecBuilder.mapCodec(
                    i ->
                            i.group(
                                            Identifier.CODEC
                                                    .fieldOf("random_name")
                                                    .forGetter(BedrockFloor::randomName),
                                            Codec.INT
                                                    .fieldOf("true_at_and_below")
                                                    .forGetter(BedrockFloor::trueAtAndBelow),
                                            Codec.INT
                                                    .fieldOf("false_at_and_above")
                                                    .forGetter(BedrockFloor::falseAtAndAbove))
                                    .apply(i, BedrockFloor::new));
    private static final float OPEN = -1000000.0F;

    @Override
    public DensitySampler compileSampler(CompileContext context) {
        return new Sampler(
                trueAtAndBelow, falseAtAndAbove, context.createRandom(randomName).forkPositional());
    }

    private record Sampler(int below, int above, PositionalRandomFactory random)
            implements DensitySampler {
        @Override
        public float sampleValue(SamplerContext context, int x, int y, int z) {
            if (y <= below) return 1.0F;
            if (y >= above) return OPEN;
            double probability = Mth.map(y, below, above, 1.0D, 0.0D);
            return random.at(x, y, z).nextFloat() < probability ? 1.0F : OPEN;
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
        return Interval.of(OPEN, 1.0F);
    }

    @Override
    public int domainAxes() {
        return ALL_AXES;
    }

    @Override
    public MapCodec<BedrockFloor> codec() {
        return MAP_CODEC;
    }
}
