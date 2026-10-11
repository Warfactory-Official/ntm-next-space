// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin.client;

import com.hbm.client.render.PlayerCapes;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.ClientAsset;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerCapes.class)
public abstract class MixinPlayerCapes {
    @Shadow @Final private static ClientAsset.ResourceTexture TEST;

    @ModifyReturnValue(method = "forPlayer", at = @At("RETURN"))
    private static ClientAsset.ResourceTexture hbmspace$noTestCape(
            ClientAsset.ResourceTexture cape) {
        return cape == TEST ? null : cape;
    }
}
