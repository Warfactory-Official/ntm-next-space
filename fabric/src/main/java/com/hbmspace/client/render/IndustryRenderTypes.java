// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.render;

import com.hbm.client.render.ParticleRenderTypes;
import com.hbm.client.render.WorldRenderPipeline;
import com.hbmspace.HbmSpace;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

public final class IndustryRenderTypes {

    public static final RenderPipeline LAMP_PIPELINE =
            WorldRenderPipeline.of(
                    RenderPipeline.builder(RenderPipelines.MATRICES_FOG_LIGHT_DIR_SNIPPET)
                            .withLocation(
                                    Identifier.fromNamespaceAndPath(
                                            HbmSpace.MOD_ID, "pipeline/hydroponic_lamp"))
                            .withVertexShader("core/entity")
                            .withFragmentShader(ParticleRenderTypes.ENTITY_FADE)
                            .withShaderDefine("ALPHA_CUTOUT", 0.1F)
                            .withShaderDefine("EMISSIVE")
                            .withShaderDefine("NO_OVERLAY")
                            .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
                            .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
                            .withVertexBinding(0, DefaultVertexFormat.ENTITY)
                            .withPrimitiveTopology(PrimitiveTopology.QUADS)
                            .withDepthStencilState(
                                    new DepthStencilState(
                                            DepthStencilState.DEFAULT.depthTest(), false))
                            .withCull(false));

    private static final Function<Identifier, RenderType> LAMP =
            Util.memoize(
                    texture ->
                            RenderType.create(
                                    "hbmspace_hydroponic_lamp",
                                    RenderSetup.builder(LAMP_PIPELINE)
                                            .withTexture("Sampler0", texture)
                                            .createRenderSetup()));

    private IndustryRenderTypes() {}

    public static RenderType lamp(Identifier texture) {
        return LAMP.apply(texture);
    }

    public static void forEach(Consumer<RenderPipeline> registrar) {
        registrar.accept(LAMP_PIPELINE);
    }
}
