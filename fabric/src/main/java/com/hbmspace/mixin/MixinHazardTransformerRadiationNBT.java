// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.hazard.transformer.HazardTransformerRadiationNBT;
import com.hbm.hazard.transformer.IHazardTransformer;
import com.hbm.items.ModDataComponents;
import com.hbmspace.hazard.NeutronExposureSpace;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HazardTransformerRadiationNBT.class)
public abstract class MixinHazardTransformerRadiationNBT implements IHazardTransformer {
    @Override
    public boolean readsLiveStorage() {
        return true;
    }

    @Inject(method = "appliesTo", at = @At("HEAD"), cancellable = true)
    private void hbmspace$combinedComponentRadiation(
            ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        Float radiation = stack.get(ModDataComponents.HAZ_RADIATION.get());
        cir.setReturnValue(
                radiation != null && radiation + NeutronExposureSpace.activation(stack) > 0F);
    }

    @ModifyArg(
            method = "transform",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/hbm/hazard/HazardEntry;<init>(Lcom/hbm/hazard/type/IHazardType;D)V"),
            index = 1)
    private double hbmspace$sumBeforeGamma(
            double radiation, @Local(argsOnly = true) ItemStack stack) {
        return (float) radiation + NeutronExposureSpace.activation(stack);
    }
}
