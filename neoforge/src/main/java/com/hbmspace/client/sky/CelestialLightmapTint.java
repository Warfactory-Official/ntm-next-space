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

package com.hbmspace.client.sky;

import com.hbmspace.HbmSpace;
import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.UniformType;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public final class CelestialLightmapTint {
    public static final BindGroupLayout INFO =
            BindGroupLayout.builder()
                    .withUniform("CelestialTint", UniformType.UNIFORM_BUFFER)
                    .withUniform("LightmapRaw", UniformType.COMBINED_IMAGE_SAMPLER)
                    .build();
    public static final RenderPipeline PIPELINE =
            RenderPipeline.builder(RenderPipelines.GLOBALS_SNIPPET)
                    .withColorTargetState(ColorTargetState.DEFAULT)
                    .withLocation(
                            Identifier.fromNamespaceAndPath(
                                    HbmSpace.MOD_ID, "pipeline/lightmap_tint"))
                    .withVertexShader("core/screenquad")
                    .withFragmentShader(
                            Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "core/lightmap_tint"))
                    .withBindGroupLayout(INFO)
                    .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
                    .build();
    public static final int INFO_SIZE = 80;

    private CelestialLightmapTint() {}

    public static float bonus(float skyBrightness, float sunBrightness, boolean ike) {
        float sun = ike ? sunBrightness - .1F : sunBrightness;
        return Math.max(skyBrightness - sun, 0F) * (ike ? 20F : 60F);
    }
}
