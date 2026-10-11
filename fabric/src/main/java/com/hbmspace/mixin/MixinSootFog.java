// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.client.SootFog;
import com.hbmspace.client.fog.TerrainExpFog;
import com.hbmspace.client.fog.TerrainFogPayload;
import net.minecraft.client.renderer.fog.FogData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(SootFog.class)
public abstract class MixinSootFog {
    @ModifyVariable(method = "apply", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private static boolean hbmspace$celestialRange(boolean atmospheric, FogData fog) {
        return atmospheric && (((TerrainFogPayload) fog).hbmspace$flags() & TerrainExpFog.EXP) == 0;
    }
}
