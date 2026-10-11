// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.client.ScrapMaterialProperty;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ScrapMaterialProperty.class)
public abstract class MixinScrapMaterialProperty {
    @ModifyReturnValue(
            method =
                    "get(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/client/multiplayer/ClientLevel;"
                            + "Lnet/minecraft/world/entity/LivingEntity;ILnet/minecraft/world/item/ItemDisplayContext;)Ljava/lang/String;",
            at = @At("RETURN"),
            require = 1)
    private String hbmspace$deshScrapModel(String model) {
        return "desh".equals(model) ? "workers_alloy" : model;
    }
}
