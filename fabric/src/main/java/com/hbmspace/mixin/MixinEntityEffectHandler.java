// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.handler.EntityEffectHandler;
import com.hbmspace.hazard.NeutronExposureSpace;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityEffectHandler.class)
public abstract class MixinEntityEffectHandler {
    @Inject(method = "handleRadiation", at = @At("HEAD"))
    private static void hbmspace$activationDecay(LivingEntity entity, CallbackInfo ci) {
        NeutronExposureSpace.tick(entity);
    }
}
