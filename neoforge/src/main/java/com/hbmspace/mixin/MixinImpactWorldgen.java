// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.world.ImpactWorldgen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ImpactWorldgen.class)
public abstract class MixinImpactWorldgen {
    @ModifyConstant(
            method =
                    "decorates(Lcom/hbm/saveddata/TomSaveData;Ljava/lang/Boolean;Lnet/minecraft/util/RandomSource;)Z",
            constant = @Constant(intValue = 9))
    private static int hbmspace$regrowthOdds(int odds) {
        return 4;
    }
}
