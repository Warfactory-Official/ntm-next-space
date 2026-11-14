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

import com.hbm.client.render.FELRenderer;
import com.hbm.render.util.BeamPronter.EnumBeamType;
import com.hbm.render.util.BeamPronter.EnumWaveType;
import com.hbmspace.client.render.BeamAlphaSpace;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FELRenderer.class)
public abstract class MixinRenderFEL {
    @WrapOperation(method = "submit", at = @At(value = "INVOKE", target = BeamAlphaSpace.PRONT))
    private void hbmspace$halfAlpha(
            PoseStack ps,
            SubmitNodeCollector collector,
            Vec3 skeleton,
            EnumWaveType wave,
            EnumBeamType beam,
            int outer,
            int inner,
            int start,
            int segments,
            float size,
            int layers,
            float thickness,
            Operation<Void> original) {
        BeamAlphaSpace.pront(
                ps, collector, skeleton, wave, beam, outer, inner, start, segments, size, layers,
                thickness);
    }
}
