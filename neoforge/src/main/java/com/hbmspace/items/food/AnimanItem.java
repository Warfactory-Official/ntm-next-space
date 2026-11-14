/*
 * SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
 * SPDX-FileCopyrightText: 2023 JamesH_2
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * Copyright (C) 2026 movblock. All rights reserved.
 * Copyright (C) 2023 JamesH_2. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only,
 * as published by the Free Software Foundation.
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this code. If not, see <https://www.gnu.org/licenses/>.
 *
 * Under GPLv3 §7(c) and §7(e), these additional terms apply to LGPL-covered
 * material authored by movblock:
 *
 * - the license grants no rights to the name "NTM NEXT: Space";
 * - a modified version must not be named NTM NEXT: Space or presented as
 *   the official release, and must be marked as different from it.
 *
 * Accurately identifying a work as derived from NTM NEXT: Space does not
 * violate these additional terms, provided it is clearly distinguished from
 * the official release and does not imply endorsement. Ports are encouraged
 * to acknowledge NTM NEXT: Space as their upstream project and provide a
 * link to it.
 */

package com.hbmspace.items.food;

import com.hbm.config.VersatileConfig;
import com.hbm.items.special.CustomLoreItem;
import com.hbm.particle.EffectKinds.VomitMode;
import com.hbm.particle.helper.ParticleCreators;
import com.hbm.sound.EntitySounds;
import com.hbm.sound.ModSounds;
import com.hbmspace.potion.HbmPotionSpace;
import com.hbmspace.sound.ModSoundsSpace;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class AnimanItem extends CustomLoreItem {

    private static final int HUNGER_TICKS = 60;
    private static final int HUNGER_AMPLIFIER = 19;
    private static final int VOMIT_COUNT = 25;

    public AnimanItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (VersatileConfig.hasPotionSickness(player)) return InteractionResult.PASS;
        return super.use(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!(entity instanceof Player player) || !(level instanceof ServerLevel server))
            return result;

        VersatileConfig.applyPotionSickness(player, 5);
        EntitySounds.play(
                player,
                player.getX(),
                player.getY(),
                player.getZ(),
                ModSoundsSpace.PLAYER_OIL.get(),
                SoundSource.PLAYERS,
                1.0F,
                1.0F);
        player.addEffect(
                new MobEffectInstance(
                        BuiltInRegistries.MOB_EFFECT.wrapAsHolder(HbmPotionSpace.RUN.get()),
                        10 * SharedConstants.TICKS_PER_SECOND,
                        0));
        ParticleCreators.vomit(server, player, VomitMode.NORMAL, VOMIT_COUNT);
        EntitySounds.play(
                player,
                player.getX(),
                player.getY(),
                player.getZ(),
                ModSounds.PLAYER_VOMIT.get(),
                SoundSource.PLAYERS,
                1.0F,
                1.0F);
        player.addEffect(new MobEffectInstance(MobEffects.HUNGER, HUNGER_TICKS, HUNGER_AMPLIFIER));
        return result;
    }
}
