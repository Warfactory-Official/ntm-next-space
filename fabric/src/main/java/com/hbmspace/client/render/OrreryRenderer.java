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

package com.hbmspace.client.render;

import com.hbm.client.render.HybridBlockEntityRenderState;
import com.hbm.render.util.Vertices;
import com.hbmspace.blockentity.deco.OrreryBlockEntity;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.dim.SolarSystem.OrreryMetric;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import java.util.List;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class OrreryRenderer
        implements BlockEntityRenderer<OrreryBlockEntity, OrreryRenderer.State> {

    private static final int LIT =
            ARGB.colorFromFloat(1.0F, OrreryPronter.LIT, OrreryPronter.LIT, OrreryPronter.LIT);

    public static final class State extends HybridBlockEntityRenderState {
        OrreryPronter.@Nullable Scene scene;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(
            OrreryBlockEntity be,
            State state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(
                be, state, partialTicks, cameraPosition, breakProgress);
        state.scene =
                !state.forceNative && VisualizationManager.supportsVisualization(be.getLevel())
                        ? null
                        : OrreryPronter.scene(be.getLevel(), partialTicks);
    }

    @Override
    public void submit(
            State state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState camera) {
        OrreryPronter.Scene scene = state.scene;
        if (scene == null) return;

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        collector.submitCustomGeometry(
                poseStack,
                OrreryRenderTypes.cube(scene.sun().texture),
                (pose, buf) -> cube(pose, buf, OrreryPronter.SUN_UV_MIN, OrreryPronter.SUN_UV_MAX));

        double scale = OrreryPronter.scale(scene.sun());
        poseStack.scale((float) scale, (float) scale, (float) scale);

        for (OrreryMetric metric : scene.metrics()) {
            CelestialBody body = metric.body;
            float size = (float) OrreryPronter.bodyScale(body);
            poseStack.pushPose();
            poseStack.translate(metric.position.x, metric.position.z, metric.position.y);
            poseStack.scale(size, size, size);
            collector.submitCustomGeometry(
                    poseStack,
                    OrreryRenderTypes.cube(body.texture),
                    (pose, buf) -> cube(pose, buf, 0.0F, 1.0F));
            poseStack.popPose();
        }

        List<OrreryMetric> metrics = scene.metrics();
        collector.submitCustomGeometry(
                poseStack,
                OrreryRenderTypes.LINES,
                (pose, buf) -> {
                    for (OrreryMetric metric : metrics) orbit(pose, buf, metric);
                });

        poseStack.popPose();
    }

    private static void cube(PoseStack.Pose pose, VertexConsumer buf, float uvMin, float uvMax) {
        float[] vertex = new float[5];
        for (int i = 0; i < OrreryPronter.CUBE_VERTICES; i++) {
            OrreryPronter.cubeVertex(i, uvMin, uvMax, vertex);
            Vertices.emit(
                    buf,
                    pose,
                    vertex[0],
                    vertex[1],
                    vertex[2],
                    LIT,
                    vertex[3],
                    vertex[4],
                    LightCoordsUtil.FULL_BRIGHT,
                    0.0F,
                    0.0F,
                    1.0F);
        }
    }

    private static void orbit(PoseStack.Pose pose, VertexConsumer buf, OrreryMetric metric) {
        int color = OrreryPronter.orbitColor(metric.body);
        Vec3[] path = metric.orbitalPath;
        for (int i = 0; i < path.length; i++) {
            Vec3 from = path[i];
            Vec3 to = path[(i + 1) % path.length];
            float dx = (float) (to.x - from.x);
            float dy = (float) (to.z - from.z);
            float dz = (float) (to.y - from.y);
            float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
            dx /= length;
            dy /= length;
            dz /= length;
            Vertices.emitLine(
                    buf,
                    pose,
                    (float) from.x,
                    (float) from.z,
                    (float) from.y,
                    color,
                    dx,
                    dy,
                    dz,
                    1.0F);
            Vertices.emitLine(
                    buf, pose, (float) to.x, (float) to.z, (float) to.y, color, dx, dy, dz, 1.0F);
        }
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}
