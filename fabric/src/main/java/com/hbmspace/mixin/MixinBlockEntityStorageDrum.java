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

import com.hbm.blockentity.machine.StorageDrumBlockEntity;
import com.hbmspace.hazard.NeutronExposureSpace;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StorageDrumBlockEntity.class)
public abstract class MixinBlockEntityStorageDrum {
    @Redirect(
            method = "tickServer",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/hbm/util/ContaminationUtil;"
                                            + "neutronActivateItem(Lnet/minecraft/world/item/ItemStack;FF)Z"))
    private boolean hbmspace$replaceLegacyDecay(ItemStack stack, float rad, float factor) {
        return false;
    }

    @Inject(method = "tickServer", at = @At("TAIL"))
    private void hbmspace$sourceDecay(CallbackInfo ci) {
        StorageDrumBlockEntity drum = (StorageDrumBlockEntity) (Object) this;
        for (int slot = 0; slot < drum.getContainerSize(); slot++) {
            ItemStack stack = drum.getItem(slot);
            float before = NeutronExposureSpace.activation(stack);
            NeutronExposureSpace.decay(stack, 0.9899916F);
            if (before != NeutronExposureSpace.activation(stack)) drum.setChanged();
        }
    }
}
