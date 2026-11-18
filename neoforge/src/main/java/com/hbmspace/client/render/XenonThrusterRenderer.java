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
import com.hbm.client.render.FlatTranslucent;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.util.Facing;
import com.hbmspace.blockentity.machine.XenonThrusterBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;
import org.jspecify.annotations.Nullable;

public class XenonThrusterRenderer
        implements BlockEntityRenderer<XenonThrusterBlockEntity, XenonThrusterRenderer.State> {

    private static final int EXHAUST = StationModels.xenon_thruster.partId("Exhaust");
    private static final int FULL_BRIGHT = 0xF000F0;

    private final HFRWavefrontObject model = StationModels.xenon_thruster;
    private final RenderType exhaustType = FlatTranslucent.of(StationModels.xenon_exhaust_tex);

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
    public AABB getRenderBoundingBox(XenonThrusterBlockEntity be) {
        BlockPos pos = be.getBlockPos();
        return new AABB(
                pos.getX() - 4,
                pos.getY() - 1,
                pos.getZ() - 4,
                pos.getX() + 5,
                pos.getY() + 2,
                pos.getZ() + 5);
    }

    @Override
    public void extractRenderState(
            XenonThrusterBlockEntity be,
            State state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(
                be, state, partialTicks, cameraPosition, breakProgress);
        state.yaw = Facing.yaw(be.getBlockState().getValue(MultiblockCoreBlock.FACING), 0);

        float trailStretch = be.getLevel().getRandom().nextFloat();
        trailStretch = 1.2F - (trailStretch * trailStretch * 0.2F);
        state.trailStretch = trailStretch * be.thrustAmount;
        state.thrustAmount = be.thrustAmount;
    }

    @Override
    public void submit(State s, PoseStack ps, SubmitNodeCollector col, CameraRenderState camera) {
        if (s.trailStretch <= 0) return;

        ps.pushPose();
        ps.translate(0.5, -1.0, 0.5);
        ps.rotate((Quaternionfc) Axis.YP.rotationDegrees(s.yaw));
        ps.translate(0, 0, -0.5);

        ps.translate(0, 0, -1F);
        ps.scale(1, 1, s.trailStretch);
        ps.translate(0, 0, 1F);

        int color = ARGB.colorFromFloat(s.thrustAmount, 1F, 1F, 1F);
        final HFRWavefrontObject m = this.model;
        col.submitCustomGeometry(
                ps,
                exhaustType,
                (pose, buffer) -> m.renderPart(pose, buffer, FULL_BRIGHT, color, EXHAUST));
        ps.popPose();
    }

    public static final class State extends BlockEntityRenderState {
        public float yaw;
        public float trailStretch;
        public float thrustAmount;
    }
}
