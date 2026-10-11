// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.blockentity.machine.BlockEntityGeiger;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntityGeiger.class)
public abstract class MixinBlockEntityGeiger {
    @Inject(method = "pick", at = @At("HEAD"), cancellable = true)
    private static void hbmspace$chanceClick(
            double x, ServerLevel level, CallbackInfoReturnable<Integer> cir) {
        if (level.getRandom().nextFloat() > x) cir.setReturnValue(0);
    }
}
