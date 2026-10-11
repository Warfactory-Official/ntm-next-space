// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.items.tool.ItemDosimeter;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemDosimeter.class)
public abstract class MixinItemDosimeter {
    @Inject(method = "pickGeiger", at = @At("HEAD"), cancellable = true)
    private static void hbmspace$chanceClick(
            double x, RandomSource rand, CallbackInfoReturnable<Integer> cir) {
        if (x > 1e-5 && rand.nextFloat() > x) cir.setReturnValue(0);
    }
}
