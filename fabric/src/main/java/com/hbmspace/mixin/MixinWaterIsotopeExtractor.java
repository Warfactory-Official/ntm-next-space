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

import com.hbm.blockentity.machine.DeuteriumExtractorBlockEntity;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(DeuteriumExtractorBlockEntity.class)
public abstract class MixinWaterIsotopeExtractor {
    @Shadow public long energy;

    @ModifyConstant(
            method = {"tickServer", "energyReady"},
            constant = @Constant(longValue = 20L))
    private long hbmspace$hundredth(long divisor) {
        return 100L;
    }

    @ModifyExpressionValue(
            method = "tickServer",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/hbm/blockentity/machine/DeuteriumExtractorBlockEntity;energyReady()Z"))
    private boolean hbmspace$over200(boolean ready) {
        return ready && energy > 200;
    }
}
