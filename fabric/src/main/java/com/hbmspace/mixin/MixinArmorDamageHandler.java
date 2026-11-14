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

import com.hbm.items.armor.ArmorDamageHandler;
import com.hbmspace.lib.ModDamageTypesSpace;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ArmorDamageHandler.class)
public abstract class MixinArmorDamageHandler {
    @WrapOperation(
            method = "filterAttack",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/hbm/items/armor/ArmorDamageHandler;plink(Lnet/minecraft/world/entity/player/Player;)V",
                            ordinal = 1))
    private static void hbmspace$silentOxyprime(
            Player player, Operation<Void> original, @Local(argsOnly = true) DamageSource source) {
        if (!source.is(ModDamageTypesSpace.OXYPRIME)) original.call(player);
    }
}
