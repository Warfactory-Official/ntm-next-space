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

package com.hbmspace.client.render.mob;

import com.hbm.client.render.WorldRenderPipeline;
import com.hbm.render.util.Vertices;
import com.hbm.util.RegistryUtil;
import com.hbmspace.HbmSpace;
import com.hbmspace.client.render.OrreryPronter;
import com.hbmspace.entity.mob.MoonCowEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.engine_room.flywheel.api.visualization.ConcurrentRenderStateExtraction;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;

public class MoonCowRenderer
        extends MobRenderer<MoonCowEntity, LivingEntityRenderState, MoonCowModel>
        implements ConcurrentRenderStateExtraction {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(HbmSpace.MOD_ID, "textures/entity/moon_cow.png");

    private final MoonCowModel adult;
    private final MoonCowModel baby;

    public MoonCowRenderer(EntityRendererProvider.Context context) {
        super(context, new MoonCowModel(MoonCowModel.adult().bakeRoot()), 0.7F);
        adult = model;
        baby = new MoonCowModel(MoonCowModel.baby().bakeRoot());
        addLayer(new Helmet(this));
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return TEXTURE;
    }

    @Override
    public void submit(
            LivingEntityRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState camera) {
        model = state.isBaby ? baby : adult;
        super.submit(state, poseStack, collector, camera);
    }

    private static final class Helmet extends RenderLayer<LivingEntityRenderState, MoonCowModel> {

        private static final RenderType GLASS =
                WorldRenderPipeline.oneSidedCutout(
                        RegistryUtil.id("textures/block/glass_boron.png"));
        private static final float[][] NORMALS = {
            {0, 0, -1}, {-1, 0, 0}, {0, 0, 1}, {1, 0, 0}, {0, -1, 0}, {0, 1, 0}
        };

        Helmet(RenderLayerParent<LivingEntityRenderState, MoonCowModel> parent) {
            super(parent);
        }

        @Override
        public void submit(
                PoseStack poseStack,
                SubmitNodeCollector collector,
                int lightCoords,
                LivingEntityRenderState state,
                float yRot,
                float xRot) {
            int overlay = LivingEntityRenderer.getOverlayCoords(state, 0.0F);
            poseStack.pushPose();
            getParentModel().head().translateAndRotate(poseStack);
            poseStack.translate(0.0F, 0.0F, -0.25F);
            poseStack.scale(0.7F, 0.7F, 0.7F);
            collector.submitCustomGeometry(
                    poseStack, GLASS, (pose, buffer) -> cube(pose, buffer, lightCoords, overlay));
            poseStack.popPose();
        }

        private static void cube(
                PoseStack.Pose pose, VertexConsumer buffer, int light, int overlay) {
            float[] v = new float[5];
            for (int i = 0; i < OrreryPronter.CUBE_VERTICES; i++) {
                OrreryPronter.cubeVertex(i, 0, 1, v);
                float[] n = NORMALS[i / 4];
                Vertices.emit(
                        buffer, pose, v[0], v[1], v[2], -1, v[3], v[4], overlay, light, n[0], n[1],
                        n[2]);
            }
        }
    }
}
