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

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Interval;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.densityfunction.*;
import org.jspecify.annotations.Nullable;

public record NoiseCaves(DensityFunction terrain, DensityFunction caves)
        implements DensityFunction {
    public static final MapCodec<NoiseCaves> MAP_CODEC =
            RecordCodecBuilder.mapCodec(
                    i ->
                            i.group(
                                            DensityFunction.CODEC
                                                    .fieldOf("terrain")
                                                    .forGetter(NoiseCaves::terrain),
                                            DensityFunction.CODEC
                                                    .fieldOf("caves")
                                                    .forGetter(NoiseCaves::caves))
                                    .apply(i, NoiseCaves::new));

    public static Aquifer aquifer(DensitySampler.Bound caves, Aquifer.FluidPicker fluids) {
        BlockState air = Blocks.AIR.defaultBlockState();
        return new Aquifer() {
            @Override
            public @Nullable BlockState computeSubstance(int x, int y, int z, double density) {
                if (density > 0.0D) return null;
                if (caves.sampleValue(x, y, z) < 0.0F) return air;
                return fluids.computeFluid(x, y, z).at(y);
            }

            @Override
            public boolean shouldScheduleFluidUpdate() {
                return false;
            }
        };
    }

    @Override
    public DensitySampler compileSampler(CompileContext context) {
        return terrain.compileSampler(context);
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        return new NoiseCaves(rule.rewrite(terrain), rule.rewrite(caves));
    }

    @Override
    public Interval range() {
        return terrain.range();
    }

    @Override
    public int domainAxes() {
        return terrain.domainAxes();
    }

    @Override
    public MapCodec<NoiseCaves> codec() {
        return MAP_CODEC;
    }
}
