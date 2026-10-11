// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

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
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

public class RenderSpinningPart<T extends BlockEntity>
        implements BlockEntityRenderer<T, RenderSpinningPart.State>,
                ConcurrentRenderStateExtraction {

    private final SpinningPart<T> part;
    private final RenderType type;
    private final int groupId;

    public RenderSpinningPart(SpinningPart<T> part) {
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
