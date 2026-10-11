// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.hazard.HazardSystem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(HazardSystem.class)
public abstract class MixinHazardSystem {
    @Redirect(
            method = "addHazardInfo",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/hbm/util/ContaminationUtil;getNeutronRads(Lnet/minecraft/world/item/ItemStack;)F"))
    private static float hbmspace$oneActivationTooltip(ItemStack stack) {
        return 0F;
    }
}
