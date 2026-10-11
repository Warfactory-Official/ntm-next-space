// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin.client;

import com.hbm.client.gui.ScreenMachineSILEX;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ScreenMachineSILEX.class)
public abstract class MixinScreenMachineSILEX {
    @ModifyConstant(method = "waveFrequency", constant = @Constant(floatValue = 0.1F))
    private static float hbmspace$waveBase(float base) {
        return 0.05F;
    }
}
