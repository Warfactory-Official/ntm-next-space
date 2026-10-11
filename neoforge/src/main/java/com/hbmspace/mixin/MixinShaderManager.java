// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbmspace.client.fog.FogShaderPatch;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import java.util.Map;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ShaderManager.class)
public abstract class MixinShaderManager {
    @ModifyExpressionValue(
            method = "prepare",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/server/packs/resources/ResourceManager;listResources(Ljava/lang/String;Ljava/util/function/Predicate;)Ljava/util/Map;"))
    private Map<Identifier, Resource> hbmspace$expFogIncludes(Map<Identifier, Resource> files) {
        return FogShaderPatch.patch(files);
    }
}
