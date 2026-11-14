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

import com.hbmspace.client.fog.TerrainExpFog;
import me.cortex.voxy.client.core.NormalRenderPipeline;
import me.cortex.voxy.client.core.rendering.Viewport;
import org.lwjgl.opengl.GL30C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = NormalRenderPipeline.class, remap = false)
public abstract class MixinNormalRenderPipeline {
    @Redirect(
            method = "finish",
            at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL30C;glUniform4f(IFFFF)V"))
    private void hbmspace$density(
            int location,
            float x,
            float y,
            float z,
            float w,
            @Coerce Object viewport,
            int sourceDepth,
            int outputFramebuffer,
            int width,
            int height) {
        if (location == 4) {
            if (w != 0F)
                throw new IllegalStateException("Voxy's fog compositor already uses endParams.w");
            var fog = ((Viewport<?>) viewport).fogParameters;
            int flags =
                    ((com.hbmspace.client.fog.TerrainFogPayload) (java.lang.Object) (fog))
                            .hbmspace$flags();
            if ((flags & TerrainExpFog.EXP) != 0) {
                w =
                        ((com.hbmspace.client.fog.TerrainFogPayload) (java.lang.Object) (fog))
                                .hbmspace$density();
                if ((flags & TerrainExpFog.DARKNESS) != 0) w = -w;
            }
        }
        GL30C.glUniform4f(location, x, y, z, w);
    }

    @ModifyVariable(method = "finish", at = @At("STORE"), name = "fogCoversAllRendering")
    private boolean hbmspace$keepAtmosphericLods(
            boolean covered,
            @Coerce Object viewport,
            int sourceDepth,
            int outputFramebuffer,
            int width,
            int height) {
        int flags =
                ((com.hbmspace.client.fog.TerrainFogPayload)
                                (java.lang.Object) (((Viewport<?>) viewport).fogParameters))
                        .hbmspace$flags();
        return covered
                && ((flags & TerrainExpFog.EXP) == 0 || (flags & TerrainExpFog.DARKNESS) != 0);
    }
}
