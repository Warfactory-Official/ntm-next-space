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

import com.hbmspace.util.datafix.SpaceDataFixers;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.SavedDataStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SavedDataStorage.class)
public abstract class MixinSavedDataStorage {

    @ModifyReturnValue(method = "readTagFromDisk", at = @At("RETURN"))
    private CompoundTag hbmspace$upgrade(CompoundTag tag) {
        return SpaceDataFixers.upgrade(tag);
    }

    @ModifyReturnValue(method = "encodeUnchecked", at = @At("RETURN"))
    private CompoundTag hbmspace$stamp(CompoundTag tag) {
        SpaceDataFixers.stamp(tag);
        return tag;
    }
}
