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
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.jspecify.annotations.Nullable;

public record NoiseCaves(DensityFunction caves) implements DensityFunction {

    public static final MapCodec<NoiseCaves> MAP_CODEC =
            RecordCodecBuilder.mapCodec(
                    i ->
                            i.group(
                                            DensityFunction.CODEC
                                                    .fieldOf("caves")
                                                    .forGetter(NoiseCaves::caves))
                                    .apply(i, NoiseCaves::new));

    private static final KeyDispatchDataCodec<NoiseCaves> CODEC =
            KeyDispatchDataCodec.of(MAP_CODEC);

    public static Aquifer aquifer(DensityFunction caves, Aquifer.FluidPicker fluids) {
        BlockState air = Blocks.AIR.defaultBlockState();
        return new Aquifer() {
            @Override
            public @Nullable BlockState computeSubstance(FunctionContext context, double density) {
                if (density > 0.0D) return null;
                if (caves.compute(context) < 0.0D) return air;
                return fluids.computeFluid(context.blockX(), context.blockY(), context.blockZ())
                        .at(context.blockY());
            }

            @Override
            public boolean shouldScheduleFluidUpdate() {
                return false;
            }
        };
    }

    @Override
    public double compute(FunctionContext context) {
        return caves.compute(context);
    }

    @Override
    public void fillArray(double[] output, ContextProvider contextProvider) {
        caves.fillArray(output, contextProvider);
    }

    @Override
    public DensityFunction mapChildren(Visitor visitor) {
        return new NoiseCaves(visitor.apply(caves));
    }

    @Override
    public double minValue() {
        return caves.minValue();
    }

    @Override
    public double maxValue() {
        return caves.maxValue();
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return CODEC;
    }
}
