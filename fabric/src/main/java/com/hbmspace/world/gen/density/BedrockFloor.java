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
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import org.jspecify.annotations.Nullable;

public record BedrockFloor(
        Identifier randomName,
        int trueAtAndBelow,
        int falseAtAndAbove,
        @Nullable PositionalRandomFactory random)
        implements SeededDensityFunction {

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
                                    .apply(
                                            i,
                                            (name, below, above) ->
                                                    new BedrockFloor(name, below, above, null)));

    private static final KeyDispatchDataCodec<BedrockFloor> CODEC =
            KeyDispatchDataCodec.of(MAP_CODEC);
    private static final double OPEN = -1000000.0D;

    @Override
    public DensityFunction seeded(PositionalRandomFactory random) {
        return new BedrockFloor(
                randomName,
                trueAtAndBelow,
                falseAtAndAbove,
                random.fromHashOf(randomName).forkPositional());
    }

    @Override
    public double compute(FunctionContext context) {
        if (random == null)
            throw new IllegalStateException("bedrock floor computed before a level wired it");
        int y = context.blockY();
        if (y <= trueAtAndBelow) return 1.0D;
        if (y >= falseAtAndAbove) return OPEN;
        double probability = Mth.map(y, trueAtAndBelow, falseAtAndAbove, 1.0D, 0.0D);
        return random.at(context.blockX(), y, context.blockZ()).nextFloat() < probability
                ? 1.0D
                : OPEN;
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
        return OPEN;
    }

    @Override
    public double maxValue() {
        return 1.0D;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }
}
