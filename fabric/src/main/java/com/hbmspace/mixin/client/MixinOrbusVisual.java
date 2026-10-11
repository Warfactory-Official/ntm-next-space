// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin.client;

import com.hbm.client.render.flywheel.OrbusVisual;
import com.hbmspace.client.render.BeamAlphaSpace;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(OrbusVisual.class)
public abstract class MixinOrbusVisual {
    @ModifyConstant(
            method = "writeFrame",
            constant = {@Constant(intValue = 0x101020), @Constant(intValue = 0x202060)})
    private int hbmspace$halfAlpha(int rgb) {
        return BeamAlphaSpace.premultiply(rgb);
    }
}
