// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.render;

import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.client.render.FlatTranslucent;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.util.Facing;
import com.hbmspace.blockentity.machine.BlockEntityXenonThruster;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class RenderXenonThruster
        implements BlockEntityRenderer<BlockEntityXenonThruster, RenderXenonThruster.State> {

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
    public void extractRenderState(
            BlockEntityXenonThruster be,
            State state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(
                be, state, partialTicks, cameraPosition, breakProgress);
        state.yaw = Facing.yaw(be.getBlockState().getValue(BlockMultiblockCore.FACING), 0);

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
        ps.mulPose(Axis.YP.rotationDegrees(s.yaw));
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
