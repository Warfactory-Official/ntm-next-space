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

import com.hbm.extprop.PlayerState;
import com.hbm.handler.threading.TargetPoint;
import com.hbm.items.armor.ArmorSuitEffects.Jetpack;
import com.hbm.items.armor.ArmorSuitEffects;
import com.hbm.packet.PacketWire;
import com.hbm.packet.layout.JetpackDnsLayout;
import com.hbm.sound.EntitySounds;
import com.hbm.sound.ModSounds;
import com.hbmspace.items.armor.SuitFlightSpace;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorSuitEffects.class)
public abstract class MixinArmorSuitEffects {

    @Shadow
    private static boolean thrusting(ServerPlayer player, Jetpack kind) {
        throw new AssertionError();
    }

    @Shadow
    private static boolean gliding(
            ServerPlayer player, Jetpack kind, PlayerState props, boolean thrusting) {
        throw new AssertionError();
    }

    @Inject(method = "heldAloft", at = @At("HEAD"), cancellable = true)
    private static void hbmspace$brakeHeldAloft(
            ServerPlayer player, CallbackInfoReturnable<Boolean> cir) {
        if (ArmorSuitEffects.jetpackKind(player) != Jetpack.BRAKE) return;
        boolean thrusting = thrusting(player, Jetpack.BRAKE);
        cir.setReturnValue(
                thrusting
                        || gliding(
                                player, Jetpack.BRAKE, PlayerState.getOrCreate(player), thrusting));
    }

    @Inject(method = "jetpackServerTick", at = @At("HEAD"), cancellable = true)
    private static void hbmspace$dnsServerTick(ServerPlayer player, Jetpack kind, CallbackInfo ci) {
        if (kind != Jetpack.DNS) return;
        ci.cancel();
        PlayerState props = PlayerState.getOrCreate(player);
        boolean thrusting = thrusting(player, kind);
        boolean shows =
                thrusting
                        || (!player.isFallFlying() || props.jetpackActive())
                                && SuitFlightSpace.dnsShowsThrust(player, props.enableBackpack);
        if (shows) {
            PacketWire.sendToAllAround(
                    JetpackDnsLayout.encode(player.getId()),
                    new TargetPoint(
                            player.level(), player.getX(), player.getY(), player.getZ(), 100D));
        }
        if (thrusting || gliding(player, kind, props, thrusting)) player.fallDistance = 0;
        if (shows) {
            EntitySounds.play(
                    player,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    ModSounds.JETPACK_THRUST.get(),
                    SoundSource.PLAYERS,
                    0.125F,
                    1.5F);
        }
    }
}
