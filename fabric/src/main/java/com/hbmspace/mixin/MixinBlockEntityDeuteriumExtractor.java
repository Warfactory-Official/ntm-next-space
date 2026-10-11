// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.blockentity.machine.BlockEntityDeuteriumExtractor;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(BlockEntityDeuteriumExtractor.class)
public abstract class MixinBlockEntityDeuteriumExtractor {
    @Shadow public long power;

    @ModifyConstant(
            method = {"tickServer", "hasPower"},
            constant = @Constant(longValue = 20L))
    private long hbmspace$hundredth(long ntm) {
        return 100L;
    }

    @WrapOperation(
            method = "tickServer",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/hbm/blockentity/machine/BlockEntityDeuteriumExtractor;hasPower()Z"))
    private boolean hbmspace$over200(
            BlockEntityDeuteriumExtractor self, Operation<Boolean> original) {
        return original.call(self) && power > 200;
    }
}
