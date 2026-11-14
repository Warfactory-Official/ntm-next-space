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
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.util.Facing;
import com.hbmspace.blockentity.machine.HydroponicBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.engine_room.flywheel.api.visualization.ConcurrentRenderStateExtraction;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;
import org.jspecify.annotations.Nullable;

public class HydroponicRenderer
        implements BlockEntityRenderer<HydroponicBlockEntity, HydroponicRenderer.State>,
                ConcurrentRenderStateExtraction {

    private final HFRWavefrontObject mesh;
    private final int lights;
    private final RenderType type;

    public HydroponicRenderer(HFRWavefrontObject mesh, Identifier texture) {
        this.mesh = mesh;
        this.lights = mesh.partId("Lights");
        this.type = IndustryRenderTypes.lamp(texture);
    }

    public static float yaw(Direction facing) {
        return Facing.yaw(facing, 90);
    }

    public static boolean lit(HydroponicBlockEntity be) {
        return be.power >= HydroponicBlockEntity.LIGHT_POWER;
    }

    public static AABB extent(BlockPos pos) {
        return new AABB(
                pos.getX() - 2,
                pos.getY(),
                pos.getZ() - 2,
                pos.getX() + 3,
                pos.getY() + 3,
                pos.getZ() + 3);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public AABB getRenderBoundingBox(HydroponicBlockEntity be) {
        return extent(be.getBlockPos());
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public void extractRenderState(
            HydroponicBlockEntity be,
            State state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(
                be, state, partialTicks, cameraPosition, breakProgress);
        state.facing = MultiblockCoreBlock.coreFacing(be.getBlockState());
        state.lit = lit(be);
    }

    @Override
    public void submit(
            State state, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.lit) return;
        ps.pushPose();
        ps.translate(0.5, 0.0, 0.5);
        ps.rotate((Quaternionfc) Axis.YP.rotationDegrees(yaw(state.facing)));
        collector.submitCustomGeometry(
                ps,
                type,
                (pose, buffer) ->
                        mesh.renderPart(pose, buffer, LightCoordsUtil.FULL_BRIGHT, -1, lights));
        ps.popPose();
    }

    public static final class State extends BlockEntityRenderState {
        Direction facing = Direction.NORTH;
        boolean lit;
    }
}
