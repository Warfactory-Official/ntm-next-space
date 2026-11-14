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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.SurfaceRules.RuleSource;
import net.minecraft.world.level.levelgen.SurfaceRules;

public record SubstituteRule(RuleSource rule, List<Replacement> replacements)
        implements RuleSource {

    public record Replacement(BlockState from, BlockState to) {

        static final Codec<Replacement> CODEC =
                RecordCodecBuilder.create(
                        i ->
                                i.group(
                                                BlockState.CODEC
                                                        .fieldOf("from")
                                                        .forGetter(Replacement::from),
                                                BlockState.CODEC
                                                        .fieldOf("to")
                                                        .forGetter(Replacement::to))
                                        .apply(i, Replacement::new));
    }

    public static final MapCodec<SubstituteRule> CODEC =
            RecordCodecBuilder.mapCodec(
                    i ->
                            i.group(
                                            RuleSource.CODEC
                                                    .fieldOf("rule")
                                                    .forGetter(SubstituteRule::rule),
                                            Replacement.CODEC
                                                    .listOf()
                                                    .fieldOf("replacements")
                                                    .forGetter(SubstituteRule::replacements))
                                    .apply(i, SubstituteRule::new));

    @Override
    public MapCodec<? extends RuleSource> codec() {
        return CODEC;
    }

    @Override
    public SurfaceRules.SurfaceRule apply(SurfaceRules.Context context) {
        SurfaceRules.SurfaceRule inner = rule.apply(context);
        Map<BlockState, BlockState> map = new HashMap<>();
        for (Replacement replacement : replacements) map.put(replacement.from(), replacement.to());
        return (x, y, z) -> {
            BlockState state = inner.tryApply(x, y, z);
            return state == null ? null : map.getOrDefault(state, state);
        };
    }
}
