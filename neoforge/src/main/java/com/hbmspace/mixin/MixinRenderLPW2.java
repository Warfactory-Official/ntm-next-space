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

import com.hbm.blockentity.machine.LPW2BlockEntity;
import com.hbm.client.render.LPW2Renderer;
import com.hbmspace.blockentity.machine.LPW2SpaceBlockEntity;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LPW2Renderer.class)
public abstract class MixinRenderLPW2 {
    @ModifyArg(
            method =
                    "extractRenderState(Lcom/hbm/blockentity/machine/LPW2BlockEntity;Lcom/hbm/client/render/LPW2Renderer$State;FLnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V",
            at = @At(value = "INVOKE", target = "Lcom/hbm/client/render/LPW2Poses;update(FDD)V"),
            index = 1)
    private double hbmspace$engineTime(
            double clock,
            @Local(argsOnly = true) LPW2BlockEntity be,
            @Local(argsOnly = true) float partialTicks) {
        return ((LPW2SpaceBlockEntity) be).renderTime(partialTicks);
    }
}
