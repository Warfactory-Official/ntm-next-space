// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.inventory.OreDictManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(OreDictManager.class)
public abstract class MixinOreDictManager {
    @ModifyConstant(
            method = "<clinit>",
            constant = @Constant(stringValue = "workers_alloy"),
            require = 1)
    private static String hbmspace$desh(String original) {
        return "desh";
    }
}
