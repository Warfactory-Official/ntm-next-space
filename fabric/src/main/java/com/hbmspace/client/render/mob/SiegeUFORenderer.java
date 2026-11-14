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

import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.util.GameTime;
import com.hbmspace.entity.mob.siege.SiegeUFOEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.engine_room.flywheel.api.visualization.ConcurrentRenderStateExtraction;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class SiegeUFORenderer extends EntityRenderer<SiegeUFOEntity, SiegeUFORenderer.State>
        implements ConcurrentRenderStateExtraction {

    private final HFRWavefrontObject mesh = SiegeModels.MINI_UFO.get();
    private final RenderType[] types =
            SiegeModels.UFO_SKINS.stream()
                    .map(RenderTypes::entityCutout)
                    .toArray(RenderType[]::new);

    public SiegeUFORenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(SiegeUFOEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.tier = entity.getTier().id;
        state.pose.identity();
        pose(state.pose, entity, partialTicks);
    }

    static void pose(Matrix4f pose, SiegeUFOEntity entity, float partialTick) {
        pose.translate(0F, 0.25F, 0F);
        pose.rotateY((float) ((entity.tickCount + partialTick) * 5D % 360D) * Mth.DEG_TO_RAD);
        if (!entity.isAlive()) {
            pose.rotate(
                    new Quaternionf()
                            .rotateAxis(
                                    (entity.deathTime + partialTick) * 5F * Mth.DEG_TO_RAD,
                                    0.70710677F,
                                    0F,
                                    0.70710677F));
        } else if (((LivingEntity) entity).invulnerableTime > 0) {
            pose.rotateX(
                    (float)
                                    (Math.sin(GameTime.now() * 0.01D)
                                            * (((LivingEntity) entity).invulnerableTime
                                                    - partialTick))
                            * Mth.DEG_TO_RAD);
        }
    }

    @Override
    public void submit(
            State state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState camera) {
        int light = state.lightCoords;
        poseStack.pushPose();
        poseStack.mulPose(state.pose);
        collector.submitCustomGeometry(
                poseStack,
                types[state.tier],
                (pose, buffer) -> mesh.render(pose, buffer, light, -1));
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    public static final class State extends EntityRenderState {
        final Matrix4f pose = new Matrix4f();
        int tier;
    }
}
