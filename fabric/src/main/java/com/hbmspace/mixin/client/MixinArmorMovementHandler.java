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

package com.hbmspace.mixin.client;

import com.hbm.client.ArmorMovementHandler;
import com.hbm.extprop.PlayerState;
import com.hbm.items.armor.ArmorSuitEffects.Jetpack;
import com.hbmspace.items.armor.SuitFlightSpace;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorMovementHandler.class)
public abstract class MixinArmorMovementHandler {

    @Shadow
    private static float forwardInput() {
        throw new AssertionError();
    }

    @Shadow
    private static float strafeInput() {
        throw new AssertionError();
    }

    @Inject(
            method = "jetpack",
            cancellable = true,
            at =
                    @At(
                            value = "FIELD",
                            target =
                                    "Lcom/hbm/items/armor/ArmorSuitEffects$Jetpack;DNS:Lcom/hbm/items/armor/ArmorSuitEffects$Jetpack;",
                            ordinal = 1))
    private static void hbmspace$dnsGlide(
            LocalPlayer player,
            Jetpack kind,
            CallbackInfo ci,
            @Local(ordinal = 0) Vec3 motion,
            @Local PlayerState props,
            @Local(ordinal = 0) float gravity) {
        if (kind != Jetpack.DNS) return;
        player.setDeltaMovement(
                SuitFlightSpace.dnsGlide(
                        player,
                        motion,
                        props.enableBackpack,
                        forwardInput(),
                        strafeInput(),
                        gravity));
        ci.cancel();
    }
}
