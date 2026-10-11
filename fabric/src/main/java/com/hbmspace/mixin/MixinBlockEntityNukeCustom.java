// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.blockentity.bomb.BlockEntityNukeCustom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(BlockEntityNukeCustom.class)
public abstract class MixinBlockEntityNukeCustom {
    @ModifyConstant(
            method = "totals",
            constant = @Constant(floatValue = BlockEntityNukeCustom.AMAT_NEEDS_NUKE, ordinal = 0))
    private float hbmspace$amatNeedsNoNuke(float needs) {
        return 0F;
    }
}
