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

package com.hbmspace.client.gui;

import com.hbmspace.HbmSpace;
import com.hbmspace.client.sky.SkyDraws;
import com.hbmspace.client.sky.SkyPipelines;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.resources.Identifier;
import org.joml.Quaternionfc;

public final class StardarMapRenderer extends PictureInPictureRenderer<StardarMapRenderState> {
    public static final RenderPipeline ATMOSPHERE_EMISSIVE_MASKED = masked("atmosphere_emissive");
    public static final RenderPipeline NIGHT_LIGHTS_MASKED = masked("nightlights");

    private final SkyDraws draws = new SkyDraws();

    public static void forEachPipeline(Consumer<RenderPipeline> registrar) {
        List.of(ATMOSPHERE_EMISSIVE_MASKED, NIGHT_LIGHTS_MASKED).forEach(registrar);
    }

    private static RenderPipeline masked(String shader) {
        return RenderPipeline.builder(RenderPipelines.GLOBALS_SNIPPET)
                .withBindGroupLayout(BindGroupLayouts.PROJECTION)
                .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
                .withBindGroupLayout(SkyPipelines.PLANET_INFO)
                .withLocation(id("pipeline/stardar_" + shader))
                .withVertexShader(id("core/planet"))
                .withFragmentShader(id("core/" + shader))
                .withShaderDefine("BODY_MASK")
                .withColorTargetState(new ColorTargetState(BlendFunction.OVERLAY))
                .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX)
                .withPrimitiveTopology(PrimitiveTopology.QUADS)
                .withBindGroupLayout(BindGroupLayouts.SAMPLER0_SAMPLER1_SAMPLER2)
                .build();
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, path);
    }

    @Override
    public Class<StardarMapRenderState> getRenderStateClass() {
        return StardarMapRenderState.class;
    }

    @Override
    protected String getTextureLabel() {
        return "Stardar map";
    }

    @Override
    protected float getTranslateY(int height, int guiScale) {
        return 0;
    }

    @Override
    protected void renderToTexture(
            StardarMapRenderState state, PoseStack pose, SubmitNodeCollector collector) {
        pose.translate(-(state.x1() - state.x0()) / 2F, 0F, 0F);
        for (StardarMapRenderState.Draw draw : state.draws()) {
            switch (draw) {
                case StardarMapRenderState.Textured quad -> textured(quad, pose);
                case StardarMapRenderState.Colored quads -> colored(quads, pose);
            }
        }
        draws.flush(textureView, depthTextureView);
        draws.endFrame();
    }

    private void textured(StardarMapRenderState.Textured quad, PoseStack pose) {
        pose.pushPose();
        if (quad.angle() != 0) {
            pose.translate(quad.pivotX(), quad.pivotY(), 0F);
            pose.rotate((Quaternionfc) Axis.ZP.rotationDegrees(quad.angle()));
            pose.translate(-quad.pivotX(), -quad.pivotY(), 0F);
        }
        VertexConsumer buffer =
                draws.draw(
                        quad.pipeline(),
                        pose,
                        quad.r(),
                        quad.g(),
                        quad.b(),
                        quad.a(),
                        quad.texture(),
                        quad.texture1(),
                        quad.texture2(),
                        quad.planet(),
                        0);
        buffer.addVertex(quad.x0(), quad.y1(), 0F).setUv(quad.u0(), quad.v1());
        buffer.addVertex(quad.x1(), quad.y1(), 0F).setUv(quad.u1(), quad.v1());
        buffer.addVertex(quad.x1(), quad.y0(), 0F).setUv(quad.u1(), quad.v0());
        buffer.addVertex(quad.x0(), quad.y0(), 0F).setUv(quad.u0(), quad.v0());
        pose.popPose();
    }

    private void colored(StardarMapRenderState.Colored quads, PoseStack pose) {
        VertexConsumer buffer =
                draws.draw(SkyPipelines.COLORED_TRANSLUCENT_NO_CULL, pose, 1, 1, 1, 1, null);
        float[] v = quads.vertices();
        for (int i = 0; i < v.length; i += 6) {
            buffer.addVertex(v[i], v[i + 1], 0F).setColor(v[i + 2], v[i + 3], v[i + 4], v[i + 5]);
        }
    }

    @Override
    public void close() {
        super.close();
        draws.close();
    }
}
