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

package com.hbmspace.mixin.client;

import com.hbm.client.render.flywheel.BeamVisual;
import com.hbm.client.render.flywheel.FELVisual;
import com.hbm.render.util.BeamPronter.EnumWaveType;
import com.hbmspace.client.render.BeamAlphaSpace;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FELVisual.class)
public abstract class MixinFELVisual {
    @WrapOperation(
            method = "writeFrame",
            at = @At(value = "INVOKE", target = BeamAlphaSpace.BEAM_UPDATE))
    private void hbmspace$halfAlpha(
            BeamVisual beam,
            Matrix4fc base,
            Vec3 skeleton,
            EnumWaveType wave,
            int phase,
            int count,
            float size,
            float thickness,
            int outer,
            int inner,
            Operation<Void> original) {
        original.call(
                beam,
                base,
                skeleton,
                wave,
                phase,
                count,
                size,
                thickness,
                BeamAlphaSpace.premultiply(outer),
                BeamAlphaSpace.premultiply(inner));
    }
}
