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

import com.hbm.blocks.multiblock.MultiblockCoreBlock;
import com.hbm.util.Facing;
import com.hbmspace.blockentity.machine.OrbitalStationLauncherBlockEntity;
import com.hbmspace.handler.RocketStruct;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class StationLauncherRenderer
        implements BlockEntityRenderer<
                OrbitalStationLauncherBlockEntity, StationLauncherRenderer.State> {

    private final PosedPartsRenderer<OrbitalStationLauncherBlockEntity> arms;
    private final RocketPronter pronter = new RocketPronter();

    public StationLauncherRenderer(PosedParts<OrbitalStationLauncherBlockEntity> arms) {
        this.arms = new PosedPartsRenderer<>(arms);
    }

    @Override
    public State createRenderState() {
        return new State(arms.createRenderState());
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
            OrbitalStationLauncherBlockEntity be,
            State state,
            float partialTick,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(
                be, state, partialTick, cameraPosition, breakProgress);
        arms.extractRenderState(be, state.arms, partialTick, cameraPosition, breakProgress);
        state.rocket = be.rocket.copy();
        state.yaw = Facing.yaw(be.getBlockState().getValue(MultiblockCoreBlock.FACING), 0);
    }

    @Override
    public void submit(
            State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        arms.submit(state.arms, pose, collector, camera);

        pose.pushPose();
        StationRenders.launcherRocketOrigin(pose, state.yaw, state.rocket);
        pronter.pront(state.rocket, null, pose, collector, state.lightCoords, false, 0, 0, 0F);
        pose.popPose();
    }

    public static final class State extends BlockEntityRenderState {
        final PosedPartsRenderer.State arms;
        RocketStruct rocket = new RocketStruct();
        float yaw;

        State(PosedPartsRenderer.State arms) {
            this.arms = arms;
        }
    }
}
