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
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.chunk.storage.SimpleRegionStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(SimpleRegionStorage.class)
public abstract class MixinSimpleRegionStorage {

    @Shadow @Final private DataFixTypes dataFixType;

    @ModifyReturnValue(
            method =
                    "upgradeChunkTag(Lnet/minecraft/nbt/CompoundTag;ILnet/minecraft/nbt/CompoundTag;I)Lnet/minecraft/nbt/CompoundTag;",
            at = @At("RETURN"))
    private CompoundTag hbmspace$upgrade(CompoundTag chunkTag) {
        return spaceData() ? SpaceDataFixers.upgrade(chunkTag) : chunkTag;
    }

    @ModifyVariable(
            method =
                    "write(Lnet/minecraft/world/level/ChunkPos;Ljava/util/function/Supplier;)Ljava/util/concurrent/CompletableFuture;",
            at = @At("HEAD"),
            argsOnly = true)
    private Supplier<CompoundTag> hbmspace$stamp(Supplier<CompoundTag> supplier) {
        if (!spaceData()) return supplier;
        return () -> {
            CompoundTag tag = supplier.get();
            if (tag != null) SpaceDataFixers.stamp(tag);
            return tag;
        };
    }

    private boolean spaceData() {
        return dataFixType == DataFixTypes.CHUNK || dataFixType == DataFixTypes.ENTITY_CHUNK;
    }
}
