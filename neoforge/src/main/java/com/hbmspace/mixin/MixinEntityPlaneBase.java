// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.entity.logic.EntityPlaneBase;
import com.hbm.particle.helper.ExplosionCreator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlaneBase.class)
public abstract class MixinEntityPlaneBase {
    @Inject(
            method = "tick",
            at =
                    @At(
                            value = "INVOKE",
                            target = "Lcom/hbm/entity/logic/EntityPlaneBase;discard()V",
                            ordinal = 0,
                            shift = At.Shift.AFTER))
    private void hbmspace$crashEffect(CallbackInfo ci) {
        EntityPlaneBase plane = (EntityPlaneBase) (Object) this;
        ExplosionCreator.composeEffectLarge(
                plane.level(), plane.getX(), plane.getY(), plane.getZ());
    }
}
