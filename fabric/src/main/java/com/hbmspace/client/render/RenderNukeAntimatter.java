// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.render;

import com.hbm.blocks.machine.BlockMachineHorizontal;
import com.hbm.client.render.FlatCutout;
import com.hbm.client.render.RenderNukeFstbmb;
import com.hbm.main.ResourceManager;
import com.hbm.render.util.BeamPronter.EnumBeamType;
import com.hbm.render.util.BeamPronter.EnumWaveType;
import com.hbm.render.util.BeamPronter;
import com.hbm.util.GameTime;
import com.hbmspace.blockentity.bomb.BlockEntityNukeAntimatter;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class RenderNukeAntimatter
        implements BlockEntityRenderer<BlockEntityNukeAntimatter, RenderNukeAntimatter.State> {

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
    public void extractRenderState(
            BlockEntityNukeAntimatter be,
            State state,
            float partialTick,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(
                be, state, partialTick, cameraPosition, breakProgress);
        state.core = be.core;
        state.yaw =
                RenderNukeFstbmb.yawFor(be.getBlockState().getValue(BlockMachineHorizontal.FACING));
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
        ps.mulPose(Axis.YP.rotationDegrees(s.yaw));

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
