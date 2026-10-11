// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.blockentity.network.BlockEntityCranePartitioner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(BlockEntityCranePartitioner.class)
public abstract class MixinBlockEntityCranePartitioner {
    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 90), require = 1, allow = 1)
    private static int hbmspace$inventorySize(int original) {
        return 104;
    }

    @ModifyConstant(
            method = "accessAll",
            constant = @Constant(intValue = 90),
            require = 1,
            allow = 1)
    private static int hbmspace$sidedSlots(int original) {
        return 104;
    }

    @ModifyConstant(
            method = "tickServer",
            constant = @Constant(intValue = 45),
            require = 2,
            allow = 2)
    private int hbmspace$queueTickEnd(int original) {
        return 96;
    }

    @ModifyConstant(
            method = "canPlaceItem",
            constant = @Constant(intValue = 45),
            require = 1,
            allow = 1)
    private int hbmspace$queueInsertEnd(int original) {
        return 96;
    }

    @ModifyConstant(
            method = "canTakeItemThroughFace",
            constant = @Constant(intValue = 45),
            require = 1,
            allow = 1)
    private int hbmspace$declogExtractStart(int original) {
        return 96;
    }
}
