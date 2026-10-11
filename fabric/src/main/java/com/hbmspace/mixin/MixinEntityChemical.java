// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.entity.projectile.EntityChemical;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityChemical.class)
public abstract class MixinEntityChemical {
    @Shadow
    private void seedBlock(BlockPos target) {
        throw new AssertionError();
    }

    @Inject(method = "seedSlurry", at = @At("HEAD"), cancellable = true)
    private void hbmspace$struckBlockAlone(BlockPos pos, CallbackInfo ci) {
        seedBlock(pos);
        ci.cancel();
    }
}
