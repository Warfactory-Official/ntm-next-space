// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class RenderRocketHost<T extends BlockEntity, S>
        implements BlockEntityRenderer<T, RenderRocketHost.State<S>> {

    private final RocketHost<T, S> host;
    private final RocketPronter pronter = new RocketPronter();

    public RenderRocketHost(RocketHost<T, S> host) {
        this.host = host;
    }

    @Override
    public State<S> createRenderState() {
        return new State<>();
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
    public AABB getRenderBoundingBox(T be) {
        return host.bounds(be.getBlockPos(), host.snapshot(be));
    }

    @Override
    public void extractRenderState(
            T be,
            State<S> state,
            float partialTick,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(
                be, state, partialTick, cameraPosition, breakProgress);
        state.snapshot = host.snapshot(be);
    }

    @Override
    public void submit(
            State<S> state,
            PoseStack pose,
            SubmitNodeCollector collector,
            CameraRenderState camera) {
        int light = state.lightCoords;
        host.draw(
                state.snapshot,
                pronter,
                pose,
                (piece, cull) ->
                        collector.submitCustomGeometry(
                                pose,
                                cull
                                        ? RenderTypes.entityCutoutCull(piece.texture())
                                        : RenderTypes.entityCutout(piece.texture()),
                                (p, buffer) -> piece.render(p, buffer, light)));
    }

    public static final class State<S> extends BlockEntityRenderState {
        S snapshot;
    }
}
