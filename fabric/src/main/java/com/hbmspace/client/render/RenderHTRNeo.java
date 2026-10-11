// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.render;

import com.hbm.blocks.multiblock.BlockMultiblockCore;
import com.hbm.client.render.FlatTranslucent;
import com.hbm.client.render.FusionPlasmaRenderTypes;
import com.hbm.main.ResourceManager;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.util.Facing;
import com.hbm.util.GameTime;
import com.hbmspace.blockentity.machine.BlockEntityMachineHTRNeo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class RenderHTRNeo
        implements BlockEntityRenderer<BlockEntityMachineHTRNeo, RenderHTRNeo.State> {

    private static final HFRWavefrontObject MODEL = StationModels.htrf4_neo;
    private static final int ROTOR = MODEL.partId("Rotor");
    private static final int ROTOR_2 = MODEL.partId("Rotor2");
    private static final int EXHAUST = MODEL.partId("Exhaust");
    private static final int FULL_BRIGHT = 0xF000F0;

    private final RenderType bodyType = RenderTypes.entityCutoutCull(StationModels.htrf4_neo_tex);
    private final RenderType exhaustType = FlatTranslucent.of(StationModels.htrf4_exhaust_tex);
    private final RenderType sparkleType =
            FusionPlasmaRenderTypes.plasma(ResourceManager.fusion_plasma_sparkle_tex);

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public int getViewDistance() {
        return 64;
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public void extractRenderState(
            BlockEntityMachineHTRNeo be,
            State state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(
                be, state, partialTicks, cameraPosition, breakProgress);
        state.yaw = Facing.yaw(be.getBlockState().getValue(BlockMultiblockCore.FACING), 90);
        state.rotor = be.prevRotor + (be.rotor - be.prevRotor) * partialTicks;

        float trailStretch = be.getLevel().getRandom().nextFloat();
        trailStretch = 1.2F - (trailStretch * trailStretch * 0.1F);
        state.trailStretch = trailStretch * be.thrustAmount;
        state.thrustAmount = be.thrustAmount;
        state.r = be.plasmaR;
        state.g = be.plasmaG;
        state.b = be.plasmaB;
        state.sparkleSpin = (float) (GameTime.now() / 250D % 1D);
    }

    @Override
    public void submit(State s, PoseStack ps, SubmitNodeCollector col, CameraRenderState camera) {
        int light = s.lightCoords;
        ps.pushPose();
        ps.translate(0.5, -2.0, 0.5);
        ps.mulPose(Axis.YP.rotationDegrees(s.yaw));

        ps.pushPose();
        ps.translate(0, 2.5F, 0);
        ps.mulPose(Axis.ZP.rotationDegrees(s.rotor));
        ps.translate(0, -2.5F, 0);
        col.submitCustomGeometry(
                ps,
                bodyType,
                (pose, buffer) -> {
                    MODEL.renderPart(pose, buffer, light, -1, ROTOR);
                    MODEL.renderPart(pose, buffer, light, -1, ROTOR_2);
                });
        ps.popPose();

        if (s.trailStretch > 0) {
            ps.translate(0, 0, 12);
            ps.scale(1, 1, s.trailStretch);
            ps.translate(0, 0, -12);

            int plume = color(s.r, s.g, s.b, s.thrustAmount);
            col.submitCustomGeometry(
                    ps,
                    exhaustType,
                    (pose, buffer) -> MODEL.renderPart(pose, buffer, FULL_BRIGHT, plume, EXHAUST));

            int sparkle = color(s.r * 2, s.g * 2, s.b * 2, s.thrustAmount * 0.75F);
            float spin = s.sparkleSpin;
            col.submitCustomGeometry(
                    ps,
                    sparkleType,
                    (pose, buffer) ->
                            MODEL.renderPart(
                                    pose, buffer, FULL_BRIGHT, sparkle, EXHAUST, 0F, 1F, 0F, spin));
        }

        ps.popPose();
    }

    private static int color(float r, float g, float b, float a) {
        return ARGB.colorFromFloat(
                Mth.clamp(a, 0F, 1F),
                Mth.clamp(r, 0F, 1F),
                Mth.clamp(g, 0F, 1F),
                Mth.clamp(b, 0F, 1F));
    }

    public static final class State extends BlockEntityRenderState {
        public float yaw;
        public float rotor;
        public float trailStretch;
        public float thrustAmount;
        public float r;
        public float g;
        public float b;
        public float sparkleSpin;
    }
}
