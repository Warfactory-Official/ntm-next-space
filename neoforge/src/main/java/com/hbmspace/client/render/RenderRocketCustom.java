// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.render;

import com.hbm.util.GameTime;
import com.hbmspace.dim.CelestialBody;
import com.hbmspace.entity.missile.EntityRideableRocket.RocketState;
import com.hbmspace.entity.missile.EntityRideableRocket;
import com.hbmspace.handler.RocketStruct;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

public class RenderRocketCustom
        extends EntityRenderer<EntityRideableRocket, RenderRocketCustom.State> {

    private final RocketPronter pronter = new RocketPronter();

    public RenderRocketCustom(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0F;
    }

    @Override
    protected boolean affectedByCulling(EntityRideableRocket entity) {
        return false;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityRideableRocket entity, State state, float partialTicks) {
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

    private static RocketPronter.PodPose pod(EntityRideableRocket entity, float partialTicks) {
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
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yaw - 90F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(state.pitch));
        poseStack.mulPose(Axis.YN.rotationDegrees(state.yaw - 90F));
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
