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

import com.hbm.blockentity.network.CranePartitionerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(CranePartitionerBlockEntity.class)
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
