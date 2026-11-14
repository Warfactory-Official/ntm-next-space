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

import com.hbmspace.world.gen.density.SeededDensityFunction;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.SurfaceSystem;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RandomState.class)
public abstract class MixinRandomState {

    @Shadow @Final private PositionalRandomFactory random;

    @Shadow @Final private SurfaceSystem surfaceSystem;

    @Unique private boolean hbmspace$celestial;

    @ModifyArg(
            method = "<init>",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/level/levelgen/NoiseRouter;"
                                            + "mapAll(Lnet/minecraft/world/level/levelgen/DensityFunction$Visitor;)"
                                            + "Lnet/minecraft/world/level/levelgen/NoiseRouter;"))
    private DensityFunction.Visitor hbmspace$seedCelestialFunctions(
            DensityFunction.Visitor wiring) {
        Map<DensityFunction, DensityFunction> seeded = new HashMap<>();
        return new DensityFunction.Visitor() {
            @Override
            public DensityFunction apply(DensityFunction input) {
                if (!(input instanceof SeededDensityFunction function)) return wiring.apply(input);
                hbmspace$celestial = true;
                return wiring.apply(
                        seeded.computeIfAbsent(function, unwired -> function.seeded(random)));
            }

            @Override
            public DensityFunction.NoiseHolder visitNoise(DensityFunction.NoiseHolder noise) {
                return wiring.visitNoise(noise);
            }
        };
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void hbmspace$solidSea(
            NoiseGeneratorSettings settings,
            HolderGetter<NormalNoise.NoiseParameters> noises,
            long seed,
            CallbackInfo ci) {
        BlockState sea = settings.defaultFluid();
        if (hbmspace$celestial
                && !sea.isAir()
                && sea.getFluidState().isEmpty()
                && sea != settings.defaultBlock()) {
            ((com.hbmspace.world.gen.surface.SolidSeaSurface) (surfaceSystem))
                    .hbmspace$readSeaAsFluid(sea);
        }
    }
}
