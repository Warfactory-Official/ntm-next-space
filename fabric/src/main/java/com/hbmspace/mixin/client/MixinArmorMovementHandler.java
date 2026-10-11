// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.mixin.client;

import com.hbm.client.ArmorMovementHandler;
import com.hbm.extprop.HbmPlayerProps;
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
            @Local HbmPlayerProps props,
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
