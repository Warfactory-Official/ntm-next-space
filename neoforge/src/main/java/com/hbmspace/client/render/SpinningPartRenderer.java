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

import com.mojang.blaze3d.vertex.PoseStack;
import dev.engine_room.flywheel.api.visualization.ConcurrentRenderStateExtraction;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

public class SpinningPartRenderer<T extends BlockEntity>
        implements BlockEntityRenderer<T, SpinningPartRenderer.State>,
                ConcurrentRenderStateExtraction {

    private final SpinningPart<T> part;
    private final RenderType type;
    private final int groupId;

    public SpinningPartRenderer(SpinningPart<T> part) {
        this.part = part;
        this.type =
                part.cull()
                        ? RenderTypes.entityCutoutCull(part.texture())
                        : RenderTypes.entityCutout(part.texture());
        this.groupId = part.groupId();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(T be) {
        return part.extent().at(be.getBlockPos());
    }

    @Override
    public void extractRenderState(
            T be,
            State state,
            float partialTick,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(
                be, state, partialTick, cameraPosition, breakProgress);
        part.pose(be.getBlockState(), part.angle().at(be, partialTick), state.pose.identity());
    }

    @Override
    public void submit(
            State state, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
        int light = state.lightCoords;
        ps.pushPose();
        ps.mulPose(state.pose);
        collector.submitCustomGeometry(
                ps,
                type,
                (pose, buffer) -> part.mesh().renderPart(pose, buffer, light, -1, groupId));
        ps.popPose();
    }

    public static final class State extends BlockEntityRenderState {
        final Matrix4f pose = new Matrix4f();
    }
}
