/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file under the Apache License except in
 * compliance with that License. You may obtain a copy at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Apache License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License for the specific language governing permissions
 * and limitations under the License.
 */

package com.hbmspace.client.render;

import com.hbm.entity.item.TntNtmEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.engine_room.flywheel.api.visualization.ConcurrentRenderStateExtraction;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.TntMinecartRenderer;
import net.minecraft.client.renderer.entity.TntRenderer;
import net.minecraft.client.renderer.entity.state.TntRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Quaternionfc;

public class TntSpinRenderer extends EntityRenderer<TntNtmEntity, TntSpinRenderer.State>
        implements ConcurrentRenderStateExtraction {

    private final BlockModelResolver blockModelResolver;

    public TntSpinRenderer(EntityRendererProvider.Context context) {
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
        poseStack.rotate((Quaternionfc) Axis.YP.rotationDegrees(state.spinYaw - 90.0F));
        poseStack.rotate((Quaternionfc) Axis.ZP.rotationDegrees(state.spinPitch));
        poseStack.rotate((Quaternionfc) Axis.YP.rotationDegrees(90.0F));
        float fuse = state.fuseRemainingInTicks;
        if (fuse < 10.0F) {
            float scale = 1.0F + TntRenderer.getSwellAmount(fuse);
            poseStack.scale(scale, scale, scale);
        }

        poseStack.rotate((Quaternionfc) Axis.YP.rotationDegrees(-90.0F));
        poseStack.translate(-0.5F, -0.5F, 0.5F);
        poseStack.rotate((Quaternionfc) Axis.YP.rotationDegrees(90.0F));
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
    public void extractRenderState(TntNtmEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.fuseRemainingInTicks = entity.getFuse() - partialTicks + 1.0F;
        this.blockModelResolver.update(
                state.blockState, entity.getBlockState(), TntRenderer.BLOCK_DISPLAY_CONTEXT);
        state.spinYaw =
                ((com.hbmspace.entity.SpinningCharge) (entity)).hbmspace$spinYaw(partialTicks);
        state.spinPitch =
                ((com.hbmspace.entity.SpinningCharge) (entity)).hbmspace$spinPitch(partialTicks);
    }

    public static final class State extends TntRenderState {
        public float spinYaw;
        public float spinPitch;
    }
}
