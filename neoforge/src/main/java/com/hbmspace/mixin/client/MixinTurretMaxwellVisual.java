// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin.client;

import com.hbm.client.render.flywheel.TurretMaxwellVisual;
import com.hbmspace.client.render.BeamAlphaSpace;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(TurretMaxwellVisual.class)
public abstract class MixinTurretMaxwellVisual {
    @ModifyConstant(method = "updateMovingParts", constant = @Constant(intValue = 0x2020FF))
    private int hbmspace$halfAlpha(int rgb) {
        return BeamAlphaSpace.premultiply(rgb);
    }
}
