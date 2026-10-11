// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.client.render;

import com.hbm.entity.item.EntityTntNtm;
import com.hbmspace.entity.SpinningCharge;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.TntMinecartRenderer;
import net.minecraft.client.renderer.entity.TntRenderer;
import net.minecraft.client.renderer.entity.state.TntRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;

public class RenderTntSpin extends EntityRenderer<EntityTntNtm, RenderTntSpin.State> {

    private final BlockModelResolver blockModelResolver;

    public RenderTntSpin(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
        this.blockModelResolver = context.getBlockModelResolver();
    }

    @Override
    public void submit(
            State state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.5F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.spinYaw - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(state.spinPitch));
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        float fuse = state.fuseRemainingInTicks;
        if (fuse < 10.0F) {
            float scale = 1.0F + TntRenderer.getSwellAmount(fuse);
            poseStack.scale(scale, scale, scale);
        }

        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        poseStack.translate(-0.5F, -0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        if (!state.blockState.isEmpty()) {
            TntMinecartRenderer.submitWhiteSolidBlock(
                    state.blockState,
                    poseStack,
                    submitNodeCollector,
                    state.lightCoords,
                    TntRenderer.isLit(fuse),
                    state.outlineColor);
        }

        poseStack.popPose();
        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityTntNtm entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.fuseRemainingInTicks = entity.getFuse() - partialTicks + 1.0F;
        this.blockModelResolver.update(
                state.blockState, entity.getBlockState(), TntRenderer.BLOCK_DISPLAY_CONTEXT);
        SpinningCharge spin = (SpinningCharge) entity;
        state.spinYaw = spin.hbmspace$spinYaw(partialTicks);
        state.spinPitch = spin.hbmspace$spinPitch(partialTicks);
    }

    public static final class State extends TntRenderState {
        public float spinYaw;
        public float spinPitch;
    }
}
