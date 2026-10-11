// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbmspace.dim.SpaceDimensions;
import com.hbmspace.dim.duna.DunaDustStorm;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Level.class)
public abstract class MixinLevel {
    @ModifyReturnValue(method = "isBrightOutside", at = @At("RETURN"))
    private boolean hbmspace$stormNight(boolean bright) {
        Level level = (Level) (Object) this;
        if (!bright || level.dimension() != SpaceDimensions.DUNA) return bright;
        float intensity =
                level instanceof ServerLevel server
                        ? DunaDustStorm.get(server).intensity
                        : DunaDustStorm.clientIntensity;
        return !(intensity >= 0.2F);
    }
}
