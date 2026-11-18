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
import java.util.function.DoubleSupplier;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.material.MaterialRuleContext;

public record SurfaceDepth(double divisor, double jitter) {

    public static final MapCodec<SurfaceDepth> MAP_CODEC =
            RecordCodecBuilder.mapCodec(
                    i ->
                            i.group(
                                            Codec.doubleRange(1.0D, 64.0D)
                                                    .fieldOf("divisor")
                                                    .forGetter(SurfaceDepth::divisor),
                                            Codec.doubleRange(0.0D, 1.0D)
                                                    .fieldOf("jitter")
                                                    .forGetter(SurfaceDepth::jitter))
                                    .apply(i, SurfaceDepth::new));

    private static final Identifier JITTER =
            Identifier.fromNamespaceAndPath("hbmspace", "surface_depth");

    Column column(MaterialRuleContext context) {
        return new Column(
                context,
                context.getNoiseSampler(Noises.SURFACE, false),
                context.getOrCreateRandomFactory(JITTER));
    }

    final class Column {
        private final MaterialRuleContext context;
        private final DoubleSupplier noise;
        private final PositionalRandomFactory random;
        private int x = Integer.MIN_VALUE;
        private int z = Integer.MIN_VALUE;
        private int depth;

        private Column(
                MaterialRuleContext context, DoubleSupplier noise, PositionalRandomFactory random) {
            this.context = context;
            this.noise = noise;
            this.random = random;
        }

        int depth() {
            if (context.blockX() != x || context.blockZ() != z) {
                x = context.blockX();
                z = context.blockZ();
                depth =
                        (int)
                                (noise.getAsDouble() * 8.25D / divisor
                                        + divisor
                                        + random.at(x, 0, z).nextDouble() * jitter);
            }
            return depth;
        }
    }
}
