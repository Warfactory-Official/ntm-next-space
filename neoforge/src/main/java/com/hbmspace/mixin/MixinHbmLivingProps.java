// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin;

import com.hbm.extprop.HbmLivingProps;
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

@Mixin(HbmLivingProps.class)
public abstract class MixinHbmLivingProps {
    @Shadow @Final @Mutable public static MapCodec<HbmLivingProps> MAP_CODEC;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void hbmspace$saveActivation(CallbackInfo ci) {
        MapCodec<HbmLivingProps> base = MAP_CODEC;
        MAP_CODEC =
                RecordCodecBuilder.mapCodec(
                        i ->
                                i.group(
                                                base.forGetter((HbmLivingProps p) -> p),
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

    @Inject(method = "getNeutron", at = @At("HEAD"), cancellable = true)
    private static void hbmspace$disabledRead(
            LivingEntity entity, CallbackInfoReturnable<Double> cir) {
        if (!NeutronExposureSpace.enabled()) cir.setReturnValue(0D);
    }

    @Inject(method = "setNeutron", at = @At("HEAD"), cancellable = true)
    private static void hbmspace$disabledWrite(LivingEntity entity, double dose, CallbackInfo ci) {
        if (!NeutronExposureSpace.enabled()) ci.cancel();
    }
}
