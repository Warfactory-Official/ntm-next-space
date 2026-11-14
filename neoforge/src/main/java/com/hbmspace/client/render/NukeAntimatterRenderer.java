/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2023-2024 JamesH_2
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2023-2024 JamesH_2. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only,
 * as published by the Free Software Foundation.
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this code. If not, see <https://www.gnu.org/licenses/>.
 *
 * Under GPLv3 §7(c) and §7(e), these additional terms apply to LGPL-covered
 * material authored by movblock:
 *
 * - the license grants no rights to the name "NTM NEXT: Space";
 * - a modified version must not be named NTM NEXT: Space or presented as
 *   the official release, and must be marked as different from it.
 *
 * Accurately identifying a work as derived from NTM NEXT: Space does not
 * violate these additional terms, provided it is clearly distinguished from
 * the official release and does not imply endorsement. Ports are encouraged
 * to acknowledge NTM NEXT: Space as their upstream project and provide a
 * link to it.
 */

package com.hbmspace.client.render;

import com.hbm.blocks.machine.HorizontalMachineBlock;
import com.hbm.client.render.FlatCutout;
import com.hbm.client.render.NukeFstbmbRenderer;
import com.hbm.main.ResourceManager;
import com.hbm.render.util.BeamPronter.EnumBeamType;
import com.hbm.render.util.BeamPronter.EnumWaveType;
import com.hbm.render.util.BeamPronter;
import com.hbm.util.GameTime;
import com.hbmspace.blockentity.bomb.NukeAntimatterBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;
import org.jspecify.annotations.Nullable;

public class NukeAntimatterRenderer
        implements BlockEntityRenderer<NukeAntimatterBlockEntity, NukeAntimatterRenderer.State> {

    static final Vec3 BEAM = new Vec3(0, 2.5, 0);
    static final double SCALE = 0.1;
    static final int CORE_COLOR = 0xFF010101;
    static final float BEAM_ALPHA = 0.5F;

    private final RenderType sphereType = FlatCutout.culled(ResourceManager.white_tex);

    @Override
    public State createRenderState() {
        return new State();
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
    public AABB getRenderBoundingBox(NukeAntimatterBlockEntity be) {
        return AABB.INFINITE;
    }

    @Override
    public void extractRenderState(
            NukeAntimatterBlockEntity be,
            State state,
            float partialTick,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(
                be, state, partialTick, cameraPosition, breakProgress);
        state.core = be.core;
        state.yaw =
                NukeFstbmbRenderer.yawFor(
                        be.getBlockState().getValue(HorizontalMachineBlock.FACING));
        double ticks = GameTime.now() / 50D;
        state.bob = (float) Math.sin(ticks * 0.1D % (Math.PI * 2D));
        long tick = (long) ticks;
        state.seedHalf = (int) (tick / 2L % 1000L);
        state.seedQuarter = (int) (tick / 4L % 1000L);
    }

    @Override
    public void submit(State s, PoseStack ps, SubmitNodeCollector col, CameraRenderState camera) {
        if (!s.core) return;
        int light = s.lightCoords;

        ps.pushPose();
        ps.translate(0.5F, 0F, 0.5F);
        ps.rotate((Quaternionfc) Axis.YP.rotationDegrees(s.yaw));

        ps.pushPose();
        ps.translate(0, 2.4D + s.bob * 0.125D * SCALE, 0);
        ps.scale((float) SCALE, (float) SCALE, (float) SCALE);
        col.submitCustomGeometry(
                ps,
                sphereType,
                (pose, buffer) ->
                        ResourceManager.sphere_uv.render(pose, buffer, light, CORE_COLOR));
        ps.popPose();

        ps.translate(0, 1, 0);
        BeamPronter.prontBeam(
                ps,
                col,
                BEAM,
                EnumWaveType.SPIRAL,
                EnumBeamType.SOLID,
                0x101020,
                0x101020,
                0,
                1,
                0F,
                6,
                (float) SCALE * 0.1F,
                BEAM_ALPHA);
        BeamPronter.prontBeam(
                ps,
                col,
                BEAM,
                EnumWaveType.RANDOM,
                EnumBeamType.SOLID,
                0x202060,
                0x202060,
                s.seedHalf,
                6,
                (float) SCALE,
                2,
                0.0625F * (float) SCALE,
                BEAM_ALPHA);
        BeamPronter.prontBeam(
                ps,
                col,
                BEAM,
                EnumWaveType.RANDOM,
                EnumBeamType.SOLID,
                0x202060,
                0x202060,
                s.seedQuarter,
                6,
                (float) SCALE,
                2,
                0.0625F * (float) SCALE,
                BEAM_ALPHA);
        ps.popPose();
    }

    public static final class State extends BlockEntityRenderState {
        public boolean core;
        public float yaw;
        public float bob;
        public int seedHalf;
        public int seedQuarter;
    }
}
