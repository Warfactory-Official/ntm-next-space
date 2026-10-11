// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.handler.ArmorModHandler;
import com.hbmspace.handler.SpaceItemEvents;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorModHandler.class)
public abstract class MixinArmorModHandler {
    @Inject(method = "onKilledByPlayer", at = @At("TAIL"))
    private static void hbmspace$villagerFlesh(LivingEntity entity, CallbackInfo ci) {
        SpaceItemEvents.killedByPlayer(entity);
    }
}
