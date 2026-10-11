// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.handler.PlayerShield;
import com.hbmspace.extprop.SpacePlayerProps;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerShield.class)
public abstract class MixinPlayerShield {
    @Inject(method = "absorb", at = @At("RETURN"), cancellable = true)
    private static void hbmspace$nitanAfterShield(
            Player player, float amount, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(SpacePlayerProps.absorbNitan(player, cir.getReturnValue()));
    }
}
