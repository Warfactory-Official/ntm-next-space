// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin.client;

import com.hbm.client.render.flywheel.BeamVisual;
import com.hbm.client.render.flywheel.EmitterVisual;
import com.hbm.render.util.BeamPronter.EnumWaveType;
import com.hbmspace.client.render.BeamAlphaSpace;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EmitterVisual.class)
public abstract class MixinEmitterVisual {
    @WrapOperation(
            method = "writeFrame",
            at = @At(value = "INVOKE", target = BeamAlphaSpace.BEAM_UPDATE))
    private void hbmspace$halfAlpha(
            BeamVisual beam,
            Matrix4fc base,
            Vec3 skeleton,
            EnumWaveType wave,
            int phase,
            int count,
            float size,
            float thickness,
            int outer,
            int inner,
            Operation<Void> original) {
        original.call(
                beam,
                base,
                skeleton,
                wave,
                phase,
                count,
                size,
                thickness,
                BeamAlphaSpace.premultiply(outer),
                BeamAlphaSpace.premultiply(inner));
    }
}
