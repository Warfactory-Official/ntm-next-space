// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin.client;

import com.hbm.client.render.flywheel.CoreComponentVisual;
import com.hbmspace.client.render.BeamAlphaSpace;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(CoreComponentVisual.class)
public abstract class MixinCoreComponentVisual {
    @ModifyConstant(
            method = "updateMovingParts",
            constant = {@Constant(intValue = 0x404000), @Constant(intValue = 0x401500)})
    private int hbmspace$halfAlpha(int rgb) {
        return BeamAlphaSpace.premultiply(rgb);
    }
}
