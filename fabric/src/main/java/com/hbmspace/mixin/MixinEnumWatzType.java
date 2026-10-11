// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.items.machine.ItemWatzPellet;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemWatzPellet.EnumWatzType.class)
public abstract class MixinEnumWatzType {
    @ModifyReturnValue(method = "yield", at = @At("RETURN"))
    private double hbmspace$yield(double ntm) {
        return 1_000_000_000D;
    }
}
