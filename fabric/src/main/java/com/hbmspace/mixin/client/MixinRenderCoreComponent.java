// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin.client;

import com.hbm.client.render.RenderCoreComponent;
import com.hbm.render.util.BeamPronter.EnumBeamType;
import com.hbm.render.util.BeamPronter.EnumWaveType;
import com.hbmspace.client.render.BeamAlphaSpace;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RenderCoreComponent.class)
public abstract class MixinRenderCoreComponent {
    @WrapOperation(method = "submit", at = @At(value = "INVOKE", target = BeamAlphaSpace.PRONT))
    private void hbmspace$halfAlpha(
            PoseStack ps,
            SubmitNodeCollector collector,
            Vec3 skeleton,
            EnumWaveType wave,
            EnumBeamType beam,
            int outer,
            int inner,
            int start,
            int segments,
            float size,
            int layers,
            float thickness,
            Operation<Void> original) {
        BeamAlphaSpace.pront(
                ps, collector, skeleton, wave, beam, outer, inner, start, segments, size, layers,
                thickness);
    }
}
