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
import net.minecraft.world.level.levelgen.material.MaterialRuleContext;
import net.minecraft.world.level.levelgen.material.condition.ConditionEvaluator;
import net.minecraft.world.level.levelgen.material.condition.MaterialCondition;

public record FloorAboveCondition(int anchor, int multiplier, SurfaceDepth depth)
        implements MaterialCondition {

    public static final MapCodec<FloorAboveCondition> CODEC =
            RecordCodecBuilder.mapCodec(
                    i ->
                            i.group(
                                            Codec.INT
                                                    .fieldOf("anchor")
                                                    .forGetter(FloorAboveCondition::anchor),
                                            Codec.intRange(-20, 20)
                                                    .fieldOf("depth_multiplier")
                                                    .forGetter(FloorAboveCondition::multiplier),
                                            SurfaceDepth.MAP_CODEC.forGetter(
                                                    FloorAboveCondition::depth))
                                    .apply(i, FloorAboveCondition::new));

    @Override
    public MapCodec<? extends MaterialCondition> codec() {
        return CODEC;
    }

    @Override
    public ConditionEvaluator compile(MaterialRuleContext context) {
        SurfaceDepth.Column column = depth.column(context);
        return () ->
                context.blockY() + context.stoneDepthAbove() - 1
                        >= anchor + multiplier * column.depth();
    }
}
