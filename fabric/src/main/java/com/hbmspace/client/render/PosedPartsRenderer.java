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

import com.hbm.render.loader.HFRWavefrontObject;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.engine_room.flywheel.api.visualization.ConcurrentRenderStateExtraction;
import java.util.List;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

public class PosedPartsRenderer<T extends BlockEntity>
        implements BlockEntityRenderer<T, PosedPartsRenderer.State>,
                ConcurrentRenderStateExtraction {

    private final PosedParts<T> parts;
    private final RenderType[] types;
    private final int[] groups;

    public PosedPartsRenderer(PosedParts<T> parts) {
        this.parts = parts;
        List<PosedParts.Part<T>> list = parts.parts();
        types = new RenderType[list.size()];
        groups = new int[list.size()];
        for (int i = 0; i < types.length; i++) {
            PosedParts.Part<T> part = list.get(i);
            types[i] =
                    part.cull()
                            ? RenderTypes.entityCutoutCull(part.texture())
                            : RenderTypes.entityCutout(part.texture());
            groups[i] = part.groupId();
        }
    }

    @Override
    public State createRenderState() {
        return new State(types.length);
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
    public void extractRenderState(
            T be,
            State state,
            float partialTick,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(
                be, state, partialTick, cameraPosition, breakProgress);
        List<PosedParts.Part<T>> list = parts.parts();
        for (int i = 0; i < state.poses.length; i++) {
            state.drawn[i] = list.get(i).pose().at(be, partialTick, state.poses[i].identity());
        }
    }

    @Override
    public void submit(
            State state, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
        int light = state.lightCoords;
        List<PosedParts.Part<T>> list = parts.parts();
        for (int i = 0; i < state.poses.length; i++) {
            if (!state.drawn[i]) continue;
            HFRWavefrontObject mesh = list.get(i).mesh();
            int group = groups[i];
            ps.pushPose();
            ps.mulPose(state.poses[i]);
            collector.submitCustomGeometry(
                    ps,
                    types[i],
                    (pose, buffer) -> mesh.renderPart(pose, buffer, light, -1, group));
            ps.popPose();
        }
    }

    public static final class State extends BlockEntityRenderState {
        final Matrix4f[] poses;
        final boolean[] drawn;

        State(int parts) {
            poses = new Matrix4f[parts];
            for (int i = 0; i < parts; i++) poses[i] = new Matrix4f();
            drawn = new boolean[parts];
        }
    }
}
