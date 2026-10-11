// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.blockentity.machine.BlockEntityMachineLPW2;
import com.hbm.client.render.RenderLPW2;
import com.hbm.util.BobMathUtil;
import com.hbm.util.GameTime;
import com.hbmspace.blockentity.machine.BlockEntityMachineLPW2Space;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderLPW2.class)
public abstract class MixinRenderLPW2 {
    @ModifyVariable(
            method =
                    "extractRenderState(Lcom/hbm/blockentity/machine/BlockEntityMachineLPW2;Lcom/hbm/client/render/RenderLPW2$State;FLnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V",
            at = @At("STORE"),
            ordinal = 0)
    private double hbmspace$engineTime(
            double time,
            BlockEntityMachineLPW2 be,
            RenderLPW2.State state,
            float partialTicks,
            Vec3 camera,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breaking) {
        return ((BlockEntityMachineLPW2Space) be).renderTime(partialTicks);
    }

    @Inject(
            method =
                    "extractRenderState(Lcom/hbm/blockentity/machine/BlockEntityMachineLPW2;Lcom/hbm/client/render/RenderLPW2$State;FLnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V",
            at = @At("TAIL"))
    private void hbmspace$terminalTime(
            BlockEntityMachineLPW2 be,
            RenderLPW2.State state,
            float partialTicks,
            Vec3 camera,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breaking,
            CallbackInfo ci) {
        double timer = ((GameTime.millis(be.getLevel()) / 50L) % 1_000_000L + partialTicks) / 3D;
        state.errorScroll = (BobMathUtil.sps(timer) + timer / 2D) % 1;
    }
}
