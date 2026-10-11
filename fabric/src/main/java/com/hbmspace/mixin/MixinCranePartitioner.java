// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.blocks.network.CranePartitioner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(CranePartitioner.class)
public abstract class MixinCranePartitioner {
    @ModifyConstant(
            method = "onItemEnter",
            constant = @Constant(intValue = 45),
            require = 1,
            allow = 1)
    private int hbmspace$queueSize(int original) {
        return 96;
    }

    @ModifyArg(
            method = "onItemEnter",
            at =
                    @At(
                            value = "INVOKE",
                            ordinal = 1,
                            target =
                                    "Lcom/hbm/util/InventoryUtil;tryAddItemToInventory(Ljava/util/List;IILnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"),
            index = 2,
            require = 1,
            allow = 1)
    private int hbmspace$declogLastSlot(int original) {
        return 103;
    }
}
