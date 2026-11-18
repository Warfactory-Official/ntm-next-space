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

import com.hbm.blockentity.machine.storage.LockedContents;
import com.hbmspace.util.datafix.SpaceDataFixers;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LockedContents.class)
public abstract class MixinLockedContents {

    @ModifyExpressionValue(
            method = "open",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/hbm/util/datafix/HbmDataFixers;upgradeSealedItems(Lnet/minecraft/nbt/CompoundTag;)"
                                            + "Lnet/minecraft/nbt/CompoundTag;"))
    private CompoundTag hbmspace$upgradeSealedItems(CompoundTag tag) {
        return SpaceDataFixers.upgrade(tag);
    }
}
