// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.hbm.hazard.HazardSystem$PlayerHazardData")
public abstract class MixinPlayerHazardData {
    @Inject(method = "applyNeutronActivation", at = @At("HEAD"), cancellable = true)
    private void hbmspace$sourceNeutronSystem(float carried, CallbackInfo ci) {
        ci.cancel();
    }
}
