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

import com.hbmspace.dim.SpaceGravity;
import com.hbmspace.dim.trait.CBT_Invasion;
import com.hbmspace.extprop.SpacePlayerProps;
import com.hbmspace.handler.SpaceItemEvents;
import com.hbmspace.handler.atmosphere.EveDeath;
import com.hbmspace.items.armor.ArmorModsSpace;
import com.hbmspace.potion.HbmPotionSpace;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import java.util.Collection;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {
    @Inject(
            method = "die",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/entity/LivingEntity;handleKillingBlow()V",
                            shift = At.Shift.AFTER))
    private void hbmspace$acceptedDeath(DamageSource source, CallbackInfo ci) {
        var entity = (LivingEntity) (Object) this;
        EveDeath.onDeath(entity, source);
        SpaceItemEvents.onDeath(entity, source);
        CBT_Invasion.onDeath(entity);
        ArmorModsSpace.onDeath(entity, source);
    }

    @Inject(method = "onEffectsRemoved", at = @At("TAIL"))
    private void hbmspace$clearNitanBuffer(Collection<MobEffectInstance> effects, CallbackInfo ci) {
        if (!((Object) this instanceof Player player) || player.level().isClientSide()) return;
        for (MobEffectInstance effect : effects) {
            if (effect.getEffect().value() == HbmPotionSpace.NITAN.get()) {
                SpacePlayerProps.clearNitanHealth(player);
                return;
            }
        }
    }

    @ModifyExpressionValue(
            method = "getEffectiveGravity",
            at =
                    @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/entity/LivingEntity;getGravity()D"))
    private double hbmspace$bodyGravity(double gravity) {
        return SpaceGravity.effective((LivingEntity) (Object) this, gravity);
    }

    @ModifyExpressionValue(
            method = "travelInAir",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/entity/LivingEntity;omnidirectionalAirMover()Z"))
    private boolean hbmspace$weightlessDrag(boolean omnidirectional) {
        return omnidirectional || SpaceGravity.weightless((LivingEntity) (Object) this);
    }
}
