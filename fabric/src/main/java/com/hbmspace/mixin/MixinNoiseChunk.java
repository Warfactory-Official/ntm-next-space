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

import com.hbmspace.world.gen.density.NoiseCaves;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.densityfunction.DensitySamplerSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(NoiseChunk.class)
public abstract class MixinNoiseChunk {
    @Shadow @Final private DensitySamplerSet cachingSamplers;

    @WrapOperation(
            method =
                    "<init>(Lnet/minecraft/world/level/levelgen/RandomState;"
                            + "Lnet/minecraft/world/level/levelgen/Beardifier;Lnet/minecraft/world/level/levelgen/NoiseGeneratorSettings;"
                            + "Lnet/minecraft/world/level/levelgen/Aquifer$FluidPicker;"
                            + "Lnet/minecraft/world/level/levelgen/blending/Blender;"
                            + "Lnet/minecraft/world/level/levelgen/densityfunction/DensityVolume;)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/level/levelgen/Aquifer;"
                                            + "createDisabled(Lnet/minecraft/world/level/levelgen/Aquifer$FluidPicker;)"
                                            + "Lnet/minecraft/world/level/levelgen/Aquifer;"),
            require = 1,
            expect = 1,
            allow = 1)
    private Aquifer hbmspace$openTerrain(
            Aquifer.FluidPicker fluids,
            Operation<Aquifer> disabled,
            @Local(argsOnly = true) NoiseGeneratorSettings settings) {
        return settings.noiseRouter().finalDensity() instanceof NoiseCaves marker
                ? NoiseCaves.aquifer(cachingSamplers.get(marker.caves()), fluids)
                : disabled.call(fluids);
    }
}
