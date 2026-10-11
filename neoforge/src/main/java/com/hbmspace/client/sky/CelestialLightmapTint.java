// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.sky;

import com.hbmspace.HbmSpace;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BindGroupLayout;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public final class CelestialLightmapTint {
    public static final BindGroupLayout INFO =
            BindGroupLayout.builder()
                    .withUniform("CelestialTint", UniformType.UNIFORM_BUFFER)
                    .withSampler("LightmapRaw")
                    .build();
    public static final RenderPipeline PIPELINE =
            RenderPipeline.builder(RenderPipelines.GLOBALS_SNIPPET)
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
