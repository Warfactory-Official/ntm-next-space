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

package com.hbmspace.mixin;

import com.hbmspace.world.gen.density.BedrockFloor;
import com.hbmspace.world.gen.density.CelestialBiomes;
import com.hbmspace.world.gen.density.CelestialRelief;
import com.hbmspace.world.gen.density.LimitNoise;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DfRewriteRule;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RandomState.class)
public abstract class MixinRandomState {
    @Inject(
            method =
                    "create(Lnet/minecraft/core/HolderGetter;JLnet/minecraft/world/level/levelgen/NoiseGeneratorSettings;)Lnet/minecraft/world/level/levelgen/RandomState;",
            at = @At("RETURN"))
    private static void hbmspace$solidSea(
            HolderGetter<NormalNoise> noises,
            long seed,
            NoiseGeneratorSettings settings,
            CallbackInfoReturnable<RandomState> ci) {
        BlockState sea = settings.defaultFluid();
        if (!sea.isAir()
                && sea.getFluidState().isEmpty()
                && sea != settings.defaultBlock()
                && hbmspace$celestial(settings.noiseRouter())) {
            ((com.hbmspace.world.gen.surface.SolidSeaSurface) (ci.getReturnValue().surfaceSystem()))
                    .hbmspace$readSeaAsFluid(sea);
        }
    }

    @Unique
    private static boolean hbmspace$celestial(NoiseRouter router) {
        ArrayDeque<DensityFunction> pending = new ArrayDeque<>();
        pending.add(router.temperature());
        pending.add(router.vegetation());
        pending.add(router.continents());
        pending.add(router.erosion());
        pending.add(router.depth());
        pending.add(router.ridges());
        pending.add(router.chunkSurfaceLevel());
        pending.add(router.finalDensity());
        Set<DensityFunction> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        DfRewriteRule children =
                child -> {
                    pending.addLast(child);
                    return child;
                };
        while (!pending.isEmpty()) {
            DensityFunction function = pending.removeFirst();
            if (!visited.add(function)) continue;
            if (function instanceof BedrockFloor
                    || function instanceof CelestialBiomes
                    || function instanceof CelestialRelief
                    || function instanceof LimitNoise) return true;
            function.rewriteChildren(children);
        }
        return false;
    }
}
