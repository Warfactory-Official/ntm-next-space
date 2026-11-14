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

import com.hbmspace.client.fog.SodiumFogBridge;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.caffeinemc.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import net.caffeinemc.mods.sodium.client.render.chunk.UniformBufferManager;
import net.caffeinemc.mods.sodium.client.util.FogParameters;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

@Mixin(value = UniformBufferManager.class, remap = false)
public abstract class MixinUniformBufferManager {
    @Coerce
    @ModifyExpressionValue(
            method = "update",
            at =
                    @At(
                            value = "NEW",
                            target =
                                    "net/caffeinemc/mods/sodium/client/render/chunk/UniformBufferManager$GlobalUniforms"))
    private Object hbmspace$frameSnapshot(
            @Coerce Object original, ChunkRenderMatrices matrices, FogParameters parameters) {
        return SodiumFogBridge.project(
                original,
                ((com.hbmspace.client.fog.BoundTerrainFog) (java.lang.Object) (parameters)));
    }
}
