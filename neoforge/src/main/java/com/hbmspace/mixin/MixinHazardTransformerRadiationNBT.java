/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file under the Apache License except in
 * compliance with that License. You may obtain a copy at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Apache License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License for the specific language governing permissions
 * and limitations under the License.
 */

package com.hbmspace.mixin;

import com.hbm.hazard.transformer.HazardTransformer;
import com.hbm.hazard.transformer.HazardTransformerRadiationNBT;
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
public abstract class MixinHazardTransformerRadiationNBT implements HazardTransformer {
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
                                    "Lcom/hbm/hazard/HazardEntry;<init>(Lcom/hbm/hazard/type/HazardKind;D)V"),
            index = 1)
    private double hbmspace$sumBeforeGamma(
            double radiation, @Local(argsOnly = true) ItemStack stack) {
        return (float) radiation + NeutronExposureSpace.activation(stack);
    }
}
