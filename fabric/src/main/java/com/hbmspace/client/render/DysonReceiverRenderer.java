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

import com.hbm.render.util.BeamPronter;
import com.hbmspace.blockentity.machine.DysonReceiverBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.engine_room.flywheel.api.visualization.ConcurrentRenderStateExtraction;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

public class DysonReceiverRenderer
        implements BlockEntityRenderer<DysonReceiverBlockEntity, DysonReceiverRenderer.State>,
                ConcurrentRenderStateExtraction {
    public static final int COLOR = 0xFF8800;
    public static final float ALPHA = 0.5F;
    public static final float THICKNESS = 0.4F;
    public static final float SPIRAL_SIZE = 0F;
    public static final float RANDOM_SIZE = 0.0625F;
    public static final int LAYERS = 2;
    public static final float AXIS = 1.5F;

    private final PosedPartsRenderer<DysonReceiverBlockEntity> coils =
            new PosedPartsRenderer<>(SkyRenders.Parts.RECEIVER_PARTS);

    @Override
    public State createRenderState() {
        return new State(coils.createRenderState());
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
            DysonReceiverBlockEntity be,
            State state,
            float partialTick,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(
                be, state, partialTick, cameraPosition, breakProgress);
        coils.extractRenderState(be, state.coils, partialTick, cameraPosition, breakProgress);
        state.length = be.isReceiving ? be.beamLength : 0;
        state.phase = (int) (be.getLevel().getGameTime() % 1000);
        SkyRenders.base(be, state.base.identity()).translate(0F, AXIS, 0F);
    }

    @Override
    public void submit(
            State state, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
        coils.submit(state.coils, ps, collector, camera);
        if (state.length <= 0) return;

        Vec3 skeleton = new Vec3(0, 0, state.length);
        ps.pushPose();
        ps.mulPose(state.base);
        BeamPronter.prontBeam(
                ps,
                collector,
                skeleton,
                BeamPronter.EnumWaveType.SPIRAL,
                BeamPronter.EnumBeamType.SOLID,
                COLOR,
                COLOR,
                0,
                1,
                SPIRAL_SIZE,
                LAYERS,
                THICKNESS,
                ALPHA);
        BeamPronter.prontBeam(
                ps,
                collector,
                skeleton,
                BeamPronter.EnumWaveType.RANDOM,
                BeamPronter.EnumBeamType.SOLID,
                COLOR,
                COLOR,
                state.phase,
                state.length / 2,
                RANDOM_SIZE,
                LAYERS,
                THICKNESS,
                ALPHA);
        ps.popPose();
    }

    public static final class State extends BlockEntityRenderState {
        final PosedPartsRenderer.State coils;
        final Matrix4f base = new Matrix4f();
        int length;
        int phase;

        State(PosedPartsRenderer.State coils) {
            this.coils = coils;
        }
    }
}
