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

package com.hbmspace.items.food;

import com.hbmspace.extprop.SpacePlayerProps;
import com.hbmspace.potion.HbmPotionSpace;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class FlaskSpaceItem extends Item {

    private static final int DURATION = 3 * SharedConstants.TICKS_PER_MINUTE;

    public final EnumInfusion type;

    public FlaskSpaceItem(Properties properties, EnumInfusion type) {
        super(properties);
        this.type = type;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide()
                && entity instanceof Player player
                && type == EnumInfusion.NITAN) {
            SpacePlayerProps.addNitanDose(player);
            player.addEffect(
                    new MobEffectInstance(
                            BuiltInRegistries.MOB_EFFECT.wrapAsHolder(HbmPotionSpace.NITAN.get()),
                            DURATION,
                            0));
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, DURATION, 3));
        }
        return result;
    }

    public enum EnumInfusion {
        NITAN
    }
}
