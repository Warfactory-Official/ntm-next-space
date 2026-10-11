// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbmspace.client.fog.BoundTerrainFog;
import com.hbmspace.client.fog.SodiumFogBridge;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.caffeinemc.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import net.caffeinemc.mods.sodium.client.util.FogParameters;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

@Pseudo
@Mixin(
        targets = "net.caffeinemc.mods.sodium.client.render.chunk.UniformBufferManager",
        remap = false)
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
        return SodiumFogBridge.project(original, (BoundTerrainFog) (Object) parameters);
    }
}
