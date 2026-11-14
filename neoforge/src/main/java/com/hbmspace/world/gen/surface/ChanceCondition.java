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

package com.hbmspace.world.gen.surface;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.SurfaceRules.ConditionSource;
import net.minecraft.world.level.levelgen.SurfaceRules;

public record ChanceCondition(Identifier randomName, float probability) implements ConditionSource {

    public static final MapCodec<ChanceCondition> CODEC =
            RecordCodecBuilder.mapCodec(
                    i ->
                            i.group(
                                            Identifier.CODEC
                                                    .fieldOf("random_name")
                                                    .forGetter(ChanceCondition::randomName),
                                            Codec.floatRange(0.0F, 1.0F)
                                                    .fieldOf("probability")
                                                    .forGetter(ChanceCondition::probability))
                                    .apply(i, ChanceCondition::new));

    @Override
    public MapCodec<? extends ConditionSource> codec() {
        return CODEC;
    }

    @Override
    public SurfaceRules.Condition apply(SurfaceRules.Context context) {
        PositionalRandomFactory random = context.randomState.getOrCreateRandomFactory(randomName);
        return () ->
                random.at(context.blockX, context.blockY, context.blockZ).nextFloat() < probability;
    }
}
