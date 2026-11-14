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

import com.hbm.items.food.EmeticItem;
import com.hbmspace.extprop.SpacePlayerProps;
import com.hbmspace.potion.HbmPotionSpace;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EmeticItem.class)
public abstract class MixinItemEmetic {
    @Inject(method = "finishUsingItem", at = @At("RETURN"))
    private void hbmspace$purgeNitan(
            ItemStack stack,
            Level level,
            LivingEntity entity,
            CallbackInfoReturnable<ItemStack> cir) {
        if (level instanceof ServerLevel
                && entity instanceof Player player
                && SpacePlayerProps.getNitanCount(player) > 0) {
            player.removeEffect(
                    BuiltInRegistries.MOB_EFFECT.wrapAsHolder(HbmPotionSpace.NITAN.get()));
            SpacePlayerProps.setNitanCount(player, 0);
            SpacePlayerProps.clearNitanHealth(player);
        }
    }
}
