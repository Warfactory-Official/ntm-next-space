// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-FileCopyrightText: 2024 JamesH_2
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.render;

import com.hbm.render.util.BeamPronter.EnumBeamType;
import com.hbm.render.util.BeamPronter.EnumWaveType;
import com.hbm.render.util.BeamPronter;
import com.hbmspace.blockentity.deco.BlockEntityLightningVolcano;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class RenderVol2
        implements BlockEntityRenderer<BlockEntityLightningVolcano, RenderVol2.State> {

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
            BlockEntityLightningVolcano be,
            State state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(
                be, state, partialTicks, cameraPosition, breakProgress);
        state.discharging = be.chargetime == 0;
        state.flash = be.flashd;
        state.gameTime = be.getLevel() == null ? 0L : be.getLevel().getGameTime();
    }

    @Override
    public void submit(
            State state, PoseStack ps, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.discharging) return;

        ps.pushPose();
        ps.translate(0.5, 0.0, 0.5);
        ps.mulPose(Axis.YP.rotationDegrees(90.0F));
        ps.translate(0.0, 0.5, -0.5);
        ps.mulPose(Axis.XP.rotationDegrees(90.0F));
        ps.translate(0.0, 0.5, 0.5);

        float scale = state.flash;
        float alpha = 1.0F - Math.min(1.0F, scale / 110);
        Vec3 skeleton = new Vec3(0, 0, -100 - scale);

        BeamPronter.prontBeam(
                ps,
                collector,
                skeleton,
                EnumWaveType.SPIRAL,
                EnumBeamType.SOLID,
                0x101020,
                0x101020,
                0,
                8,
                0F,
                6,
                3 * alpha,
                alpha * 2F);
        BeamPronter.prontBeam(
                ps,
                collector,
                skeleton,
                EnumWaveType.RANDOM,
                EnumBeamType.SOLID,
                0x202060,
                0x202060,
                (int) (state.gameTime / 2) % 1000,
                26,
                3F,
                2,
                0.0625F * 2,
                0.5F * alpha);
        BeamPronter.prontBeam(
                ps,
                collector,
                skeleton,
                EnumWaveType.RANDOM,
                EnumBeamType.SOLID,
                0x202060,
                0x202060,
                (int) (state.gameTime / 4) % 1000,
                26,
                3F,
                2,
                0.0625F * 2,
                0.5F * alpha);

        ps.popPose();
    }

    public static final class State extends BlockEntityRenderState {
        public boolean discharging;
        public float flash;
        public long gameTime;
    }
}
