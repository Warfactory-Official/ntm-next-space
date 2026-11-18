/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2024 George Paton
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2024 George Paton. All rights reserved.
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

import com.hbm.util.GameTime;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.entity.missile.RideableRocketEntity.RocketState;
import com.hbmspace.entity.missile.RideableRocketEntity;
import com.hbmspace.handler.RocketStruct;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import org.joml.Quaternionfc;
import org.jspecify.annotations.Nullable;

public class RocketCustomRenderer
        extends EntityRenderer<RideableRocketEntity, RocketCustomRenderer.State> {

    private final RocketPronter pronter = new RocketPronter();

    public RocketCustomRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0F;
    }

    @Override
    protected boolean affectedByCulling(RideableRocketEntity entity) {
        return false;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(RideableRocketEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.yaw = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot());
        state.pitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        state.rocket = entity.getRocket();
        state.deployed = entity.preview || !CelestialBody.inOrbit(entity.level());
        state.decoupleTimer = entity.decoupleTimer;
        state.shroudTimer = entity.shroudTimer;
        state.partialTick = partialTicks;
        state.pod = entity.isReusable() && !entity.preview ? pod(entity, partialTicks) : null;
    }

    private static RocketPronter.PodPose pod(RideableRocketEntity entity, float partialTicks) {
        if (CelestialBody.inOrbit(entity.level())) return new RocketPronter.PodPose(true, 0, 0, 0);

        RocketState rocketState = entity.getState();
        float lerpTimer = entity.getStateTimer() + partialTicks;

        float door = 0;
        if (rocketState == RocketState.LANDED) {
            door = Mth.clamp(lerpTimer * 2, 0, 90);
        } else if (rocketState == RocketState.AWAITING) {
            door = Mth.clamp(90 - lerpTimer * 2, 0, 90);
        }

        float airbrake = 0;
        if (rocketState == RocketState.LANDING) {
            if (entity.podBrakes) {
                airbrake = 65;
            } else {
                float t = Mth.clamp((GameTime.now() - entity.podBrakeStart) / 1000F, 0, 1);
                airbrake = (1 - t) * 65;
            }
        }

        return new RocketPronter.PodPose(
                false, door, airbrake, rocketState == RocketState.LAUNCHING ? 0 : 1);
    }

    @Override
    public void submit(
            State state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.rotate((Quaternionfc) Axis.YP.rotationDegrees(state.yaw - 90F));
        poseStack.rotate((Quaternionfc) Axis.ZP.rotationDegrees(state.pitch));
        poseStack.rotate((Quaternionfc) Axis.YN.rotationDegrees(state.yaw - 90F));
        pronter.pront(
                state.rocket,
                state.pod,
                poseStack,
                collector,
                state.lightCoords,
                state.deployed,
                state.decoupleTimer,
                state.shroudTimer,
                state.partialTick);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    public static final class State extends EntityRenderState {
        public float yaw;
        public float pitch;
        public RocketStruct rocket;
        public boolean deployed;
        public int decoupleTimer;
        public int shroudTimer;
        public float partialTick;
        public RocketPronter.@Nullable PodPose pod;
    }
}
