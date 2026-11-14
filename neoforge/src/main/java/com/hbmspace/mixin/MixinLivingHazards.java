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

import com.hbm.extprop.LivingHazards;
import com.hbmspace.hazard.NeutronExposureSpace;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingHazards.class)
public abstract class MixinLivingHazards {
    @Shadow @Final @Mutable public static MapCodec<LivingHazards> CODEC;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void hbmspace$saveActivation(CallbackInfo ci) {
        MapCodec<LivingHazards> base = CODEC;
        CODEC =
                RecordCodecBuilder.mapCodec(
                        i ->
                                i.group(
                                                base.forGetter((LivingHazards p) -> p),
                                                Codec.FLOAT
                                                        .optionalFieldOf("activation", 0F)
                                                        .forGetter(p -> (float) p.neutron))
                                        .apply(
                                                i,
                                                (props, activation) -> {
                                                    props.neutron = activation;
                                                    return props;
                                                }));
    }

    @Inject(
            method = "neutron(Lnet/minecraft/world/entity/LivingEntity;)D",
            at = @At("HEAD"),
            cancellable = true)
    private static void hbmspace$disabledRead(
            LivingEntity entity, CallbackInfoReturnable<Double> cir) {
        if (!NeutronExposureSpace.enabled()) cir.setReturnValue(0D);
    }

    @Inject(
            method = "setNeutron(Lnet/minecraft/world/entity/LivingEntity;D)V",
            at = @At("HEAD"),
            cancellable = true)
    private static void hbmspace$disabledWrite(LivingEntity entity, double dose, CallbackInfo ci) {
        if (!NeutronExposureSpace.enabled()) ci.cancel();
    }
}
