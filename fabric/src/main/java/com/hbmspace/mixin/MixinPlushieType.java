// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.blocks.generic.BlockPlushie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(BlockPlushie.PlushieType.class)
public abstract class MixinPlushieType {
    @ModifyConstant(
            method = "squeak",
            constant = @Constant(floatValue = 100.0F),
            require = 1,
            allow = 1)
    private float hbmspace$hundunVolume(float volume) {
        return 0.25F;
    }
}
