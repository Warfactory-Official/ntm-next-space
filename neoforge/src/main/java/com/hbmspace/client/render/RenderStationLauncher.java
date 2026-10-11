// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.render;

import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.util.Facing;
import com.hbmspace.blockentity.machine.BlockEntityOrbitalStationLauncher;
import com.hbmspace.handler.RocketStruct;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class RenderStationLauncher
        implements BlockEntityRenderer<
                BlockEntityOrbitalStationLauncher, RenderStationLauncher.State> {

    private final RenderPosedParts<BlockEntityOrbitalStationLauncher> arms;
    private final RocketPronter pronter = new RocketPronter();

    public RenderStationLauncher(PosedParts<BlockEntityOrbitalStationLauncher> arms) {
        this.arms = new RenderPosedParts<>(arms);
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
    public AABB getRenderBoundingBox(BlockEntityOrbitalStationLauncher be) {
        return arms.getRenderBoundingBox(be)
                .minmax(StationRenders.launcherRocketBox(be.getBlockPos(), be.rocket));
    }

    @Override
    public void extractRenderState(
            BlockEntityOrbitalStationLauncher be,
            State state,
            float partialTick,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(
                be, state, partialTick, cameraPosition, breakProgress);
        arms.extractRenderState(be, state.arms, partialTick, cameraPosition, breakProgress);
        state.rocket = be.rocket.copy();
        state.yaw = Facing.yaw(be.getBlockState().getValue(BlockMultiblockCore.FACING), 0);
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
        final RenderPosedParts.State arms;
        RocketStruct rocket = new RocketStruct();
        float yaw;

        State(RenderPosedParts.State arms) {
            this.arms = arms;
        }
    }
}
