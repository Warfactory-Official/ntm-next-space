/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file under the Apache License except in
 * compliance with that License. You may obtain a copy at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Apache License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License for the specific language governing permissions
 * and limitations under the License.
 */

package com.hbmspace.mixin;

import com.hbm.blocks.network.CranePartitionerBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(CranePartitionerBlock.class)
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
